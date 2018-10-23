from collections import defaultdict
from graph import Graph
from operator import itemgetter
from tqdm import tqdm

import dill as pickle
import json
import os
import sys

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
    # print("Dumping graph object for future use")
    # pickle.dump(corpus_graph, open(CORPUS_GRAPH_DUMP, "wb"), protocol=pickle.HIGHEST_PROTOCOL)
else:
    print("Corpus graph already exists. Loading it....")
    corpus_graph = pickle.load(open(CORPUS_GRAPH_DUMP, "rb"))

# print(corpus_graph)
qa_graphs = pickle.load(open(sys.argv[3], "rb"))
out = sys.argv[4]


scores = {}

print("Predicting and Calculating scores")
for question_id in tqdm(qa_graphs, ascii=True):
    scores[question_id] = {}

    scores[question_id]['correct_answer'] = qa_graphs[question_id]['correct_answer']
    scores[question_id]["options"] = {}
    for key in qa_graphs[question_id]['option_graphs']:
        scores[question_id]["options"][key] = corpus_graph.compare_graph(qa_graphs[question_id]['option_graphs'][key])
        # arc = corpus_graph.compare_graph(qa_graphs[question_id]['option_graphs'][key])
        # # ncert = cg_ncert.compare_graph(qa_graphs[question_id]['option_graphs'][key])
        # scores[question_id]["options"][key] = (arc, ncert)

    # print(scores[question_id])

points = 0
p_at = defaultdict(int)
p_at[1] = 0
p_at[2] = 0
p_at[3] = 0

with open(out, "w") as f:
    for question_id in tqdm(scores, ascii=True):
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

            option_scores.sort(key=itemgetter(1, 0), reverse=True)
            ranked_answers = [each[0] for each in option_scores]
            
            for i in range(0, 3):
                if correct_answer in ranked_answers[0:i + 1]:
                    p_at[i + 1] += 1

        points += point
        f.write("%s\t%s\t%s\t%f\n" % (questions[question_id], correct_answer, str(possible_answers), point))

for key in p_at:
    p_at[key] = p_at[key] / len(scores)

print("Number of questions: ", len(scores))
print("Score: ", points / len(scores))
print("Precisoin at: ")
for key in p_at:
    print("\t%d: " % key, p_at[key])
