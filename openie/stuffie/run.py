import click
import json
import os
import pickle
import sys
import time

from graph_stuffie import Graph, Node
from operator import itemgetter
from tqdm import tqdm

sys.path.append("..")
import utils


def load_corpus_graphs(corpus_graph_file):
    g = Graph()
    print(f"Loading Corpus Graph from {corpus_graph_file}....")
    st = time.time()
    g.load(corpus_graph_file)
    et = time.time()
    print("Graph Loading Complete. Took %f minutes" % ((et - st) / 60))
    return g


def get_qa_graph(path):
    """
    Load the qa graph pickle dump located @PATH
    """
    print(f"Loading QA Dump @ {path}")
    return pickle.load(open(path, "rb"))


def match(string1, string2):
    return 1 if string1 == string2 else 0


def score_graphs(corpus_graph, hypothesis_graph, depth_threshold=2, match_threshold=0.5, beam_threshold=4):
    score = 0
    corpus_graph_nodes = corpus_graph.nodes
    hypo_graph_nodes = hypothesis_graph.nodes

    # Sort the hypothesis nodes in descending order of the depths of subtree
    sorted_hypo_graph_node_names = list(hypo_graph_nodes.keys())
    sorted_hypo_graph_node_names.sort(key=lambda node_name: hypothesis_graph.get_node_height(node_name), reverse=True)

    visited = {}
    for phrase in sorted_hypo_graph_node_names:

        if phrase not in visited and phrase in corpus_graph_nodes:

            hypo_nodes = [(phrase, 1)]
            cand_nodes_from_corpus = {phrase: [(phrase, 0)]}

            while len(cand_nodes_from_corpus) != 0:
                # print(len(hypo_nodes), len(cand_nodes_from_corpus))
                # import ipdb
                # ipdb.set_trace()
                # hnode -> next node from hypothesis graph that we
                # are trying to match
                # hnd -> its position in the path from the start node
                new_hypo_nodes = []
                for hnode, hnd in hypo_nodes:

                    # candidates -> list of (node, depth) in corpus graph with which hnode can match
                    candidates = cand_nodes_from_corpus[hnode]

                    match_scores = []
                    whose_child_to_be_skipped = []
                    for cand, depth in candidates:

                        # skip those search paths which have crossed the depth threshold
                        if depth == depth_threshold:
                            whose_child_to_be_skipped.append(cand)
                            continue
                        match_scores.append((cand, match(hnode, cand)))

                    winner_cand, max_score = max(match_scores, key=lambda x: x[1]) if len(match_scores) != 0 else (None, 0)

                    # found the most probable match
                    if max_score > match_threshold:
                        score += (max_score * hnd)  # the deeper match in hypothesis graph higher the score

                        visited[hnode] = True
                        del cand_nodes_from_corpus[hnode]

                        nbrs = hypo_graph_nodes[hnode].edges
                        for nbr in nbrs:
                            if nbr not in visited:
                                new_hypo_nodes.append((nbr, hnd + 1))
                                cand_nodes_from_corpus[nbr] = [(edge, 0) for edge in corpus_graph_nodes[winner_cand].edges]
                                cand_nodes_from_corpus[nbr] = cand_nodes_from_corpus[nbr][0:beam_threshold]

                    else:
                        new_candidates = {}
                        for cand, depth in candidates:
                            if cand not in whose_child_to_be_skipped:
                                for new_cand in corpus_graph_nodes[cand].edges:
                                    if new_cand not in new_candidates:
                                        new_candidates[new_cand] = depth + 1
                                    else:
                                        new_candidates[new_cand] = min(depth + 1, new_candidates[new_cand])

                        new_candidates = list(new_candidates.items())
                        del cand_nodes_from_corpus[hnode]
                        if len(new_candidates) != 0:
                            new_hypo_nodes.append((hnode, hnd))
                            cand_nodes_from_corpus[hnode] = new_candidates[0:beam_threshold]

                del hypo_nodes
                hypo_nodes = list(new_hypo_nodes)

    return score


def test():
    # Test corpus Graph
    cg = Graph()
    a = Node('a')
    b = Node('b')
    c = Node('c')
    d = Node('d')
    p = Node('p')
    q = Node('q')
    r = Node('r')
    a.add_edge(b)
    a.add_edge(c)
    c.add_edge(p)
    c.add_edge(q)
    p.add_edge(r)
    cg.nodes = {
        'a': a,
        'b': b,
        'c': c,
        'd': d,
        'p': p,
        'q': q,
        'r': r
    }

    # Test Hypo Graph
    hg = Graph()
    a = Node('a')
    d = Node('d')
    p = Node('p')
    r = Node('r')
    a.add_edge(p)
    a.add_edge(d)
    p.add_edge(r)
    hg.nodes = {
        'a': a,
        'd': d,
        'p': p,
        'r': r
    }

    print(score_graphs(cg, hg))


