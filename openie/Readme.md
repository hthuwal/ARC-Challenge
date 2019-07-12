**Assumption: The data is already downloaded using the steps mentioned in the [Readme](../Readme.md) of the root directory.**

## Approach I

Run the stanford openie on the corpus and QA Hypothesis. Follow the steps mentioned [here](stanford/Readme.md). You should have two dumps after this

1. `path_to_corpus_triplets`: Stanford OpenIE triplets of the entire corpus.
2. `path_to_qa_hypo_graph_dump`: Dump of hypothesis graphs.

Run the following command to create the corpus graph and use Approach I to answer these questions to save the predictions in `path_to_pred_file`.

**Note**: The corpus graph is saved for future use.

```bash
python run.py path_to_corpus_triplets path_to_qa_hypo_graph_dump \
    path_to_pred_file
```

Using the official evaluation script by ARC

```bash
python ../evaluator.py \
    -qa ../data/ARC-V1-Feb2018-2/ARC-Challenge/ARC-Challenge-Test.jsonl \
    -p path_to_pred_file \
    -o output_file
```