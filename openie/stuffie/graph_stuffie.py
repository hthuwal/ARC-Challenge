import click
import os
import pickle
import re
import sys
import time

from multiprocessing import Pool, cpu_count
from tqdm import tqdm
from utils_stuffie import read_stuffie_output, pprint


def timeit(function):
    def inner(*args, **kwargs):
        st = time.time()
        out = function(*args, **kwargs)
        et = time.time()
        time_taken = et - st
        unit = "seconds" if time_taken <= 60 else "minutes"
        time_taken = time_taken if time_taken <= 60 else time_taken / 60
        print(f"\nTime take by {function.__name__} is {time_taken} {unit}.\n")
        return out
    return inner


class Node(object):
    """Node of the Graph

    phrase: The string corresponding to this node.
    edges: List of edges that I am connected to.
    parents: Parents that are connected to me.
    """

    def __init__(self, phrase: str =None):
        self.phrase = phrase
        self.edges: list[str] = []
        self.parents: list[str] = []

    def __hash__(self):
        return hash(self.phrase)

    def __eq__(self, other):
        """
        Check if two nodes are talking about the same phrase
        """
        return self.phrase == other.phrase

    def __repr__(self):
        return f"--- self.phrase ---, {len(self.edges)} Edges\n"

    def remove_redundant_edges(self):
        self.edges = list(set(self.edges))
        self.parents = list(set(self.parents))

    def update(self, other):
        self.edges += other.edges
        self.parents += other.parents
        self.remove_redundant_edges()

    def add_edge(self, node):
        """
        Add node to my edge list
        """
        if node.phrase not in self.edges:
            self.edges.append(node.phrase)
            node.parents.append(self.phrase)


class Graph(object):
    def __init__(self, file=None, all_triplets=None, dpb=False):
        self.nodes = {}
        if file is not None or all_triplets is not None:
            self.build(file, all_triplets=all_triplets, dpb=dpb)

    def recurse_and_find_phrase(phrase, triplets, is_facet=False):
        while phrase.startswith("#"):
            fkey = phrase[1:]
            if fkey not in triplets:
                return None
            else:
                phrase = triplets[fkey][1] if is_facet else triplets[fkey][0]

        return phrase

    def create_node(self, phrase, triplets, is_facet=False):
        if phrase[0] == "#":
            phrase = Graph.recurse_and_find_phrase(phrase, triplets, is_facet)
            if phrase is None:
                return None
        phrase = phrase.lower()
        if phrase in self.nodes:
            return self.nodes[phrase]
        else:
            return Node(phrase=phrase)

    def update_node_dict(self, new_nodes):
        for node in new_nodes:
            if node.phrase in self.nodes:
                self.nodes[node.phrase].update(node)
            else:
                self.nodes[node.phrase] = node

    def clean_triplet(strings):

        regex = re.compile(r"<ctx*.*>")

        strings = [re.sub(regex, "", each.strip().lower()) for each in strings]
        count_ = 0

        for string in strings:
            if not string:
                return False, None
            elif string == "<_>":
                count_ += 1

        if count_ > 1:
            return False, None

        return True, strings

    def build(self, infile, all_triplets=None, dpb=False):
        # print("Reading Triplets...")

        if not all_triplets:
            all_triplets = list(tqdm(read_stuffie_output(infile), ascii=True, disable=dpb))

        # print("Reading Complete...")
        for triplets in tqdm(all_triplets, ascii=True, disable=dpb):
            keys = list(triplets.keys())
            keys.sort(reverse=True)

            # Dealing with referential triples
            for key in keys:

                triplet = triplets[key]

                if len(triplet) == 3:
                    is_tangible, triplet = Graph.clean_triplet(triplet)
                    if not is_tangible:
                        continue

                    subj, pred, obj = triplet
                    subj_node = self.create_node(subj, triplets, is_facet=False)
                    pred_node = self.create_node(pred, triplets, is_facet=False)
                    obj_node = self.create_node(obj, triplets, is_facet=False)

                    if subj_node is None or obj_node is None or pred_node is None:
                        continue

                    pred_node.add_edge(obj_node)
                    subj_node.add_edge(pred_node)
                    self.update_node_dict([subj_node, pred_node, obj_node])

            # Dealing with Facets
            keys.sort()
            for key in keys:

                triplet = triplets[key]

                if(len(triplet) == 2):

                    is_tangible, triplet = Graph.clean_triplet(triplet)
                    if not is_tangible:
                        continue

                    j = key.rfind('.')
                    parent = key[:j]
                    connector, facet_phrase = triplet

                    subj_node = self.create_node(f"#{parent}", triplets, is_facet=True)
                    pred_node = self.create_node(connector, triplet, is_facet=True)
                    obj_node = self.create_node(facet_phrase, triplets, is_facet=False)

                    if subj_node is None or obj_node is None or pred_node is None:
                        continue

                    pred_node.add_edge(obj_node)
                    subj_node.add_edge(pred_node)
                    self.update_node_dict([subj_node, pred_node, obj_node])

        self.remove_redundancy()

    def remove_redundancy(self):
        for phrase in self.nodes:
            self.nodes[phrase].remove_redundant_edges()

    def save(self, path):
        with open(path, "wb") as f:
            pickle.dump(self.nodes, f)

    def load(self, path):
        with open(path, "rb") as f:
            self.nodes = pickle.load(f)

    def merge(self, other):
        for phrase in other.nodes:
            if phrase in self.nodes:
                self.nodes[phrase].edges += other.nodes[phrase].edges
                self.nodes[phrase].parents += other.nodes[phrase].parents
            else:
                self.nodes[phrase] = other.nodes[phrase]

        self.remove_redundancy()

    def __eq__(self, other):
        my_keys = sorted(list(self.nodes.keys()))
        other_keys = sorted(list(other.nodes.keys()))

        if my_keys != other_keys:
            return False

        for key in my_keys:
            if sorted(self.nodes[key].edges) != sorted(other.nodes[key].edges):
                return False
            if sorted(self.nodes[key].parents) != sorted(other.nodes[key].parents):
                return False

        return True


