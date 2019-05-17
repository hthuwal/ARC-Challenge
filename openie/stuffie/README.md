# StuffIE

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

## Running StuffIE on the corpus

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
    cd completed
    cat *.openie > combined_results.txt
    ```

## Running StuffIE on the QA hypothesis

1. Generate hypothesis and create seperate files for each of them.
    
    ```bash
    mkdir -P results/questions/hypothesis
    python prepare-hypothesis-for-stuffie.py "path_to_qa_jsonl_file"
    ```
    - This will create files `qid_optionlabel` for each question-option pair in the directory `results/questions/hypothesis`

2. Run StuffIE on the QA. Proceed similar to corpus

    ```bash
    ./run.sh 
    <f> results/questions/hypothesis results/questions/sutffie_hypothesis 20
    ```

