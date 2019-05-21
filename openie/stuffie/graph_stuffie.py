from tqdm import tqdm
from utils_stuffie import read_stuffie_output


class Node(object):
    """Node of the Graph

    phrase: The string corresponding to this node.
    edges: List of edges that I am connected to.
    parents: Parents that are connected to me.
    """

    def __init__(self, phrase: str =None):
        self.phrase = phrase
        self.edges = []
        self.parents = []

    def __eq__(self, other):
        """
        Check if two nodes are talking about the same phrase
        """
        return self.phrase == other.phrase

    def __repr__(self):
        """
        Recursively represent the node.
        """
        repres = "--- " + self.phrase + " ---\n"
        for each in self.edges:

            edge_label = repr(each).strip().split("\n")
            edge_label = "\n".join(["\t" + line for line in edge_label])
            edge_label = "⤿" + edge_label

            repres += f"\n{edge_label}"

        repres += "\n"
        return repres

    def merge(self, other):
        """
        Merge other node with self.
        Only possible if self and other are equal.
        Just merge the edge list. Assuming there is no repetition.
        """
        assert self.phrase == other.phrase
        self.edges.extend(other.edges)

    def add_edge(self, node):
        """
        Add node to my edge list
        """

        # New Edge: Therefore Update parent list
        if node not in self.edges:
            self.edges.append(node)
            node.parents.append(self)

        # Else I was present already
        else:
            i = self.edges.index(node)
            self.edges[i].merge(node)


class Graph(object):
    def __init__(self, file=None, dpb=False):
        self.nodes = []
        self.node_indices = {}
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
        if phrase[0] == "#":
            phrase = Graph.recurse_and_find_phrase(phrase, triplets, is_facet)
            if phrase is None:
                return None
            else:
                node = Node(phrase=phrase)
        else:
            node = Node(phrase=phrase)

        if node.phrase in self.node_indices:
            return self.nodes[self.node_indices[node.phrase]]
        else:
            return node

    def update_node_list(self, new_nodes):
        for node in new_nodes:
            if node.phrase in self.node_indices:
                i = self.node_indices[node.phrase]
                self.nodes[i] = node
            else:
                self.nodes.append(node)
                self.node_indices[node.phrase] = len(self.nodes) - 1

    def build(self, infile, dpb=False):
        print("Reading Triplets...")
        all_triplets = list(tqdm(read_stuffie_output(infile), ascii=True, disable=dpb))
        print("Reading Complete...")

        for triplets in tqdm(all_triplets, ascii=True, disable=dpb):
            keys = list(triplets.keys())
            keys.sort(reverse=True)

            # Dealing with referential triples
            for key in keys:
                triplet = triplets[key]

                # Not a Facet
                if len(triplet) == 3:
                    triplet = [each.strip().lower() for each in triplet]
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
                if(len(triplets[key]) == 2):
                    j = key.rfind('.')

                    triplet = [each.strip().lower() for each in triplets[key]]
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

    def __repr__(self):
        for node in self.nodes:
            if node.parents:
                print(repr(node))

    def save(self, path):
        with open(path, "wb") as f:
            pickle.dump({'nodes': self.nodes}, f)


if __name__ == '__main__':
    g = Graph(file=sys.argv[1], dpb=False)
    print(len(g.nodes), len(set(g.nodes)))
    g.save("/tmp/test")