def create_graph(all_triplets):
    g = Graph(all_triplets=all_triplets, dpb=True)
    return g


def chunks(org_list, n):
    lp = len(org_list) // n
    for i in range(0, len(org_list), lp):
        yield org_list[i:i + lp]


@timeit
def create_graphs_multicore(all_triplets):
    num_cores = cpu_count()

    # splitting it into chunks
    data = [chunk for chunk in chunks(all_triplets, num_cores * 10)]

    # Perform operations in batches of num_cores
    for i, chunk in enumerate(chunks(data, 10)):

        print(f"Batch {i+1} {len(chunk)}")
        file_name = f"graphs/corpus_graphs/graph_part_{i+1}.graph"
        if os.path.exists(file_name):
            continue
        with Pool(processes=num_cores) as pool:
            max_ = len(chunk)
            graphs = list(pool.imap(create_graph, chunk))

            g = graphs[0]
            print("Merging graphs")
            for graph in tqdm(graphs[1:], ascii=True):
                g.merge(graph)
            g.save(file_name)

            del g, graphs


@click.command()
@click.argument('source_file', type=click.Path(exists=True))
@click.argument('target_file', type=click.Path())
def main(source_file, target_file):
    """
    Read triplets from source_file and dump graph in target_file
    """
    print("Reading Entire Data...")
    all_triplets = list(tqdm(read_stuffie_output(source_file), ascii=True, disable=False))
    print("Reading Complete...")

    create_graphs_multicore(all_triplets)

    # g_core = timeit(create_graph)(all_triplets)
    # print(f"Both graphs are equivalent: {g_multi_core == g_core}")
    # print("Saving graph for future use...")
    # g_multi_core.save(target_file)


if __name__ == '__main__':
    main()
