from graph import Graph
import dill as pickle
import sys
import json
import os
from tqdm import tqdm

CORPUS_GRAPH_DUMP, _ = os.path.splitext(sys.argv[1])
CORPUS_GRAPH_DUMP += ".graph"

questions = {}
with open("../../data/ARC-V1-Feb2018-2/ARC-Challenge/ARC-Challenge-Test.jsonl", "r") as in_file:
    for line in (in_file):
        line = json.loads(line)
        question = line['question']['stem']
        questions[line['id']] = question

stem = sys.argv[2]
stem = True if stem == "True" else False
print(stem)

if not os.path.exists(CORPUS_GRAPH_DUMP):
    print("Creating graph from corpus triples")
    corpus_graph = Graph(sys.argv[1], stem=stem, disable_progress_bar=False)
    print("Dumping graph object for future use")
    pickle.dump(corpus_graph, open(CORPUS_GRAPH_DUMP, "wb"))
else:
    print("Corpus graph already exists. Loading it....")
    corpus_graph = pickle.load(open(CORPUS_GRAPH_DUMP, "rb"))

print(corpus_graph)
qa_graphs = pickle.load(open(sys.argv[3], "rb"))
out = sys.argv[4]
scores = {}

for question_id in qa_graphs:
    scores[question_id] = {}

    scores[question_id]['correct_answer'] = qa_graphs[question_id]['correct_answer']
    scores[question_id]["options"] = {}
    for key in qa_graphs[question_id]['option_graphs']:
        scores[question_id]["options"][key] = corpus_graph.compare_graph(qa_graphs[question_id]['option_graphs'][key])

    # print(scores[question_id])

points = 0
with open(out, "w") as f:
    for question_id in scores:
        point = 0
        correct_answer = scores[question_id]['correct_answer']
        option_scores = list(scores[question_id]['options'].items())
        if len(option_scores) == 0:
            point = 0.25
            possible_answers = ['A', 'B', 'C', 'D']
        else:
            maximum = max(option_scores, key=lambda x: x[1])
            possible_answers = [each[0] for each in option_scores if each[1] == maximum[1]]
            if correct_answer in possible_answers:
                point = (1 / len(possible_answers))

        points += point
        f.write("%s\t%s\t%s\t%f\n" % (questions[question_id], correct_answer, str(possible_answers), point))

print(points / len(scores))
