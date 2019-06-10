import pickle
import sys
import unittest

from tqdm import tqdm

sys.path.append("..")
from graph_stuffie import Graph, Node

graphs_path = sys.argv[1]
sys.argv.pop()  # No cli arguments should be passed to unitest


print("Loading graph")
qags = pickle.load(open(graphs_path, "rb"))
print("Graph Loading complete\n")


class TestCorpusGraph(unittest.TestCase):

    def shouldnt_be_present(self, keyword):
        for question in tqdm(qags, ascii=True, disable=True):
            graphs = qags[question]['hypothesis_graphs']
            for option in graphs:
                graph = graphs[option]
                for each in graph.nodes:
                    msg = f"{keyword} found in %s: %s"
                    self.assertNotIn(keyword, each, msg % ("Node", each))

                    for edge in graph.nodes[each].edges:
                        self.assertNotIn(keyword, each, msg % ("edge", f"{each} -> {edge}"))

                    for parent in graph.nodes[each].parents:
                        self.assertNotIn(keyword, each, msg % ("parent", f"{parent} <- {each}"))

    def test_ctx(self):
        self.shouldnt_be_present("<ctx")

    def test_collapse_(self):
        self.shouldnt_be_present("<_")

    def test_consistency(self):
        for question in tqdm(qags, ascii=True, disable=True):
            graphs = qags[question]['hypothesis_graphs']
            for option in graphs:
                graph = graphs[option]
                for key in graph.nodes:
                    node = graph.nodes[key]
                    for edge in node.edges:
                        descendent = graph.nodes[edge]
                        self.assertIn(
                            node.phrase,
                            descendent.parents,
                            "\nFor edge {key} -> {edge}, {key} is not present in {edge}\'s parent list"
                        )

                    for parent in node.parents:
                        ancestor = graph.nodes[parent]
                        self.assertIn(
                            node.phrase,
                            ancestor.edges,
                            "\nEdge {parent} -> {key} is not present but {parent} is present in {key}\'s parent list"
                        )


if __name__ == '__main__':
    unittest.main()
