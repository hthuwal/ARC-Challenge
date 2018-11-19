## Frequently Accessed Links
- [MTP_Results before OpenIE](https://docs.google.com/spreadsheets/d/151zuO4OEE7Z1zyyDnMPC5DXp-aeJ31ROvm_7-edUVa8/edit#gid=1975852286)
- [MTP_OpenIE](https://docs.google.com/spreadsheets/d/1BgyFyzLrojTdp14Msg0WW2u-FU9rdBp6-hvgmqye8Ac/edit#gid=0)
- [Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/)
- [ARC Dataset Description](http://data.allenai.org/arc/arc-corpus/)
- [ARC Leader board](https://leaderboard.allenai.org/arc)
- [WebChild](https://www.mpi-inf.mpg.de/departments/databases-and-information-systems/research/yago-naga/webchild/)
- [Links to Open IE triplets](https://owncloud.iitd.ac.in/owncloud/index.php/s/M8dLaNwBWDWyrPq/download)
    + OpenIE on Questions
        * You might find this cleaner than the sheet.
    + OpenIE on NCERT
    + OpenIE + coref on NCERT
    + OpenIE on ARC
        * 3GB file. Can't add this to the Google sheets
    + openIE + coref on ARC

---
### 5th November 

#### Classification of Questions

- The folks in [this paper](https://arxiv.org/pdf/1806.00358.pdf) say that they will release the annotations/classifications of the questions.
- Haven't been able to find a link to the resource yet.
- Have Mailed the authors of the paper regarding the same.
- Meanwhile trying to annotate the questions myself by manually searching the corpus corresponding to each question.

#### [New SOTA Paper: Reading Strategies](https://arxiv.org/pdf/1810.13441v1.pdf)

- 42.32%.
- Strategies + [OFT](https://s3-us-west-2.amazonaws.com/openai-assets/research-covers/language-unsupervised/language_understanding_paper.pdf) (Fine Tuned Transformer Network)
- Stategies
    + Back and Forth Reading: Train two OFT networks one reading forward and backward.
    + Highlight:
        * [Special Embeddings for Noun and adjectives.](https://s3-us-west-2.amazonaws.com/openai-assets/research-covers/language-unsupervised/language_understanding_paper.pdf)
    + Self-Assessment:
        * Generate Questions and Answers and practice on them.
- [Tranformer Network](https://arxiv.org/abs/1706.03762)

#### Problem of compute resources

##### Problems with HPC

- HPC cpu/gpu is a timesharing system.
- Submitted jobs are allocated compute resources on a time sharing bases. This increases the running time of the processes.
    + Time Taken by the code to read the stanford openie triplets, create graph and answer question
        * AryaBhatta Server: ~20/25 Minutes
        * HPC (Running on standard Queue)
            - 1cpu node with 4 cores
            - Time for which my process ran: 50 Minutes
            - Real time: ~3 hours.
- Tried Running the Mausam's openIE on HPC as a job. 
- HPC kills the process after exceeds the wall time. Maximum wall time is of 7 days. So no point in running it.
- Processes get killed randomly. (Probably if load is high).

##### Current state of Aryabhatta server

![](http://www.cse.iitd.ac.in/~mcs172074/mtp/arya_usage.png)

- Completely Overwhelmed.
- Can't use it at all.
- 128 Gigs of RAM and 128 Gigs of swap storage are completely filled.
- I am using only ~18 Gigs of RAM.
- There seems to be a lot of process by a user named **exxonmobi**

#### HPC setup

- Familiarized myself with the **qsub** based job submission design of the HPC.
- Existing code base was in python3 and so were the model and graph dumps.
- The python3 modules provided by the HPC have broken sqlite installation. This cuses all the `nltk` imports to throw an error.
- Tried porting code to python2 but would have to run all the stanford openie stuff again as the graph dumps by `dill` of python3 are not backward compatible.
- Threw away all their modules. Setup anaconda python.

---

### 23rd October

Next immediate step is to download and study the ConceptNet knowledge base and use it in place of and alongside the knowledge graph created over ARC.

---

#### New Additions to the Leader Board

- [Improving Question Answering by Commonsense-Based Pre-Training](https://arxiv.org/pdf/1809.03568.pdf)
    - Score: 33.39
    - Have Used **ConceptNet** alongside TriAN model
    - [**ConceptNet**](http://conceptnet.io/)
        + A semantic network or multilingual knowledge base
        + Knowledge is the accumulation of knowledge from
            * Crowd sourced resources: Wiktionary and openMind
            * Expert Created resources: WordNet and JDict
        + Available in both neural (embeddings) and traditional (node, edge) formats.
    - They've shown that the embeddings learned over ConceptNet can somewhat remove the limitations of the SOTA Neural approaches which lack commonsense knowledge.

- [Sanity Check](https://arxiv.org/pdf/1807.01836.pdf)
    + Score: 26.56
    + Claim: True gain of Neural Network approaches is inflated with respect to the long training time because they are not compared to proper baseline.
    + They are that proper baseline.
    + Better Scores on other datasets. but no significant effect in case of ARC.

---

#### Calculating Precision @ 1,2,3

- Precision @ 1: How often is the highest ranked option is correct.
- Precision @ 2: How often is any of the top two ranked option is correct.
- Ranking is defined by the score obtained by the graph comparison algorithm
    + In case of same score. Sort Lexicographically.

**Results**

- **Without Co-reference Resolution**

    Model's are Running will update result by tomorrow.

    | Method | Score | Precision @ 1 |  Precision @ 2 |  Precision @ 3 |
    |--------|:-----:|:-------------:|:--------------:|:--------------:|
    |ARC + Naive| 27.41|0.2628|0.50256|0.7645|
    |ARC + J2| 27.6834|0.2662|0.5060|0.7636|
    |ARC + J1 + J2| 27.868|0.2705|0.5102|0.7705|
    |ARC + E| 28.124|0.2730|0.5179|0.7670|
    |ARC + E + J1| 28.252|0.2756|0.5085|0.7730|
    |ARC + J1| 28.380|0.2747|0.5119|0.7696|

- **With Co-reference Resolution**

    | Method | Score | Precision @ 1 |  Precision @ 2 |  Precision @ 3 |
    |--------|:-----:|:-------------:|:--------------:|:--------------:|
    |ARC + coref + E + J1| 27.569 | 0.2677 | 0.5085 | 0.7662 |
    |ARC + coref + J1 + J2| 27.7617 | 0.2687 | 0.5059 | 0.7687 |
    |ARC + coref + Naive| 27.8256 |0.2671|0.5043|0.7654|
    |ARC + coref + J2| 27.9109 | 0.2688 | 0.5051 | 0.7635 |
    |ARC + coref + J1| 27.9892 | 0.2705 | 0.5051 | 0.7679 |
    |ARC + coref + E| 28.2949 | 0.2739 | 0.5111 | 0.7679 |

    -  As expected the increase in accuracy is due to the increase in Precision @ 1.

--- 

#### Improving edge label comparison 

Scoring two edges with different labels between same nodes. All scores are on the ARC dataset using the Stanford openIE.

- Simple: Fraction of words that match
    + Without Co-reference Resolution: 27.41
    + With Co-reference Resolution: 27.8256

- ~~Edit Distance is the score~~ 
    + Without Co-reference Resolution: 26.75
    + Mistake: Wrong metric. Large distance should mean less score

- 1 - Normalized edit distance (E)
    + Without Co-reference Resolution: 28.124288
    + With Co-reference Resolution: 28.2949

- Jaccard Similarity 1 (J1)
    + Each edge label converted to set of characters
    + Without Co-reference Resolution: 28.38026
    + With Co-reference Resolution: 27.9892

- Jaccard Similarity 2 (J2)
    + Each edge label converted to set of words
    + Without Co-reference Resolution: 27.68344
    + With Co-reference Resolution: 27.9109

- J1 + J2
    + Without Co-reference Resolution: 27.868
    + With Co-reference Resolution: 27.762
    + This I was sure would increase the score but it didn't.
    
- E + J1
    + Without Co-reference Resolution: 28.252
    + With Co-reference Resolution: 27.569

Doing Co-reference Resolution seems to have a negative impact on the total score except for the naive edge comparison and Edit distance approach.

~~TODO: Check if same behaviour is observed in Precision @ 1~~

#### Trying python multi core

Converted the preprocessing code to multi thread to reduce time.

+ Without Multi-Processing: 9 minutes
+ With Multi-Processing: 40 Minutes !! (Increased I/O time)

---

### 10th October 

**Mausam's OpenIE**

+ The processes are still running. Its been 127 Hours.
+ 114G/126G Memory Usage.
![](http://www.cse.iitd.ac.in/~mcs172074/mtp/usage.png)

**Better string Comparison**

- Have Written code to compare graph edges and nodes based on various string comparison Algorithms (Edit Distance, Jaccard Distance).
- Unable to run the code as the arya server is already overwhelmed.
- 16 GB RAM of the DAIR machine is not sufficient.
- Debugging code, looking for memory leaks. Trying to do memory optimizations.
- **Should I request a baadal VM?**

---
#### No MultiCore Support Mausam's openIE

- Created an Issue on their repository.
- The owner of the repo didn't reply regarding multicore support. Another person was facing the same issue and he couldn't find any inbuilt multicore support either.
- Writing a script to split the data into multiple files and run 32 seperate processes.
- Cannot Run more than 3 parallel instances as each process requires 10GB to jus be loaded. 
- During processing the memory usage exceeds well over 70 GB for just 3 processes.
- NCERT:
    + Time: 30 minutes

#### The owner of the repo replied

- He gave a solution: Passing **a flag** `--ignore-errors` should apparently solve this issue. ~~Will try that tomorrow.~~
- Mausam's openIE on NCERT
    + Time: 63 minutes
- ** Output Format is different than the stanfords. Will write a script to convert it.**
- Along with confidence, it returns a Context sometimes. Can we use it somehow?
- Mausam's openIE on ARC dataset
    + Was Running on single core :/
    + Its been 28 hours its still running.
    + Need to find a way to parallelize this.
- Created an Issue regarding this on their repository.
- If no inbuilt support might try running it 32 times on 32 splits of the file.

#### Solving Issue with Mausam's OpenIE

- I ran several sample sentence on their model to figure out a way to run it.
- I found the following pattern.
    + The model ran into an exception only when the input sentences does not have a terminating punctuation mark i.e `. ? !`
    + So for all such sentences I explicitly placed a Full stop at the end.
- **The Java process only uses a single core.!!!**
- Ran on ncert dataset. After running for an hour got a new exception.

```
Exception in thread "main" java.util.regex.PatternSyntaxException: Dangling meta character '+' near index 0 
```

---

#### Issues with Mausam's OpenIE
- Fails on sentences with only one word.
    + e.g. `Harish.`
    + Java Null Pointer Exception
    + Removed all single word sentences from the corpus to solve this.
- Fails on some sentences with no binary relation of the form relation(subject, object)
    + e.g. `Chemical Reactions and Equations`
        * Java Null Pointer Exception
    + e.g. `Barack Obama, The U.S. President`
        * 0.88 (Barack Obama; `[is]` The President `[of]`; United States)
- ~~Because of these "weird errors" Unable to run it on any corpus. After running for several hours the Java process core dumps and raises a Null Pointer Exception.~~
- I am using the compiled Java file provided in their repository.
- I can look into these issues by digging in their Scala code (I have no experience in Scala). But that would deviate the effort to debugging their openIE.

**I've created an issue on their GitHub Repository regarding the same.**


---

- Performing Co-reference Resolution on the Questions seems to have no effect on the final scores.

Trying to combine NCERT and ARC corpus graphs:

|Approach| Score | 
|---------|-------|
|Score per question = ARC score + NCERT score|25.7565|
|Score per question = **Sum** of **normalized** score|25.40|
|Score per question = **max**(ARC score + NCERT score)|27.199|

---

## 25th Sep 2018

- Doing Coref resolution on the ARC dataset increased the accuracy by ~0.42 percent.
- Using both the NCERT and ARC corpus graph to score the hypothesis graph resulted in lower overall score than using just the ARC corpus. Why?

To-do/In Progress:
- Manual Analysis of the zero score questions.
- Running Mausam's OpenIE on the ARC Corpus.
- Improve the scoring/graph creation algorithm.

#### Updated Results

|Corpus/Method|Points Scored|
|-------------|:-----------:|
|ARC + DGEM with openIE (open-source) | 26.41|
|ARC + DGEM with openIE (proprietary) | 27.11|
|ARC + Graph Comparison Algo| 27.41|
|ARC + Coref + Graph Comparison Algo | 27.82|

The scores per question have been added to the spreadsheet.

---

#### StanfordNLP openIE with coref resolution
- Took 62.8 Hours to complete
- 2.99 GB files of triplets
- Creating Graph took around 20 minutes.

Graph Details:

- Analysis of the resultant graph:
    - Number of Nodes: 22123223             
    - Number of Edges: 41976390             

|Nodes|Number_of_components|
|:---:|:------------------:|
|21236195|1|
|100-1004| 87|
|21-100|1006|
|10-20|3508|
|1-9|312931|

---

## 20th Sep 2018

#### Analysis of the results

- The overall results seem to improve.

|Score|NCERT(DGEM)|NCERT(OpenIE)|NCERT(OpenIE + Coref)|ARC(DGEM)|ARC (OpenIE)|
|:---:|:---------:|:-----------:|:-------------------:|:-------:|:----------:|
|Total_Score|    22.98| 23.929| 24.017| 26.41| 27.413|

- The model is avoiding 0 scores by not predicting at all or predicting k-way ties (thereby getting 1/k score).
- The next aim should be to increase the percentage of correctly answered questions ( single prediction)
    + Do question by question analysis.
    + Try changing the scoring function.
    + Incorporate the confidence score returned by openIE into scoring function? (Probably the edge labels?)

![](http://www.cse.iitd.ac.in/~mcs172074/mtp/openie_analysis.png)


|Score|NCERT(DGEM)|NCERT(OpenIE)|NCERT(OpenIE + Coref)|ARC(DGEM)|ARC (OpenIE)|
|:---:|:---------:|:-----------:|:-------------------:|:-------:|:----------:|
|0.00 |    72.18|   45.56|   40.27|   69.28|   45.82|
|0.25 |    4.69|    30.63|   38.31|   4.52|    26.71|
|0.33 |    0.68|    5.63|    5.20|    0.34|    4.44|
|0.50 |    1.71|    7.42|    6.74|    1.37|    7.42|
|1.00 |    20.73|   10.67|   9.30|    24.49|   15.53|

---

#### Mausams openIE

- ~~Difficulty setting up the build environment as don't have administrator access on the aryabhatta server~~
- Build is complete. 
- Will run it after the StanfordNLP completes.

--- 

#### StanfordNLP openIE with coref on ARC

- Still running!

---

#### Results

|Corpus/Method|Points Scored|
|-------------|:-----------:|
|ARC + DGEM with openIE (opensource) | 26.41|
|ARC + DGEM with openIE (proprietary) | 27.11|
|ARC + Graph Comparison Algo| 27.41|

The scores per question have been added to the spreadsheet.

---

#### StanfordNLP openIE over the ARC Dataset
- Split the 1.3GB ARC corpus into 10kb files.
- Core dumped after running for 28 hours.
- Split the 1.3GB ARC corpus into 1kb files.
- 2.91GB file of triplets
- Took 11.39 hours.
- Creating Graph took around ~15 minutes.
- Unable to dump the corpus graph at the moment, due to memory issues.

Graph Details:

- Analysis of the resultant graph:
    - Number of Nodes: 21317833             
    - Number of Edges: 40606342             

|Nodes   |Number of components|
|:------:|:------------------:|
|20605758    |1|
|1004    |1|
|1002    |4|
|1001    |2|
|1000    |1|
|100-999 |74|
|20-99   |840|
|19  |108|
|10-19   |2639|
|1-9 |254438|

---

#### StanfordNLP vs openIE(mausam)

- StanfordNLP does n-ary extraction (to some extent).
- openIE perfoms better for some example.
- openIE core dumps (exception) on single word sentences.
- Will need to pre-process data before running openIE on entire corpus.

##### Sentence: Earth.

- **Stanford OpenIE:**
    + No tuples extracted.
- **Stanford OpenIE with coreference resolution:**
    + No tuples extracted.
- **OpenIE**
    + Raises Exception (and shuts down the entire process.)

##### Sentence: The U.S. president Barack Obama gave his speech on Tuesday and Wednesday to thousands of people.

In this case openIE performs bettter as it is able to extract time information for the relation **gave(subject, object)**

- **Stanford OpenIE:**
    + 1.000: (U.S. president Barack Obama; gave; his speech)
- **Stanford OpenIE with coreference resolution:**
    + 1.000: (Barack Obama; is; president)
    + 1.000: (Barack Obama; is; U.S.)
    + 1.000: (U.S. president Barack Obama; gave; U.S. president Barack Obama speech)
- **OpenIE**
    + 0.92 (The U.S. president Barack Obama; gave; his speech; `T`:on Wednesday; to thousands of people)
    + 0.38 (Barack Obama; `[is]` president `[of]`; United States)
    + 0.92 (The U.S. president Barack Obama; gave; his speech; `T`:on Tuesday; to thousands of people)

##### Sentence: Jack and Jill visited India, Japan and South Korea.

Both the StanfordNLP and the openIE perform similar on this sentence. **Both are able to do n-ary extraction**

- **Stanford OpenIE**
    + 1.000: (Jill; visited; South Korea)
    + 1.000: (Jack; visited; India)
    + 1.000: (Jill; visited; India)
    + 1.000: (Jill; visited; Japan)
    + 1.000: (Jack; visited; Japan)
    + 1.000: (Jack; visited; South Korea)
- **Stanford OpenIE with coreference resolution**
    + Same as Stanford OpenIE
- **OpenIE**
    + 0.88 (Jill; visited; South Korea)
    + 0.88 (Jill; visited; Japan)
    + 0.88 (Jill; visited; India)
    + 0.88 (Jack; visited; South Korea)
    + 0.88 (Jack; visited; Japan)
    + 0.88 (Jack; visited; India
---

- Added the triplets generated by the openIE in the [spreadsheet](https://docs.google.com/spreadsheets/d/1BgyFyzLrojTdp14Msg0WW2u-FU9rdBp6-hvgmqye8Ac/edit#gid=0).
- Moved all the openIE results to this new spreadsheet (google sheets was raising a warning that number of cells in the sheet is exceeding a threshold)
- Link to the text files:
	+ [OpenIE on Questions](http://www.cse.iitd.ac.in/~mcs172074/mtp/openie_questions.txt)
        * You might find this cleaner than the sheet.
    + [OpenIE on NCERT](http://www.cse.iitd.ac.in/~mcs172074/mtp/stanford-openie-ncert.txt)
    + [OpenIE + coref on NCERT](http://www.cse.iitd.ac.in/~mcs172074/mtp/stanford-openie-ncert-coref.txt).

## 11th Sep 2018
#### Misc

- There are 1119 Challenge questions provided for training the models. We haven't used them in any way (Since we didn't got to designing/training any neural models).
- Mausam's OpenIE [does not support coreference resolution](https://github.com/dair-iitd/OpenIE-standalone/issues/20). It acts on each sentence independently.
- StanfordCoreNLP openIE does.
- Two new additions to the leader board of ARC Challenge: (Will go through these papers next)
	+ ET-RR [Ni et al. 2018](https://nijianmo.github.io/paper/msr-2018.pdf)
		* Claimed Score: 36.36 
		* Initial Impressions: Seems to have some complex multilayer neural architecture.
		* By: Microsoft Business Applications Group AI Research and University of California San Diego
	+ BiLSTM Max-out [Mihaylov et al. 2018](https://github.com/allenai/ARC-Solvers/blob/master/arc_solvers/models/qa/README.md#bilstm-max-out-with-question-to-choices-max-attention)
		* Claimed Score: 33.87
		* Initial impressions: Much Simpler than ET-PR
		* By: Allen Institute for AI and University of Heidelberg
		* Going to be published in 2018 EMNLP.

#### Results: Question wise predictions are present in the spread sheet
|Corpus/Method|Points Scored|
|-------------|:-----------:|
|NCERT + DGEM |22.99|
|NCERT openIE + Graph Comparison Algo| 23.929|
|NCERT openIE (with coreference resolution) + Graph Comparison Algo| 24.017|

Results though improved are still below the random baseline.
- Can try different graph comparison algo, with partial word matching?
- Better coreference resolution with bigger file sizes?

#### Predicting for each question
- Get score for each hypothesis
- Predict the options with maximum scores
- Points = (1/number of predictions)

#### Algorithm Used for scoring hypothesis graph
- Corpus Graph (C<sub>g</sub>)
- Hypothesis Graph (H<sub>g</sub>)
- The score per (H<sub>g</sub>) consists of:
	+ Fraction of nodes of H<sub>g</sub> that are present in C<sub>g</sub>
	+ For each edge **He<sub>p</sub>:** a--p-->b in H<sub>g</sub> (where a and b are nodes and p is label), if an edge **Ce<sub>q</sub>:** a--q-->b is present in C<sub>g</sub>. Score for He<sub>p</sub> is defined as the  average of score(p, q) for all such q.
	+ score(p, q): is the fraction of words of p that are present in q

#### Creation of Graph for Question Hypothesis Pairs

- For each question Q<sub>i</sub>
	+ For each option a<sub>ij</sub>
- Generate hypothesis h<sub>ij</sub>: Replace wh word in question by option
- Run OpenIE on each hypothesis to create four graphs.
	+ Initial Approach:
		* Spawn a java process for each hypothesis, generate triplets, then generate graph
		* Didn't complete in even one day.
		* Too slow 1.5 minutes for loading all annotators, ~2 minutes per hypothesis
		* 1174 x 4 x 2 = 9392 minutes = 6.0 days
	+ Run a standalone OpenIE server
		* API call to the server from a python script and dump the generated graphs.
		* 1 hour 21 minutes(4.18 sec per question)

#### Creation of Graph (NCERT): With coreference Resolution
- Coreference Resolution on each of the splitted 10KB files gave memory overflow error.
- Divided ncert file into small 1KB files (splitting into such small files might have caused loosing some coreference)
- Coreferene resolution + large number of files, made the tuple extraction process very slow.
- Took ~6 hours
- Can perform stemming before creating graph. Stopword removal still renders predicates empty.
- Analysis of the resultant graph:
	+ Number of Nodes: 43493
	+ Number of Edges: 73291
	+ Number of Weakly Connected Components (using DFS): 771

|Number of Nodes|Number of Components|
|:-------------:|:------------------:|
|41296			| 1					|
|27				| 1					|
|19				| 1					|
|15				| 1					|
|14				| 1					|
|13				| 2					|
|10				| 1					|
|9				| 7					|
|8				| 7					|
|7				| 20				|
|6				| 12				|
|5				| 25				|
|4				| 52				|
|3				| 149				|
|2				| 484				|
|1				| 7					|

#### Creation of Graph (NCERT): Without coreference Resolution

- Wrote a python script to process the output of OpenIE annotator and create a graph. 
- For each extracted triplet add the following
	+ subject -----predicate-----> object
	+ object -----rev_predicate----> subject (In order to be able to check weekly connectedness)
- If we do stemming and stopword removal before creating graph, most of the triplets loose one or more of the elements because a large number of them are pronouns. So created a graph without doing stemming/stopword removal.
- ~~TODO: Need to do coreference resolution before performing stopword removal/stemming.~~
- Analysis of the resultant graph:
	+ Number of Nodes: 45232
	+ Number of Edges: 74497
	+ Number of Weakly Connected Components (using DFS): 730   


|Number of Nodes|Number of Components|
|:-------------:|:------------------:|
|43114			| 1					 |
|19				| 2					 |
|15				| 1					 |
|14				| 1					 |
|13				| 2					 |
|11				| 1					 |
|10				| 3					 |
|9				| 5					 |
|8				| 9					 |
|7				| 19			     |
|6				| 12			     |
|5				| 29			     |
|4				| 48			     |
|3				| 131			     |
|2				| 466			     |



#### Analyzed why OpenIE analysis of just 2.3 MB file was taking 35 minutes?
- Reasons
	+ Was using a python script to create a seperate child process.
	+ Each child process used Stanford openIE parser seperately for each file.
 	+ Each call and return involved "running a seperate java program" causing huge overhead
 
- Solution
	+ The OpenIE annotator can accept multiple files as arguments
	+ Call it only once with all the files as arguments
	+ Cumulate the results of all the files into a single file

- This reduced the time from 35 minutes to just 2 minutes 33 seconds!!

---

## 3rd Sep 2018

#### Assuming Corpus Graph is built, A simple approach for prediction

- Perform hashing for each edge in the corpus graph of the corpus
	+ This will take a lot of time and space but will make querying O(1).
- Convert Each **Q+a<sub>j</sub>** to a graph say (**G<sub>j</sub>**).
- Check how many edges of these graphs are present in the hashtable of the corpus graph. say **K<sub>j</sub>**
- Prediction -> Based on some function of k and number of edges in **G<sub>j</sub>**

#### Observations

- The rendered graph corresponding to **100** triplets consists of several connected components. Instead of one big connected graph. 
- This may get reduced by certain amount, If we preprocess the data that is lemmetization, stopwords removal etc.

Also, The rendered image does not represent all the generated triplets **(82752)**. The graph corresponding to the entire dataset may well be a single connected graph.

- Can't render such a graph as image.
- Run BFS/DFS to check connectedness?
	+ 82752 lines, two edges per line => Graph with 165504 edges for (2.3 MB file).

---

#### Graph Plotted corresponding to 100 triplets:

![](Open_IE_graph_NCERT.png)
<!-- ![](https://i.imgur.com/s6dvZHp.png) -->

---

#### Try to run OpenIE on NCERT dataset first (because its small)

- Ran on the entire Raw NCERT dataset `(Just 2.3MB)`.
- `java.lang.OutOfMemoryError`

Faced: Mmemory Error for just a 2.3MB file

- NCERT dataset was split into 10KB small files.
- 228 files were created.
- Wrote a script to run Stanford OpenIE on each of them to extract structured relation triple from them.
- Took 35 mins
- ARC Corpus is `1.5GB` in size. 
- Assuming nothing fails. it would take **nearly 17 days!!** to just extract the triplets from it.

---

#### Basic steps done by openIE
- First split each sentence into a set of entailed clauses. 
- Each clause is then maximally shortened, producing a set of entailed shorter sentence fragments. 
- These fragments are then segmented into OpenIE triples, and output by the system. 

---

#### Context of the support sentences

##### TL;DR

- The ARC dataset seem to have no structure at all. 
- The dataset is just a huge collection of Random Sentences from Academics Resource.
-  There is no context that exists around any sentence.
- [Link to first 100 lines of corpus](https://gist.github.com/hthuwal/6c4a7fc9454cfef851b2c6ac6033b022)

**Support Sentence:** Since the Blue Nile is a highly seasonal river, the dam would reduce flooding downstream of the dam, including on the 40 km stretch within Ethiopia.

- 5 sentences before this in corpus:

	- By showing how the planet’s sped up while at certain points in their orbits, and slowed down in others, Kepler resolved this.
	- Artificially sweetened beverages can make naturally sweet foods taste less appealing.
	- Interphase and the mitotic phase
	- 310) Chromalveolate Alveolata (Ciliates, Dinoflagellates, Apicomplexans)
	- By the time national socialism rose to power in Germany the jews had come to the consensus that race was definitely bad for the jews, and they were throwing all their efforts into an idea, a movement that would eventually be called “anti-racism”.

- 5 sentences after this in corpus

	- Before adding this tag , think about whether this is the best option .
	- But I doubt that you’ll find that view among the regulars in this forum.
    - Perhaps for her separate property she may, with her husband, appoint an agent or attorney; Cro.
    - Trust you to auto-ship toner as needed, monitored by software installed in their printers.
	- Locomotives of the London and North Eastern Railway .

**Support Sentence:** Dams alter the flow, temperature and sediment in river systems.

- 5 sentences before this in corpus:

	+ Don't have golgi body, Endo Retic, mitochondria, or chloroplasts.
	+ In urban areas, raccoons may nest in drainpipes, basements, crawl spaces and house attics.
	+ Fix ave/histo fix does not calculate local values
	+ Etodolac may increase the anticoagulant activities of Heparin.

- 5 sentences after this in corpus:

	+ Hospital codes the patient for over an hour, is unable to get her back.
	+ Another study of cucumber extracts in animals, published in the  Archives of Dermatological Research , found increased overall antioxidant benefits.
	+ Mira AL makes it easy to make research quality photometric measurements of any number of stars with any number of standards on any number of images.
	+ Treat outdoors or in a well-ven- tilated room.
	+ Sense-organs as ten- taculocysts.

## 30th Aug 2018
Can we use or learn from the top models at [The Stanford Question Answering Dataset](https://rajpurkar.github.io/SQuAD-explorer/)?

- Answer Reading Comprehension Questions
- also abstain when presented with a question that cannot be answered (Only in 2.0)

**Ideas**   

- Remove Elastic Search from Pipeline.
	1. Run SOTA model from above to extact proposed answer from the Corpus.
		* May be entire corpus at once
		* Or One paragaph at a time
		** Then guess the option based on those proposed answer. Using DGEM or somehting else.
	2. OR Run SOTA model to extract proposed words fom options. Will tell where answer is not present. Then choose from remaining?

### Manual Analysis NCERT vs ARC

Some Examples where NCERT scored greater than ARC

+ Question: A caterpillar eats an oak leaf. Which of the following best describes the energy transfer in this situation?
	* Correct Answer: **Energy is transferred from the** leaf to the caterpillar.
	* ARC: All support sentences just say **caterpillar eats leaf**
	* NCERT: Support: **energy is transferred from the** former to the
		- **Almost Exact sentence was present**
+ Question: 22 Sandy is conducting an investigation to find out which food his dog likes best. Which is the manipulated variable in his investigation?
	* Correct Answer: The kind of food he gives his dog
	* All NCERT support sentences: **his**
	* No Corelation whatsover. **his** is present in other options too

+ Question: A potential negative impact of building a dam on a river is that the dam
	* Correct Answer(A): prevents **sediment** from flowing downstream.
	* ARC gets support for (A): Dams alter the flow, temperature and **sediment** in river systems.
	* But selects (C: prevents **seasonal** **downstream** **flooding**) whose support was -> Since the Blue Nile is a highly **seasonal** river, the dam would reduce **flooding** **downstream** of the dam, including on the 40 km stretch within Ethiopia.
	* NCERT all support: increases the life of the downstream dam. chooses correct.
	* support doesnt seem to "support" the predicted answer.

It just all seems random that some questions were answered correctly!!

### What the code does

**Creating Index from Cropus**

- Each line of document converted to a json object with raw_text as one of the entries.
- Bulk add the documents to ES index
- **analyzer** used: snowball stemmer
	+ standard tokenizer: split on anything non alphanumeric
	+ convert to lowercase
	+ remove stopwards
	+ stemming using snowball stemmer

**Querying Support Sentences**

- Q<sub>i</sub> + Option<sub>ij</sub>
- **analyzer**(Q<sub>i</sub>) must be present in doc and contriubte to score
- **analyzer**(Option<sub>ij</sub>) must be present but doesnt to contriubte to overall score 
- Top 10 hits are returned by ES of which top 8 are kept
- Convert QA + support  -> hypothesis + support(premise)
- hypothesis -> Open IE -> structured hypothesis
- DGEM: does **support(premise) entails hypothesis?** returns score for every such pair 
- for each Option<sub>ij</sub> -> returns maximum (score, hypothesis) as score
- Choose option with maximum score. If k options have highest score and one fo them is correct: marks = (1/k)

**Example**
```Json
{
  "id": "Mercury_7175875",
  "question": {
    "stem": "An astronomer observes that a planet rotates faster after a meteorite impact. Which is the most likely effect of this increase in rotation?",
    "choice": {
      "text": "Planetary density will decrease.",
      "label": "A"
    },
    "support": {
      "text": "As latitude increases and the speed of the earth's rotation decreases, Coriolis effect increases.",
      "type": "sentence",
      "ir_pos": 0,
      "ir_score": 33.626408
    }
  },
  "answerKey": "C",
  "premise": "As latitude increases and the speed of the earth's rotation decreases, Coriolis effect increases.",
  "hypothesis": "An astronomer observes that a planet rotates faster after a meteorite impact. Planetary density will decrease is the most likely effect of this increase in rotation.",
  "hypothesisStructure": "An astronomer<>observes<>that a planet rotates faster after a meteorite impact$$$a planet<>rotates faster<>$$$Planetary density<>will decrease<>$$$Planetary density will decrease<>is<>the most likely effect of this increase in rotation",
  "score": 0.061089504510164
}
```
### ElasticSearch

#### Analogy
Relational DB ⇒ Databases ⇒ Tables ⇒ Rows      ⇒ Columns   
Elasticsearch ⇒ Indices   ⇒ Types  ⇒ Documents ⇒ Fields

- Almost Real Time
- Scalable, Fast, Reliable, Distributed etc
- Its all about how beautifully documents are stored -_-_

Document : Stored as a Json Object
index: a place where document live

#### Querying

**Types**

1. **Structured**: Query on Fields of the Json Object. Similar to SQL queries
	1. query string
	2. query DSL (JSON request body)
2. **Unstructured (What DGEM uses)**: A full-text query, find all documents matching the search keywords and sort them by relevance score.
	1. Elastic Search **analyzes** the text and creates ***inverted index -_-***
	2. Preprocessing: tokenization, character_filters, to_lowercase, stemming, synonyms (these people call it "**analysis**" and done by **analyzers**)
		- can create custom analyzers
		- some defualts: standard, language specific 	
	3. TF/IDF with field length normalization (Local IDF not even Global for scalability).
	4. It uses Lucene's Practical Scoring Function
	5. Other option: SOTA Okapi BM25 ranking function.
		- BM25 has upper limit on term frequency: nonlinear TF saturation	 
	6. Can cosnsider proximity by saving position of words in inverted index
	5. RegExp, prefix matching.

----

#### Consolidate NCERT dataset

- Support Sentences retrieved using the NCERT raw dataset were very small. Some even containing only 1 word!! Therefore tried to consolidate data   
- Consolidate all Lines not seperated by blank lines into paragraphs: **Score 20.98**.
- Why did this decrease? Are all the results below random baseline just **"random"**?

## 27th Aug 2018

TODO: Go through: A [new relevant paper](https://arxiv.org/abs/1806.00358) that uses ARC dataset.

#### Manual Analysis
Some Questions with zero score and for which maximum number of support sentences were obtained from ARC dataset are chosen For Manual Analysis.

1. An astronomer observes that a planet rotates faster after a meteorite impact. Which is the most likely effect of this increase in rotation?  
	- Incorrectly Answered using any combination of corpuses
	- **Correct Answer**: Planetary days will become shorter.
	- Some Sentences from corpus: It is evident that **correct answer can be deduced** based on these sentences
		+ `ARC`: ...**Earth's rotation** was **changed** by the earthquake to the point where the **days** are now 1.8 microseconds **shorter**...
		+ `ARC`: **changed** the ***earth's rotation**, making the **day** a **bit shorter** than usual  
		+ `WebChild`: **earth** is the 3rd **planet** from the sun 

2. A bicycle is traveling at 3 meters per second (m/s). The rider applies the brakes and stops the bicycle in 3 seconds. What is the average rate of acceleration of the bicycle during this time?
	- Numerical Problem: Need to form arithmetic/kinematics equations and do calculations.
	- Answer **cannot be inferred** from any of the corpus

3. Which is an example of electricity flowing in a circuit to produce sound?
	- Incorrectly answered using any combination of corpuses
	- **Correct Answer**: pushing a button to make a doorbell ring
	- Some Sentences from corpus: **Can be infered**
		+ `ARC`: ... basic idea behind an **electric** chime **doorbell** ...
		+ `ARC`: ... beeping trilling **sound** similar to that of a **doorbell** ...
		+ `WebChild`: ... **doorbell** is a **push button** at an outer door that gives a **ring**ing or buzzing signal when pushed ..

<br>

#### An Observation:
- Question Wise scores for all these scenarios are updated in the [spreadsheet.](https://docs.google.com/spreadsheets/d/151zuO4OEE7Z1zyyDnMPC5DXp-aeJ31ROvm_7-edUVa8/edit?usp=sharing). 
- If NCERT and Web_child datasets are used independently that is without the ARC dataset:
	+ We answer **14.42%** questions with better scores.
	+ But when used in conjunction with ARC dataset, the score
		* stays same for ARC + NCERT
		* decreases for ARC + WebChild
- Can we use the other corpus indirectly so that their effects contribute to the final score?

---

- Even using the entire cumulative webchild dataset as corpus seems to only degrade the performance.
- Why? Use the database differently?

|Corpuses_Used|Accuracy on Challenge Test set|
|:-----------:|:----------------------------:|
|ARC|26.41%|
|WebChildAll|20.44%|
|ARC+ WebChildAll|26.05%|

#### WebChild Contains several [corpus files](https://www.mpi-inf.mpg.de/departments/databases-and-information-systems/research/yago-naga/webchild/)

- **WebChildAll:** Cleaned/Parsed and merged them into a single more comprehensive file.
	+ [nouns.gloss](https://www.mpi-inf.mpg.de/departments/databases-and-information-systems/research/yago-naga/webchild/)
		* contains: (word, possible_meanigs list)
		* converted to: `word is meaning`
	+ [Partwhole](http://people.mpi-inf.mpg.de/~ntandon/resources/readme-partwhole.html)
		* **physical_of**
			- contains: (worda, wordb)
			- converted: `worda is part of wordb`
		* **substance_of**
			- contains: (worda, wordb)
			- converted: `worda has subtance wordb in it`
		* **member_of**
			- contains: (worda, wordb)
			- converted: `worda is a member of wordb`
	+ [comparative](http://people.mpi-inf.mpg.de/~ntandon/resources/readme-comparative.html)
		* contains: *worda*, comparison phrase, wordb. 
		* converted: `car is faster than bike`

	+ [property](http://people.mpi-inf.mpg.de/~ntandon/resources/readme-property.html)
		* contains: word, property, value | e.g. plant, color, green
		* converted: `plant has green color` and `plant is green`
	
	+ [spatial](http://people.mpi-inf.mpg.de/~ntandon/resources/readme-spatial.html)
		* contains: worda, wordb, list of spatial relations
		* converted: e.g `spine is around the muscle`, `spine is next to the muscle`, `spine is along the muscle`  

----

- Adding the noun definitions from the Web Child decreases the overall total score. Why?
- Probably because WebChild corpus size is too small?
- NCERT dataset on the otherhand seem to have no effect.

|Corpuses_Used|Accuracy on Challenge Test set|
|:-----------:|:----------------------------:|
|ARC|26.41%|
|NCERT|22.99%|
|NounGloss(WebChild)|22.69%|
|ARC + NCERT|26.41%|
|ARC + NounGloss(WebChild)|26.20%|
|ARC + NCERT + NounGloss(WebChild)|26.20%|

## 17th Aug 2018
Frequency of Named Entities in the respective datasets is now in the [spreadsheet.](https://docs.google.com/spreadsheets/d/151zuO4OEE7Z1zyyDnMPC5DXp-aeJ31ROvm_7-edUVa8/edit?usp=sharing). 

- Comparison of the three corpuses

	|Name|Number_of_lines|freq = 0|freq < thresh|
	|----|---------------|-------|-------|
	|ARC|14621856|0.14%|21.8% < 100, 15% < 1000|
	|webchild|177801|6.2%|26.13% < 10|
	|ncert-6-7-8-9-10|90110|22.15%|29.29% < 1|


## 14th Aug 2018
- Downloaded NCERT 6th to 10th pdf science books online.
- Performed StanfordNER on all the questions/options in the challenge set.
	+  5045 unique named_entities
- Setup environment on aryabhatta. Debugged elasticsearch as several elasticsearch instances were already running by other users.

## 7th Aug 2018
1. Preliminary analysis shows that the major bottleneck is the discovery of good support sentences. Because in majority of the cases the extracted support sentences do not contain enough information to find the correct answer.
2. Tried Using **Webchilds hasproperty triplets** + original corpus -> Again "support sentences seems to be the bottleneck"
3. DGEMs 27% result is based on properitery IE software (not opensource)
4. The new papers result although seem amazing but since code is not available is not verifiable.

##  1st Aug 2018
- DGEM
	- DGEM model: Ran for all the questions and collected the results in a [spreadsheet](https://docs.google.com/spreadsheets/d/151zuO4OEE7Z1zyyDnMPC5DXp-aeJ31ROvm_7-edUVa8/edit?usp=sharing).
	- Going through each question and the corresponding options and support manually.
	- Check if analysis is similar to the analysis done in KG<sup>2</sup> paper.

- New Paper [KG<sup>2</sup>](https://arxiv.org/pdf/1805.12393.pdf)
	- Claims: 31.70% on challenge set.
	- Claims: 51% difficulty in choosing correct answer is due to insufficient support.
		- **_Can use definitions here along with the provided dataset._**
		- **_Use something other than elastic search to find bettersupport sentences._**
	- Source code: One of the authors of the paper replied that **The current paper is preliminary, and they are still working on it. They'll release the code after they submit a formal paper to some venue.**

## 27th July 2018
- Source code is not available for: [KG<sup>2</sup>: 31.70%](https://arxiv.org/pdf/1805.12393.pdf) paper.
- ~~Its been 3 days since I contacted the authors asking whether they are planning to opensource the code or not.~~
- ~~Haven't received any reply yet~~
- ~~Meanwhile running DGEM-OpenIE~~

## 23rd July 2018

### New Paper Published: [KG<sup>2</sup>](https://arxiv.org/pdf/1805.12393.pdf)

- Best model yet! - Claims 31.70% on challenge set.


	|Method | Test Scores|
	|------ | -----------|
	|KG<sup>2</sup> | 31.70|
	|TableILP | 26.97|
	|BiDAF | 26.54| 
	|DGEM-OpenIE | 26.41 |
	|			 |(27.11 using some propiretry parser) | 
	|Guess-all / Random | 25.02|
	|DecompAttn | 24.34|
	|TupleInference | 23.83|
	|IR-Google | 21.58|
	|IR-ARC | 20.26|


- Based on **Contextual Knowledge Graph Embeddings.**
- Accuracy on Easy Set not known yet.

&nbsp; **Ideas**

- Wordnet
	- connects words from same POS.
	- Can we use it to improve the edge embedding learned in DGEM?
	- Convert Hierarchial Connections to edge embedding? How?
	- How much scince is in dataset?

- [Universal Language Model Fine-tuning for Text Classification](https://arxiv.org/pdf/1801.06146.pdf)
	- These Fine Tuning Techniques may be helpful (Like Transfer learning in CV)

**TODOs**

- [X] Understand allenNLP API (ongoing)
- [X] Debug the remaining two neural models (ongoing)
- [X] Read WordTree A corpus of Explanation Graphs 
- [ ] Go through the code of DGEM paper to better understand the architecture
- [x] See if the errors can be reduced using WordNet, WebChild, NCERT.


## 16th July 2018
Back to work
## June 15 to June 29
Family Trip
## June 05 to June 12
Went to hometown

### 30-05-2018
### TL;DR

**1. Papers Read**

- [x] Information Retrieval 
- [x] PMI
- [x] Table ILP 2016
- [x] Tuple Inference 2017
- [x] Decompositional Attention 2016
- [x] BiDAF 2017
- [x] DGEM 2018: **Model is complicated. Need to go thorugh the code to understand it better**
- [ ] WordTree A corpus of Explanation Graphs

Note: Available code is implemented using allenNLP and elasticsearch libraries (So, need to learn these frameworks to avoid re-implementing neural models)

**2. Models/Error Analysis**

- Code/Model only available for Neural Models: Decompositional, BiDAF and DGEM
- Even after spending considerable time Couldn't Run the models on my laptop (4 GB Memory wasn't enough)
- ~~Was facing some connectivity issues on the DAIR lab machine~~
- Connection Issue on DAIR machine is now resolved, ~~Will try to run models on it~~.
- Tried models, ran into pytorch errors, probably because code written in older version of pytorch.
- ~~Need access to servers!~~

---

### Few Benchmark queries (apart from the ones given [here](http://ai2-website.s3.amazonaws.com/publications/AI2ReasoningChallenge2018.pdf))

- **(Factual)**<br/>
Which property of air does a barometer measure?<br/>
a) pressure b) speed c) humidity d) temperature

- **(Semantic Relation)**<br/>
What is one way to change water from a liquid to a solid?<br/>
a) Decrease Temp b) Increase Temp c) Decrease Mass d) Increase Mass

- **(Parallel Evidence)**<br/> 
Sleet, rain, snow and hail are forms of<br/>
a) Erosion b) evaporation c) groundwater d) precipitation

- **(Perturbed Questions/Options)**<br/> 
In New York State, the longest period of daylight occurs during which month?<br/>
a) Eastern b) June c) history d) years.

- **(Multifact Reasoning)**<br/>
Which object in our solar system reflects light and is a satellite that orbits around one planet?<br/>
a) Earth b) Mercury c) Sun d) Moon

- **(Missing Important Words)**<br/>
Which material will spread out to **completely** fill a larger container?<br/>
a) air b) ice c) sand d) water

- **(Bad Alignment) - CO<sub>2</sub> aligned with breathe out**<br/>
Which of the following gases is necessary for humans to breathe in order to live?<br/>
a) Oxygen b) Carbon dioxide c) Helium d) Water vapor

---

### Info from various Papers 

- **q** -> question
- **a<sub>i</sub>** -> ith option (answer)

### BaseLine Solvers to divide dataset

#### 1. Information trieval

- Waterloo corpus 10<sup>10</sup> tokens (in original paper) [ARC dataset for this]
- query: *q + a<sub>i</sub>* -> Search Engine (Elasticsearch) -> top retrieved sentence **s** + score
- s should have at least one non-stopword overlap with q, and at least one with ai to ensures s has some relevance to both q and a<sub>i</sub> .
- repeat for all a<sub>i</sub> and report a<sub>i</sub> with highest score

#### 2. PMI

- Waterloo corpus
- Extract n-grams from q. 
- Calculate average PMI of each a<sub>i</sub> over all n-grams.
- Report a<sub>i</sub> with largest PMI


### Easy set vs Challenge Set

Challenge questions are answered incorrectly by both of the baseline solvers.

Basically removes most of the factoid questions(more likely to be present in the corpus)


### Other Non Neural Solvers


#### 1. Table ILP 2016, Score: 26.97

- ILP over semi structured knowledge base (Tables).
- components of q, a<sub>i</sub>, table_celss, headers -> nodes of graph.
- edge weights: similaity and entailment using WordNet. [Tuple Inference paper claims that this gives unreliable scores on longer phrases]
- find subgraph that **best** supports an option.
- **best** -> ILP formalism representing structural and semantic constraints.
- solve ILP -> using SCIP

&nbsp; **Updates**

- Table created manually + query based interactive automatic table building tool.
- Table of Subject verb object extracted using OpenIE.
- Edge denotes equality between nodes.
- Equality: Phrase Level entailment -> accounts for generalization, and lexical variability

&nbsp; **Problems/Doubts**

- Model not available
- Manually defining table schemas!! -> Time consuming
- ~~IKE, Open IE (SVO)?~~
- penalizing evidence chaining..Good or Bad?
- Irrelevent Evidence chaining may bring noise

#### 2. Tuple Inference 2017, Score: 23.83

- Tuple: (subject; predicate; objects)
- Generation of tuples: Elasticsearch (q, a<sub>i</sub>) on trainset. Open IE on top 200 hits??
- (q, <sub>i</sub>) from test set. take top 50 tuples based on tf-idf scoring.
- q, a<sub>i</sub>, tuples -> vertices
- Rest similar to TableILP

&nbsp; **Problems/Doubts**

- Model Not available
- Open IE?? (lossy nature? use jaccard score?)
- Conversion to tuples may cause loss of important bits of information.

### Neural Solvers

**Need to go through their codes to better understand the architecture**

#### 1. Deompositional Attention 2016, 24.34

&nbsp; **Adaptation**

- q + a<sub>i</sub> = hypothesis(h<sub>i</sub>). 
- Use h<sub>i</sub> as search query to retriev sentences(elasticsearch?) t<sub>ij</sub>.
- Calculate entailment scores between h<sub>i</sub> and t<sub>ij</sub>
- Report a<sub>i</sub> with maximum overall entailment score.

&nbsp; **Original Idea**

- Sentence a, Sentence b as input
- Output y belongs to {**E**ntailment, **N**eutral, **C**ontradiction}
- Each word is a pure embedding (GLOVE) or augmented with intra sentence attention.
- Attention (soft alignment matrix) -> Comparison -> Aggregation ->  output y

#### 2. BiDAF: Direct Answer System 2017, 26.54

&nbsp; **Adaptation**

- Create paragrph of bunch of retrieved sentences for q
- Apply BiDAF to find a answer span for q(context) from this paragraph
- pick a<sub>i</sub> that maximally overlaps with this answer span

&nbsp; **Original Idea**

- Hierarchial Architecture
	- Character Embedding Layer: **Char CNNs**
	- Word Embedding Layer: **Pre-trained word Embedding Model**
	- Contextual Embedding Layer
	- Attention Layer
	- Modeling Layer: **RNN**
	- Output Layer 

#### 3. DGEM 2018, 27.11

&nbsp; **Adaptation**

&nbsp; Same as Decompositional Attention

&nbsp; **Original Idea**

&nbsp;&nbsp;**Incorporate semantic and structural knowledge into model explicitly using graphical structure (similar to Tuple ILP)**

- Intution: Knowledge **--entails-->** (q, a<sub>correct</sub>)
- Create entailment dataset of above form.
- Three types of data point: Complete Entailment, Partial Entailment, Unrelated
- Uses OpenIE tuples and graph similar to Tuple Inference.
- edges: OpenIE tags, prepositions
- Node Attention: attention of the words in the node over the words in the premise. softmax of (dot product of embeddings)
- Each node represented using attention weighted LSTM
- Learn embedding for each edge label
- Probability calculation?

&nbsp; **Other Problems/Doubts**

- Manual creation of entailment dataset 
- Find Type of entailment. Done manually using Amazon Mechanincal Turk
- Manual convesion of every (q, a<sub>i</sub>) to entailment form::
: https://github.com/titoBouzout/Dictionaries.git
