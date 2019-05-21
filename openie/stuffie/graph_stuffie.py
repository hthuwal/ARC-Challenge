from tqdm import tqdm
from utils_stuffie import read_stuffie_output, pprint


class Node(object):
    def __init__(self, phrase: str =None):
        self.phrase = phrase
        self.edges = []
        self.parents = []

    def __eq__(self, other):
        return self.phrase == other.phrase

    def __repr__(self):
        repres = "--- " + self.phrase + " ---\n"
        for each in self.edges:

            edge_label = repr(each).strip().split("\n")
            edge_label = "\n".join(["\t" + line for line in edge_label])
            edge_label = "⤿" + edge_label

            repres += f"\n{edge_label}"

        repres += "\n"
        return repres

    def merge(self, other):
        assert self.phrase == other.phrase
        self.edges.extend(other.edges)

    def add_edge(self, edge):
        if edge not in self.edges:
            self.edges.append(edge)
            edge.parents.append(self)
        else:
            i = self.edges.index(edge)
            self.edges[i].merge(edge)


class Graph(object):
    def __init__(self, file=None, dpb=False):
        self.nodes = []
        if file is not None:
            self.build(file, dpb=dpb)

    def recurse_and_find_phrase(phrase, triplets, is_facet=False):
        while phrase.startswith("#"):
            fkey = phrase[1:]
            if fkey not in triplets:
                return None
            else:
                phrase = triplets[fkey][1] if is_facet else triplets[fkey][0]

        return phrase

    def create_node(self, phrase, triplets, is_facet=False):
        if phrase.startswith("#"):
            phrase = Graph.recurse_and_find_phrase(phrase, triplets, is_facet)
            if phrase is None:
                return None
            else:
                node = Node(phrase=phrase)
        else:
            node = Node(phrase=phrase)

        if node in self.nodes:
            return self.nodes[self.nodes.index(node)]
        else:
            return node

    def update_node_list(self, new_nodes):
        for node in new_nodes:
            if node in self.nodes:
                i = self.nodes.index(node)
                self.nodes[i] = node
            else:
                self.nodes.append(node)

    def build(self, infile, dpb=False):

        for triplets in tqdm(read_stuffie_output(infile), ascii=True, disable=dpb):
            keys = list(triplets.keys())
            keys.sort(reverse=True)

            # Dealing with referential triples
            for key in keys:
                triplet = triplets[key]
                # Not a Facet
                if len(triplet) == 3:
                    subj, pred, obj = triplet
                    subj_node = self.create_node(subj, triplets, is_facet=False)
                    pred_node = Node(phrase=pred)
                    obj_node = self.create_node(obj, triplets, is_facet=False)

                    if subj_node is None or obj_node is None:
                        continue

                    pred_node.add_edge(obj_node)
                    subj_node.add_edge(pred_node)
                    self.update_node_list([subj_node, pred_node, obj_node])

            # Dealing with Facets
            keys.sort()
            for key in keys:
                i = key.find('.')
                j = key.rfind('.')
                if i != j:
                    triplet = triplets[key]
                    parent = key[:j]
                    connector, facet_phrase = triplet

                    subj_node = self.create_node(f"#{parent}", triplets, is_facet=True)
                    pred_node = Node(phrase=connector)
                    obj_node = self.create_node(facet_phrase, triplets, is_facet=False)

                    if subj_node is None or obj_node is None:
                        continue

                    pred_node.add_edge(obj_node)
                    subj_node.add_edge(pred_node)
                    self.update_node_list([subj_node, pred_node, obj_node])

            # print("\r")
            if not dpb:
                for node in self.nodes:
                    if len(node.parents) == 0:
                        print(node)
                input()


if __name__ == '__main__':
    g = Graph(file="results/combined.txt", dpb=False)
