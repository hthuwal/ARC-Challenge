import click
import json
import nltk
import os
import objgraph
import pickle
import resource
import sys
import time

from functools import partial
from graph_stuffie import Graph, Node
from multiprocessing import Pool
from operator import itemgetter
from tqdm import tqdm
from utils_stuffie import timeit

sys.path.append("..")
from utils import replace_wh_word_with_blank, create_hypothesis


corpus_graph = None
qa_graphs = None
questions = None


@timeit
def load_corpus_graphs(corpus_graph_file):
    g = Graph()
    print(f"Loading Corpus Graph from {corpus_graph_file}....")
    g.load(corpus_graph_file)
    print("Graph Loading Complete...")
    return g


@timeit
def get_qa_graph(path):
    """
    Load the qa graph pickle dump located @PATH
    """
    print(f"Loading QA Dump @ {path}")
    return pickle.load(open(path, "rb"))


def match(string1, string2):
    # return 1 if string1 == string2 else 0
    return max(0, (1 - nltk.jaccard_distance(set(string1), set(string2))))

    # return 0


def score_graphs(hypothesis_graph, depth_threshold=2, match_threshold=0.5, beam_threshold=4):
    global corpus_graph
    score = 0
    corpus_graph_nodes = corpus_graph.nodes
    hypo_graph_nodes = hypothesis_graph.nodes

    # Sort the hypothesis nodes in descending order of the depths of subtree
    sorted_hypo_graph_node_names = list(hypo_graph_nodes.keys())
    sorted_hypo_graph_node_names.sort(key=lambda node_name: hypothesis_graph.get_node_height(node_name), reverse=True)

    visited = {}
    # print("Hypo Graph:\n", repr(hypothesis_graph), "\n")

    for phrase in sorted_hypo_graph_node_names:
        if phrase not in visited and phrase in corpus_graph_nodes:

            hypo_nodes = [(phrase, 1)]
            cand_nodes_from_corpus = {phrase: [(phrase, 0)]}

            while len(hypo_nodes) != 0:
                # hnode -> next node from hypothesis graph that we
                # are trying to match
                # hnd -> its position in the path from the start node
                hnode, hnd = hypo_nodes.pop()

                # candidates -> list of (node, depth) in corpus graph with which hnode can match
                # print(f"{hnode}, {hnd}\n{hypo_nodes}\n{cand_nodes_from_corpus}\n\n")

                candidates = cand_nodes_from_corpus[hnode]

                match_scores = []
                for cand, depth in candidates:
                    match_scores.append((cand, depth, match(hnode, cand)))

                winner_cand, winner_depth, max_score = max(match_scores, key=lambda x: x[2]) if len(match_scores) != 0 else (None, 0, 0)
                del match_scores

                # found the most probable match
                if winner_cand is not None and max_score > match_threshold:
                    score += ((max_score * hnd) / (winner_depth + 1))  # the deeper match in hypothesis graph higher the score

                    visited[hnode] = True
                    del cand_nodes_from_corpus[hnode]

                    nbrs = hypo_graph_nodes[hnode].edges
                    for nbr in nbrs:
                        if nbr not in visited:
                            idx = None
                            for i, (node, _) in enumerate(hypo_nodes):
                                if nbr == node:
                                    idx = i
                                    break

                            if idx is not None:
                                hypo_nodes[i] = (nbr, max(hypo_nodes[i][1], hnd + 1))
                            else:
                                hypo_nodes.append((nbr, hnd + 1))

                            cand_nodes_from_corpus[nbr] = [(edge, 0) for edge in corpus_graph_nodes[winner_cand].edges]
                            cand_nodes_from_corpus[nbr] = cand_nodes_from_corpus[nbr][0:beam_threshold]

                else:
                    new_candidates = {}
                    for cand, depth in candidates:
                        # skip those search paths which have crossed the depth threshold
                        if depth < depth_threshold:
                            for new_cand in corpus_graph_nodes[cand].edges:
                                if new_cand not in new_candidates:
                                    new_candidates[new_cand] = depth + 1
                                else:
                                    new_candidates[new_cand] = min(depth + 1, new_candidates[new_cand])

                    new_candidates = list(new_candidates.items())
                    del cand_nodes_from_corpus[hnode]
                    if len(new_candidates) != 0:
                        hypo_nodes.append((hnode, hnd))
                        cand_nodes_from_corpus[hnode] = new_candidates[0:beam_threshold]

    score = score / len(hypo_graph_nodes) if len(hypo_graph_nodes) != 0 else score

    del corpus_graph_nodes
    del hypo_graph_nodes
    del sorted_hypo_graph_node_names
    del visited

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

    print(f"Corpus Graph\n\n {cg}")
    print(f"Hypothesis Graph\n\n {hg}")

    print(score_graphs(cg, hg, depth_threshold=1))


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
            question = replace_wh_word_with_blank(question)

            for option in options:
                if options[option] != "":
                    hypothesis[option] = create_hypothesis(question, options[option])

            questions[line['id']] = [question, hypothesis, options]
    return questions


