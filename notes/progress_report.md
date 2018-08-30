##30th Aug 2018
Can we use or learn from the top models at [The Stanford Question Answering Dataset](https://rajpurkar.github.io/SQuAD-explorer/)?

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
- Manual convesion of every (q, a<sub>i</sub>) to entailment form.
