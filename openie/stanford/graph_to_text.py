# script to convert hypothesis graphs into understandable text files
import json
import utils
import sys
from graph import Graph
from tqdm import tqdm
import dill as pickle

qa_graphs = pickle.load(open(sys.argv[1], "rb"))
out_file = sys.argv[2]
seperator = "#" * 79


def graph_to_triplets(g):
    triplets = []
    for node in g.adj:
        for nbr in g.adj[node]:
            for label in g.adj[node][nbr]:
                if not label.startswith('rev'):
                    triplets.append((node, label, nbr))
    return triplets


questions = {}
with open("../../data/ARC-V1-Feb2018-2/ARC-Challenge/ARC-Challenge-Test.jsonl", "r") as in_file, open(out_file, "w") as out:
    for line in in_file:
        line = json.loads(line)
        qid = line['id']
        question = line['question']['stem']
        org_ques = question
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

        print(seperator)
        print(" " * 28, qid)
        print(seperator)
        print("\nQuestion: ", org_ques)

        row = qid + "\t"
        row += org_ques + "\t"
        triplets = {key: graph_to_triplets(value) for key, value in qa_graphs[qid]['hypothesis_graphs'].items()}
        trips = {}

        count = 0
        for key in hypothesis:
            row += options[key] + "\t"
            print("\n", key, ":", options[key], "\n")
            print("Hypothesis-%s:" % key, hypothesis[key], "\n")
            row += hypothesis[key] + "\t"
            print("Extracted triplets: subj || pred || obj")
            trips = ""
            for subj, pred, obj in triplets[key]:
                print(subj, "---", pred, "-->", obj)
                trips += ("%s --- %s --> %s || " % (subj, pred, obj))
            row += trips + "\t"
            print("")
            count += 1

        for i in range(5 - count):
            row += "NA\tNA\tNA\t"

        out.write(row + "\n")