def score_question(question_id, depth_threshold=2, match_threshold=0.5, beam_threshold=4, dumps=None):
    global questions, qa_graphs
    score = {}
    # print("Calculating scores for each question...")

    score[question_id] = {}
    score[question_id]['correct_answer'] = qa_graphs[question_id]['correct_answer']
    score[question_id]["options"] = {}

    matches, hypo_scores = {}, {}
    matches["question"] = questions[question_id][0]

    for key in qa_graphs[question_id]['hypothesis_graphs']:
        hypothesis_graph = qa_graphs[question_id]['hypothesis_graphs'][key]
        hypo_scores[key] = score_graphs(
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

        score[question_id]["options"][key] = hypo_scores[key]

    # if dumps is not None:
    #     json.dump(matches, open(os.path.join(dumps, question_id + ".json"), "w"), indent=4)

    return score


def score_questions(depth_threshold=2, match_threshold=0.5, beam_threshold=4, dumps=None):
    global qa_graphs

    question_ids = list(qa_graphs.keys())
    func = partial(score_question, depth_threshold=depth_threshold, match_threshold=match_threshold, beam_threshold=beam_threshold, dumps=dumps)
    with Pool() as pool:
        scores = list(tqdm(pool.imap(func, question_ids), ascii=True, total=len(question_ids)))

    scores_dict = {}
    for score in scores:
        scores_dict.update(score)

    return scores_dict


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


@click.command()
@click.argument('Corpus_Triplets_graph')
@click.argument("qa_graph_path", type=click.Path(exists=True))
@click.argument("prediction_file", type=click.Path())
@click.option('--stem', is_flag=True, help='Use this if you want to perform stemming on each word.')
def main(corpus_triplets_graph, qa_graph_path, prediction_file, stem):
    """
    1. Load the corpus graph.\n
    2. Load the qa graph pickle dump located @PATH\n
    3. Calculate Scores for each option for each graph.\n
    4. Predict possible answers for each question and save them in the PREDICTION_FILE.\n
    """
    global questions, corpus_graph, qa_graphs

    corpus_graph = Graph()
    print("Loading Corpus Graph...")
    timeit(corpus_graph.load)(corpus_triplets_graph)

    qa_graphs = get_qa_graph(qa_graph_path)
    questions = get_question_details()

    while True:
        x = input("Enter d, m and b (Enter q to exit)\n").strip()
        if x == "q" or x == "Q":
            break
        try:
            x = list(x.split())
            x = [int(x[0]), float(x[1]), int(x[2])]
            scores = score_questions(*x)
            make_predictions(scores, prediction_file='faltu')
        except Exception as e:
            continue

    # objgraph.show_most_common_types()
    mem = resource.getrusage(resource.RUSAGE_SELF).ru_maxrss
    print(f"Memory usage is: {mem} KB")


if __name__ == '__main__':
    main()
