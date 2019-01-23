from collections import defaultdict
from graph import Graph
from operator import itemgetter
from tqdm import tqdm

import json
import os
import sys
import time
import dill as pickle
import utils

CORPUS_GRAPH_DUMP, _ = os.path.splitext(sys.argv[1])
CORPUS_GRAPH_DUMP += ".graph"

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

stem = sys.argv[2]
stem = True if stem == "True" else False
print(stem)

corpus_graph = Graph()

if not os.path.exists(CORPUS_GRAPH_DUMP):
    print("Creating graph from corpus triples")
    corpus_graph = Graph(sys.argv[1], stem=stem, disable=False)
    print("Dumping graph object for future use")
    st = time.time()
    corpus_graph.save(CORPUS_GRAPH_DUMP)
    et = time.time()
    print("Graph Dumping Complete. Took %f minutes" % ((et - st) / 60))
else:
    print("Corpus graph already exists. Loading it....")
    st = time.time()
    corpus_graph.load(CORPUS_GRAPH_DUMP)
    et = time.time()
    print("Graph Loading Complete. Took %f minutes" % ((et - st) / 60))

qa_graphs = pickle.load(open(sys.argv[3], "rb"))
out = sys.argv[4]
if len(sys.argv) == 6:
    dumps = sys.argv[5]
else:
    dumps = None

scores = {}

print("Predicting and Calculating scores")
for question_id in tqdm(qa_graphs, ascii=True):
    scores[question_id] = {}
    scores[question_id]['correct_answer'] = qa_graphs[question_id]['correct_answer']
    scores[question_id]["options"] = {}
    matches = {}
    for key in qa_graphs[question_id]['option_graphs']:
        scores[question_id]["options"][key], match = corpus_graph.compare_graph(qa_graphs[question_id]['option_graphs'][key])
        matches[key] = {}
        matches[key]['graph'] = match
        matches[key]['hypothesis'] = questions[question_id][1][key]
        matches[key]['option'] = questions[question_id][2][key]
    matches["question"] = questions[question_id][0]

    if dumps is not None:
        json.dump(matches, open(os.path.join(dumps, question_id + ".json"), "w"), indent=4)

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
        f.write("%s\t%s\t%s\t%s\t%f\n" % (question_id, questions[question_id], correct_answer, str(possible_answers), point))

for key in p_at:
    p_at[key] = p_at[key] / len(scores)

print("Number of questions: ", len(scores))
print("Score: ", points / len(scores))
print("Precisoin at: ")
for key in p_at:
    print("\t%d: " % key, p_at[key])
print("Exiting...")
