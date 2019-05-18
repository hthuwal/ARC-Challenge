from collections import defaultdict
from graph import Graph
from gsa import GSA
from operator import itemgetter
from tqdm import tqdm

import click
import json
import os
import sys
import time
import dill as pickle
import utils


def get_corpus_graph(corpus_triplets_file, stem):
    """
    Create Corpus Graph given its openie triplets
    in the stanford openIE format in the file "CORPUS_TRIPLETS_FILE"
    """
    if not os.path.exists("corpus_graphs"):
        os.makedirs("corpus_graphs")

    CORPUS_GRAPH_DUMP, _ = os.path.splitext(os.path.basename(corpus_triplets_file))
    CORPUS_GRAPH_DUMP = f"corpus_graphs/{CORPUS_GRAPH_DUMP}.graph"
    print(f"Graph Dump: {CORPUS_GRAPH_DUMP}")

    corpus_graph = Graph()
    if not os.path.exists(CORPUS_GRAPH_DUMP):
        print("Creating graph from corpus triples")
        corpus_graph = Graph(corpus_triplets_file, stem=stem, disable=False)
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

    return corpus_graph


def get_qa_graph(path):
    """
    Load the qa graph pickle dump located @PATH
    """
    print(f"Loading QA Dump @ {path}")
    return pickle.load(open(path, "rb"))


def get_question_details():
    questions = {}
    with open("../data/ARC-V1-Feb2018-2/ARC-Challenge/ARC-Challenge-Test.jsonl", "r") as in_file:
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
    return questions


@click.command()
@click.argument('Corpus_Triplets_File')
@click.argument("qa_graph_path", type=click.Path(exists=True))
@click.option('--stem', is_flag=True, help='Use this if you want to perform stemming on each word.')
def main(corpus_triplets_file, qa_graph_path, stem):
    """
    1. Create Corpus Graph given its openie triplets in the stanford openIE format in the file "CORPUS_TRIPLETS_FILE".\n
    2. Load the qa graph pickle dump located @PATH\n
    """
    corpus_graph = get_corpus_graph(corpus_triplets_file, stem)
    qa_graphs = get_qa_graph(qa_graph_path)


if __name__ == '__main__':
    main()
