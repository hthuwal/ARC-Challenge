import click
import os
import pickle
import re
import sys
import time

from multiprocessing import Pool, cpu_count
from tqdm import tqdm
from utils_stuffie import read_stuffie_output, pprint, timeit


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
        return f"--- {self.phrase} ---, {len(self.edges)} Edges\n"
        # return f"--- {self.phrase} ---, Edges: {self.edges}, Parents: {self.parents}\n"

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

    def remove_edge(self, node):
        self.remove_redundant_edges()
        self.edges.remove(node.phrase)
        node.parents.remove(self.phrase)


class Graph(object):
    def __init__(self, file=None, all_triplets=None, dpb=False):
        self.nodes = {}
        self._count = 0
        if file is not None or all_triplets is not None:
            self.build(file, all_triplets=all_triplets, dpb=dpb)

    def recurse_and_find_phrase(phrase, triplets, is_facet=False):
        while phrase.startswith("#"):
            fkey = phrase[1:]
            if fkey not in triplets:
                return None
            else:
                phrase = triplets[fkey][1] if is_facet else triplets[fkey][0]
                regex = re.compile(r"<ctx*.*>")
                phrase = re.sub(regex, "", phrase.strip().lower())

        return phrase

    def create_node(self, phrase, triplets, is_facet=False):
        if phrase[0] == "#":
            phrase = Graph.recurse_and_find_phrase(phrase, triplets, is_facet)
            if phrase is None:
                return None

        if phrase == "<_>":
            self._count += 1
            phrase = f"<_{self._count}>"

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

    def collapse_(self):
        keys = list(self.nodes.keys())
        for key in keys:
            me = self.nodes[key]
            if key.startswith("<_"):
                assert len(me.parents) <= 1

                if len(me.parents) == 1:
                    parent = self.nodes[me.parents[0]]
                    parent.remove_edge(me)
                else:
                    parent = None

                for edge in me.edges:
                    nbr = self.nodes[edge]
                    me.remove_edge(nbr)
                    if parent:
                        parent.add_edge(nbr)

                del self.nodes[key]

    def add_facet_edges(self, triplets):
        keys = list(triplets.keys())
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
                pred_node = self.create_node(connector, triplets, is_facet=True)
                obj_node = self.create_node(facet_phrase, triplets, is_facet=False)

                if subj_node is None or obj_node is None or pred_node is None:
                    continue

                pred_node.add_edge(obj_node)
                subj_node.add_edge(pred_node)
                self.update_node_dict([subj_node, pred_node, obj_node])

    def add_simple_edges(self, triplets):
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

    def build(self, infile, all_triplets=None, dpb=False):

        if not all_triplets:
            all_triplets = list(tqdm(read_stuffie_output(infile), ascii=True, disable=dpb))

        for triplets in tqdm(all_triplets, ascii=True, disable=dpb):
            self.add_simple_edges(triplets)
            self.add_facet_edges(triplets)

        self.collapse_()
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

    def get_node_height(self, node):
        stack = [(node, 1)]
        depth = 0
        visited = {}
        while len(stack) != 0:
            top, top_depth = stack[-1]
            stack.pop()
            visited[top] = True
            if top_depth > depth:
                depth = top_depth
            for each in self.nodes[top].edges:
                if each not in visited:
                    stack.append((each, top_depth + 1))
        return depth

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

    def __repr__(self):
        sorted_nodes = list(self.nodes.keys())
        sorted_nodes.sort(key=lambda node_name: self.get_node_height(node_name), reverse=True)
        visited = {}
        string = ""
        for each in sorted_nodes:
            stack = []
            stack.append((each, 0))
            while stack:
                top, d = stack.pop()
                if top not in visited:
                    visited[top] = True
                    string += "\t" * d + "⤿" + repr(self.nodes[top]) + "\n"
                    for each in self.nodes[top].edges:
                        if each not in visited:
                            stack.append((each, d + 1))
        return string


def create_graph(all_triplets):
    g = Graph(all_triplets=all_triplets, dpb=True)
    return g


def chunks(org_list, n):
    lp = len(org_list) // n
    for i in range(0, len(org_list), lp):
        yield org_list[i:i + lp]


@timeit
def create_graphs_multicore(all_triplets, dump_dir):
    num_cores = cpu_count()

    # splitting it into chunks
    data = [chunk for chunk in chunks(all_triplets, num_cores * 10)]

    # Perform operations in batches of num_cores
    for i, chunk in enumerate(chunks(data, 10)):

        print(f"Batch {i+1}/10")
        file_name = f"{dump_dir}/graph_part_{i+1}.graph"
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

    print("Merging all graphs into a single graph")
    complete_graph = Graph()
    files = [file for file in os.listdir(dump_dir) if file.startswith("graph_part")]
    for file in tqdm(files, ascii=True):
        file = os.path.join(dump_dir, file)
        g = Graph()
        g.load(file)
        complete_graph.merge(g)

    complete_graph.save(os.path.join(dump_dir, "corpus_graph"))


@click.command()
@click.argument('source_file', type=click.Path(exists=True))
@click.argument('dump_dir', type=click.Path())
def main(source_file, dump_dir):
    """
    Read triplets from source_file and dump graph in dump_dir
    """
    print("Reading Entire Data...")
    all_triplets = list(tqdm(read_stuffie_output(source_file), ascii=True, disable=False))
    print("Reading Complete...")

    create_graphs_multicore(all_triplets, dump_dir)

    # g_core = timeit(create_graph)(all_triplets)
    # print(f"Both graphs are equivalent: {g_multi_core == g_core}")
    # print("Saving graph for future use...")
    # g_multi_core.save(target_file)


if __name__ == '__main__':
    main()
