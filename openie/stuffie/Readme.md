# Approach II using StuffIE

StuffIE is an open information extraction tool that extracts nested relations enriched with facets.
For example, from the following text:

> President Donald Trump announced Tuesday morning that he had fired Secretary of State Rex Tillerson and appointed CIA Director Mike Pompeo to replace him, ending months of speculation about how much longer the embattled Tillerson would last in the job.

StuffIE extracts:

```txt
1.4:  President Donald Trump; announced; Tuesday morning;
	    that; #1.10;
1.10: he; had fired; Secretary of State Rex Tillerson;
	    and; #1.17;
1.17: he; appointed; CIA Director Mike Pompeo;
	    to; #1.23;
	    <_>; #1.26;
1.23: he; replace; him;
1.26: he; ending; months of speculation;
	    about how much longer; #1.38;
1.38: the embattled Tillerson; would last in; the job;
1.1: Donald Trump; <be>; President;
1.13: Rex Tillerson; <be>; Secretary of State;
1.19: Mike Pompeo; <be>; CIA Director;
```

StuffIE uses the [Stanford CoreNLP tools](https://github.com/stanfordnlp/CoreNLP) and licensed under the GNU General Public License (v3 or later).

## Corpus

### Running StuffIE
 
1. Split the corpus into 10000 files. So that one can run stuffie parallely.

    ```bash
    split -n 10000 --numeric-suffixes "path_to_corpus_file" "parts/part_"
    ```

    This will split the corpus file into 10000 smaller files as `parts/part_0000 to parts/part_9999`

2. Run stuffie on the corpus

    - Compile and run the `StuffieConsoleRunner.java`

        ```bash
        ./run.sh
        ```

    - Once it prompts you to enter the command, enter the following
        ```bash
        <f> "dir_containing_parts" "dir_where_finished_file_will_be_moved" "Number of threads"
        ```
        For e.g
        ```bash
        <f> parts completed 20
        ```

3. Combine the results into a single file.
    
    ```bash
    mkdir results
    cd completed
    cat *.openie > ../results/combined_results.txt
    cd ..
    ```

### Creating Corpus Graph

```bash
mkdir graphs
python graph_stuffie.py results/combined_results.txt graphs
```

The above command will create `graphs/corpus_graph` dump as the corpus graph.

## QA Hypothesis

### Running StuffIE on the QA hypothesis

1. Generate hypothesis and create seperate files for each of them.
    
    ```bash
    mkdir -p results/questions/hypothesis
    python prepare-hypothesis-for-stuffie.py "path_to_qa_jsonl_file"
    ```
    This will create files `qid_optionlabel` for each question-option pair in the directory `results/questions/hypothesis`

2. Run StuffIE on the QA. Proceed similar to corpus

    ```bash
    ./run.sh 
    <f> results/questions/hypothesis results/questions/sutffie_hypothesis 20
    ```

### Creating Hypothesis Graphs

Following command will read stuffIE triplets for all hypothesis from QA_TRIPLETS_PATH directory. Create Graph for each hypothesis and dump all the graphs @ DUMP_FILE

```bash
python create_graph_for_qa.py QA_TRIPLETS_PATH DUMP_FILE
```
For e.g.
```bash
python create_graph_for_qa.py results/questions/sutffie_hypothesis graphs/qa_graphs
```

## Approach I

- Convert stuffIE output into stanford format and use the Approach I.

    ```bash
    python convert_into_stanford_format.py
    ```

    This will create the file `results/triplets_in_stanford_format.txt` for the corpus and multiple files in directory `results/questions/stuffie_hypothesis_stanford_format` for each hypothesis.

## Approach II

To score the qa_graphs and hypothesis graphs run the command.

```bash
python run.py "CORPUS_TRIPLETS_GRAPH" "QA_GRAPH_PATH" "PREDICTION_FILE"
```

You'll be prompted to enter the values of the hyperparameters (refere [thesis]()).