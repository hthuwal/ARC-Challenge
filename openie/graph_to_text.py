# script to convert hypothesis graphs into understandable text files
import json
import utils
import sys
from graph import Graph
from tqdm import tqdm
import dill as pickle

qa_graphs = pickle.load(open(sys.argv[1], "rb"))


def graph_to_triplets(g):
    triplets = []
    for node in g.adj:
        for nbr in g.adj[node]:
            for label in g.adj[node][nbr]:
                if not label.startswith('rev'):
                    triplets.append((node, label, nbr))
    return triplets


with open("../data/ARC-V1-Feb2018-2/ARC-Challenge/ARC-Challenge-Test.jsonl", "r") as in_file, open("out.tsv", "w") as out:
    for line in in_file:
        line = json.loads(line)
        qid = line['id']
        question = line['question']['stem']
        org_ques = question
        correct_answer = line['answerKey']
        options_text = {}

        for choice in line['question']['choices']:
            label = choice['label']
            if label not in options_text:
                options_text[label] = choice['text']

        options = {}
        options['A'] = options_text['A'] if 'A' in options_text else ""
        options['B'] = options_text['B'] if 'B' in options_text else ""
        options['C'] = options_text['C'] if 'C' in options_text else ""
        options['D'] = options_text['D'] if 'D' in options_text else ""
        options['E'] = options_text['E'] if 'E' in options_text else ""

        hypothesis = {}
        question = utils.replace_wh_word_with_blank(question)

        for option in options:
            if options[option] != "":
                hypothesis[option] = utils.create_hypothesis(question, options[option])

        print("\nQuestion: ", org_ques)
        triplets = {key: graph_to_triplets(value) for key, value in qa_graphs[qid]['option_graphs'].items()}
        trips = {}
        row = org_ques + "\t"
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
        out.write(row + "\n")
        # input()
