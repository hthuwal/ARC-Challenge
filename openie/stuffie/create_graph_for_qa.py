import click
import pickle
import json
import os
import sys

from tqdm import tqdm
from multiprocessing import Pool
from itertools import product

sys.path.append("..")
from graph_stuffie import Graph, Node


def create_graph(line, qa_triplets_path):
    line = json.loads(line.strip())
    correct_answer = line['answerKey']

    hypothesis_graphs = {}
    for choice in line['question']['choices']:
        label = choice['label']
        triplets_file = os.path.join(qa_triplets_path, f"{line['id']}_{label}.openie")
        hypothesis_graphs[label] = Graph(file=triplets_file, dpb=True)

    q_dict = {
        'correct_answer': correct_answer,
        'hypothesis_graphs': hypothesis_graphs,
    }
    return line['id'], q_dict


@click.command()
@click.argument("qa_triplets_path", type=click.Path(exists=True))
@click.argument("dump_path", type=str)
def main(qa_triplets_path, dump_path):
    """
    Read stuffIE triplets for all hypothesis from QA_TRIPLETS_PATH directory.
    Create Graph for each hypothesis and dump all the graphs @ DUMP_PATH
    """
    q_graphs = {}

    with open("../../data/ARC-V1-Feb2018-2/ARC-Challenge/ARC-Challenge-Test.jsonl", "r") as in_file:
        lines = in_file.readlines()

    with Pool(processes=32) as pool:
        max_ = len(lines)
        q_dicts = list(tqdm(pool.starmap(create_graph, product(lines, [qa_triplets_path])), ascii=True, total=max_))
        for a, b in tqdm(q_dicts, ascii=True):
            q_graphs[a] = b

    print(len(q_graphs))
    pickle.dump(q_graphs, open(dump_path, "wb"))


if __name__ == '__main__':
    main()
