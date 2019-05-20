import dill as pickle
import click

from collections import defaultdict


def get_qa_graph(path):
    """
    Load the qa graph pickle dump located @PATH
    """
    print(f"Loading QA Dump @ {path}")
    return pickle.load(open(path, "rb"))


def analyze(qa_graphs):
    count = 0
    size = defaultdict(int)
    for qid in qa_graphs:
        hypo_graphs = qa_graphs[qid]['hypothesis_graphs']
        for option in hypo_graphs:
            hypo_graph = hypo_graphs[option]
            size[hypo_graph.num_nodes()] += 1

            if hypo_graph.num_nodes() == 0 and qa_graphs[qid]['correct_answer'].lower() == option.lower():
                count += 1

    print(f"Number of Questions where there is empty graph corresponding to correct hypothesis is: {count}")
    size = list(size.items())
    size.sort(key=lambda x: x[0])
    print(size)


@click.command()
@click.argument("qa_graph_path", type=click.Path(exists=True))
def main(qa_graph_path):
    qa_graphs = get_qa_graph(qa_graph_path)
    analyze(qa_graphs)


if __name__ == '__main__':
    main()
