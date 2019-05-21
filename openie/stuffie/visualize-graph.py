import sys
from graph_stuffie import Node, Graph

import networkx as nx
import matplotlib.pyplot as plt

import pydot
from networkx.drawing.nx_pydot import write_dot

print("Loading Graph...")
g = Graph()
g.load(sys.argv[1])

print("Creatig nx graph...")
G = nx.DiGraph()

print("Adding nodes to nx graph...")
G.add_nodes_from(list(g.nodes.keys()))
# print(g.nodes.keys())

print("Adding edges to nx graph...")
for node in g.nodes:
    edges = g.nodes[node].edges
    edges = [(node, other_node) for other_node in edges]
    G.add_edges_from(edges)

print("Drawing graph")
layout = nx.spring_layout(G)
nx.draw_networkx_labels(G, layout)
nx.draw_networkx_nodes(G, layout, cmap=plt.get_cmap('jet'), node_size=100)
nx.draw_networkx_edges(G, layout, edge_list=G.edges(), arrows=True)

write_dot(G, 'plot.dot')
