import json
import os
import sys

from tqdm import tqdm

sys.path.append("..")
import utils


def add_hypothesis(qa, tdir):
    qa = json.loads(qa.strip())

    question = qa['question']['stem'].lower()
    question = utils.replace_wh_word_with_blank(question)

    for choice in qa['question']['choices']:
        option = choice['text'].lower()
        choice['hypothesis'] = utils.create_hypothesis(question, option).lower()
        label = choice['label']

        filename = os.path.join(tdir, f"{qa['id']}_{label}")
        os.makedirs(os.path.dirname(filename), exist_ok=True)

        with open(filename, "w") as f:
            f.write(choice['hypothesis'] + "\n")

    return qa


if __name__ == '__main__':
    question_file = "../../data/ARC-V1-Feb2018-2/ARC-Challenge/ARC-Challenge-Test.jsonl"
    question_with_hypothesis = "results/questions/questions_with_hypothesis.jsonl"
    jsonl = []
    with open(question_file, "r") as inp:
        for line in tqdm(inp):
            jsonl.append(add_hypothesis(line, "results/questions/hypothesis"))

    with open(question_with_hypothesis, "w") as outp:
        outp.write("\n".join(map(str, jsonl)) + "\n")
