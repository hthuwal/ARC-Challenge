## Corpus
### Running Stanford openIE

Assuming that [stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/) is downloaded and extracted in a folder `stanford-corenlp`.

- **Without Coreference Resolution**
    ```bash
    ./stanford-openie.sh path_to_corpus path_to_out_file
    ```

- **With coreference Resolution**

    ```bash
    ./stanford-openie.sh path_to_corpus path_to_out_file coref
    ```

The above commands will run stanford openie on the corpus and write the generated triplets to the file at `path_to_out_file.`

### Creating Corpus Graph

We'll create and save the corpus graph while evaluating questions. Refer [this](../Readme.md)

## Questions and Hypothesis

#### Install Requirements

```bash
 pip install stanfordcorenlp multiprocessing_on_dill tqdm
```

#### Run the stanford OpenIE server

```bash
java -mx4g -cp "stanford-corenlp/*" edu.stanford.nlp.pipeline.StanfordCoreNLPServer \
-port 9000 -timeout 15000
```

#### Create Hypothesis Graphs

Using the server to run openIE on each question and corresponding hypothesis and dump the corresponding graphs in `dump_file`.

- **Without Coreference Resolution**
    ```bash
    python create_graph_for_qa.py dump_file False
    ```

- **With Coreference Resolution**
    ```bash
    python create_graph_for_qa.py dump_file True
    ```