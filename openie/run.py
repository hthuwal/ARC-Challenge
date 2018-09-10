import json
import utils
from graph import Graph
from tqdm import tqdm
import dill as pickle

q_graphs = {}
with open("../data/ARC-V1-Feb2018-2/ARC-Challenge/ARC-Challenge-Test.jsonl", "r") as in_file:
    for line in tqdm(in_file):
        line = json.loads(line)
        question = line['question']['stem']
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

        option_graphs = {}

        for option in hypothesis:
            option_graphs[option] = utils.stanford_ie_v2(hypothesis[option])

        q_graphs[line['id']] = {'correct_answer': correct_answer, 'option_graphs': option_graphs}

print(len(q_graphs))
pickle.dump(q_graphs, open("q_graphs", "wb"))
