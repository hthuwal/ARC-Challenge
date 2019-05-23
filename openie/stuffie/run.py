import time
import pickle
from graph_stuffie import Graph, Node


def load_corpus_graphs(corpus_graph_file):
    g = Graph()
    print(f"Loading Corpus Graph from {corpus_graph_file}....")
    st = time.time()
    g.load(corpus_graph_file)
    et = time.time()
    print("Graph Loading Complete. Took %f minutes" % ((et - st) / 60))
    return g


def get_qa_graph(path):
    """
    Load the qa graph pickle dump located @PATH
    """
    print(f"Loading QA Dump @ {path}")
    return pickle.load(open(path, "rb"))


def match(string1, string2):
    return 1 if string1 == string2 else 0


def score_graphs(corpus_graph, hypothesis_graph, depth_threshold=2, match_threshold=0.5, beam_threshold=4):
    score = 0
    corpus_graph_nodes = corpus_graph.nodes
    hypo_graph_nodes = hypothesis_graph.nodes

    # Sort the hypothesis nodes in descending order of the depths of subtree
    sorted_hypo_graph_node_names = list(hypo_graph_nodes.keys())
    sorted_hypo_graph_node_names.sort(key=lambda node_name: hypothesis_graph.get_node_height(node_name), reverse=True)

    visited = {}
    for phrase in sorted_hypo_graph_node_names:

        if phrase not in visited and phrase in corpus_graph_nodes:

            hypo_nodes = [(phrase, 1)]
            cand_nodes_from_corpus = {phrase: [(phrase, 0)]}

            while len(cand_nodes_from_corpus) != 0:

                # hnode -> next node from hypothesis graph that we
                # are trying to match
                # hnd -> its position in the path from the start node
                new_hypo_nodes = []
                # import ipdb
                # ipdb.set_trace()
                for hnode, hnd in hypo_nodes:

                    # candidates -> list of (node, depth) in corpus graph with which hnode can match
                    candidates = cand_nodes_from_corpus[hnode]

                    match_scores = []
                    for cand, depth in candidates:

                        # skip those search paths which have crossed the depth threshold
                        if depth > depth_threshold:
                            continue
                        match_scores.append((cand, match(hnode, cand)))

                    winner_cand, max_score = max(match_scores, key=lambda x: x[1]) if len(match_scores) != 0 else (None, 0)

                    # found the most probable match
                    if max_score > match_threshold:
                        score += (max_score * hnd)  # the deeper match in hypothesis graph higher the score

                        visited[hnode] = True
                        del cand_nodes_from_corpus[hnode]

                        nbrs = hypo_graph_nodes[hnode].edges
                        for nbr in nbrs:
                            if nbr not in visited:
                                new_hypo_nodes.append((nbr, hnd + 1))
                                cand_nodes_from_corpus[nbr] = [(edge, 0) for edge in corpus_graph_nodes[winner_cand].edges]

                    else:
                        new_hypo_nodes.append((hnode, hnd))
                        new_candidates = {}
                        for cand, depth in candidates:
                            for new_cand in corpus_graph_nodes[cand].edges:
                                if new_cand not in new_candidates:
                                    new_candidates[new_cand] = depth + 1
                                else:
                                    new_candidates[new_cand] = min(depth + 1, new_candidates[new_cand])

                        new_candidates = list(new_candidates.items())
                        del cand_nodes_from_corpus[hnode]
                        if len(new_candidates) != 0:
                            cand_nodes_from_corpus[hnode] = new_candidates

                del hypo_nodes
                hypo_nodes = list(new_hypo_nodes)

    return score


if __name__ == "__main__":

    # Test corpus Graph
    cg = Graph()
    a = Node('a')
    b = Node('b')
    c = Node('c')
    d = Node('d')
    p = Node('p')
    q = Node('q')
    r = Node('r')
    a.add_edge(b)
    a.add_edge(c)
    c.add_edge(p)
    c.add_edge(q)
    p.add_edge(r)
    cg.nodes = {
        'a': a,
        'b': b,
        'c': c,
        'd': d,
        'p': p,
        'q': q,
        'r': r
    }

    # Test Hypo Graph
    hg = Graph()
    a = Node('a')
    d = Node('d')
    p = Node('p')
    r = Node('r')
    a.add_edge(p)
    a.add_edge(d)
    p.add_edge(r)
    hg.nodes = {
        'a': a,
        'd': d,
        'p': p,
        'r': r
    }

    print(score_graphs(cg, hg))
