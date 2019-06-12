import click
import os

from graph_stuffie import Graph, Node
from tqdm import tqdm


@click.command()
@click.argument('graph_path', type=click.Path(exists=True))
@click.argument('dest_dir')
def convert(graph_path: str, dest_dir: str):
    """
    Load the graph dump from GRAPH_PATH and create appropriate files
    in DEST_DIR/graph_dump_name to be used by bidir java code.
    """
    graph_name, _ = os.path.splitext(os.path.basename(graph_path))
    dest_dir = os.path.join(dest_dir, graph_name)

    if not os.path.exists(dest_dir):
        os.makedirs(dest_dir)

    g = Graph()
    print("Loading Graph...")
    g.load(graph_path)
    print("Graph Loading Complete...")

    node_mapping = {}
    map_file = os.path.join(dest_dir, "map_of_num.tsv")
    graph_file = os.path.join(dest_dir, f"{graph_name}.tsv")

    print(f"Creating Node Num Mappings and writing them to {map_file}...")
    with open(map_file, "w") as f:
        f.write(f"0\t< -- >\n")
        node_mapping[" -- "] = 0

        for i, node_name in tqdm(enumerate(g.nodes.keys()), ascii=True, total=len(g.nodes.keys())):
            node_mapping[node_name] = i + 1
            f.write(f"{i+1}\t<{node_name}>\n")

    print(f"Converting graph into Triplets and writing them to {graph_file} ")
    with open(graph_file, "w") as f:
        for node in tqdm(g.nodes, ascii=True):
            for nbr in tqdm(g.nodes[node].edges, ascii=True):
                f.write(f"{node_mapping[node]}\t0\t{node_mapping[nbr]}\n")


if __name__ == "__main__":
    convert()