def get_question_details():
    questions = {}
    with open("../../data/ARC-V1-Feb2018-2/ARC-Challenge/ARC-Challenge-Test.jsonl", "r") as in_file:
        for line in (in_file):
            line = json.loads(line)
            question = line['question']['stem']
            options = {}

            for choice in line['question']['choices']:
                label = choice['label']
                if label not in options:
                    options[label] = choice['text']

            hypothesis = {}
            question = utils.replace_wh_word_with_blank(question)

            for option in options:
                if options[option] != "":
                    hypothesis[option] = utils.create_hypothesis(question, options[option])

            questions[line['id']] = [question, hypothesis, options]
    return questions


def score_questions(corpus_graph, qa_graphs, depth_threshold=2, match_threshold=0.5, beam_threshold=4, dumps=None):

    scores = {}
    questions = get_question_details()
    print("Calculating scores for each question...")

    for question_id in tqdm(qa_graphs, ascii=True):
        scores[question_id] = {}
        scores[question_id]['correct_answer'] = qa_graphs[question_id]['correct_answer']
        scores[question_id]["options"] = {}

        matches, hypo_scores = {}, {}
        matches["question"] = questions[question_id][0]

        for key in qa_graphs[question_id]['hypothesis_graphs']:
            hypothesis_graph = qa_graphs[question_id]['hypothesis_graphs'][key]
            hypo_scores[key]= score_graphs(
                corpus_graph,
                hypothesis_graph,
                depth_threshold=depth_threshold,
                match_threshold=match_threshold,
                beam_threshold=beam_threshold
            )

            matches[key] = {
                # 'graph': hypothesis_match,
                'hypothesis': questions[question_id][1][key],
                'option': questions[question_id][2][key]
            }

            scores[question_id]["options"][key] = hypo_scores[key]

        if dumps is not None:
            json.dump(matches, open(os.path.join(dumps, question_id + ".json"), "w"), indent=4)

    print(len(scores))
    return scores


def make_predictions(scores, prediction_file):
    points = 0
    p_at = {1: 0, 2: 0, 3: 0}

    prediction_loc = os.path.dirname(prediction_file)
    if prediction_loc and not os.path.exists(prediction_loc):
        os.makedirs(prediction_loc)

    with open(prediction_file, "w") as f:
        for question_id in tqdm(scores, ascii=True):
            point = 0
            correct_answer = scores[question_id]['correct_answer']
            option_scores = list(scores[question_id]['options'].items())
            if len(option_scores) == 0:
                print("This shouldn't have happened")
                input()
            else:
                maximum = max(option_scores, key=lambda x: x[1])
                possible_answers = [each[0] for each in option_scores if each[1] == maximum[1]]

                if correct_answer in possible_answers:
                    point = (1 / len(possible_answers))

                option_scores.sort(key=itemgetter(1, 0), reverse=True)
                ranked_answers = [each[0] for each in option_scores]
                for i in range(0, 3):
                    if correct_answer in ranked_answers[0:i + 1]:
                        p_at[i + 1] += 1

            points += point
            # f.write("%s\t%s\t%s\t%s\t%f\n" % (question_id, questions[question_id], correct_answer, str(possible_answers), point))
            f.write("%s,%s\n" % (question_id, ";".join(possible_answers)))

    for key in p_at:
        p_at[key] = p_at[key] / len(scores)

    print("Number of questions: ", len(scores))
    print("Score: ", points / len(scores))
    print("Precisoin at: ")
    for key in p_at:
        print("\t%d: " % key, p_at[key])
    print("Exiting...")


@click.command()
@click.argument('Corpus_Triplets_graph')
@click.argument("qa_graph_path", type=click.Path(exists=True))
@click.argument("prediction_file", type=click.Path())
@click.option('--stem', is_flag=True, help='Use this if you want to perform stemming on each word.')
def main(corpus_triplets_graph, qa_graph_path, prediction_file, stem):
    """
    1. Create Corpus Graph given its openie triplets in the stanford openIE format in the file "corpus_triplets_graph".\n
    2. Load the qa graph pickle dump located @PATH\n
    3. Calculate Scores for each option for each graph.\n
    4. Predict possible answers for each question and save them in the PREDICTION_FILE.\n
    """
    corpus_graph = Graph()
    print("Loading Corpus Graph...")
    corpus_graph.load(corpus_triplets_graph)
    print("Loading Graph Complete...")
    qa_graphs = get_qa_graph(qa_graph_path)
    scores = score_questions(corpus_graph, qa_graphs, depth_threshold=1)
    make_predictions(scores, prediction_file)


if __name__ == '__main__':
    main()
    # main("graphs/corpus_graphs/corpus_graph", "graphs/qa_graphs", "temp", False)
    # test()
