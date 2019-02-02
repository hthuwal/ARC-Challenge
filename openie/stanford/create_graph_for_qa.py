import json
import utils
from tqdm import tqdm
from multiprocessing_on_dill import Pool
import sys
import dill as pickle
q_graphs = {}
out = sys.argv[1]
coref = sys.argv[2]
coref = True if coref == "True" else False


def create_graph(line):
    line = json.loads(line.strip())
    question = line['question']['stem'].lower()
    correct_answer = line['answerKey']
    options = {}

    for choice in line['question']['choices']:
        label = choice['label']
        if label not in options:
            options[label] = choice['text'].lower()

    hypothesis = {}
    question = utils.replace_wh_word_with_blank(question)

    for option in options:
        if options[option] != "":
            hypothesis[option] = utils.create_hypothesis(question, options[option]).lower()

    hypothesis_graphs = {}
    option_graphs = {}

    for option in hypothesis:
        option_graphs[option] = utils.stanford_ie_v2(options[option], coref)
        hypothesis_graphs[option] = utils.stanford_ie_v2(hypothesis[option], coref)

    q_dict = {
        'correct_answer': correct_answer,
        'hypothesis_graphs': hypothesis_graphs,
        'option_graphs': option_graphs
    }

    return line['id'], q_dict


with open("../../data/ARC-V1-Feb2018-2/ARC-Challenge/ARC-Challenge-Test.jsonl", "r") as in_file:
    lines = in_file.readlines()

    with Pool(processes=32) as pool:
        max_ = len(lines)
        q_dicts = list(tqdm(pool.imap_unordered(create_graph, lines), ascii=True, total=max_))
        for a, b in tqdm(q_dicts, ascii=True):
            q_graphs[a] = b

print(len(q_graphs))
pickle.dump(q_graphs, open(out, "wb"))
