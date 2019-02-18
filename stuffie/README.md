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

### Quick start
You need to have Java 1.8 and a compatible Maven installed. Clone a copy of StuffIE's repo and run Maven to build the artifacts. You can do this in your IDE or from your terminal. See `StuffIEConsoleRunner.java` to see an example of how to run StuffIE.

You may need to increase your java heap size when running StuffIE by setting the `-Xms` and `-Xmx` parameters. 

### Contact
For any question about StuffIE, please send an email to rprasojo@unibz.it.

### Citing StuffIE
If you use StuffIE in your academic work, please cite it with the following Bibtex citation:
```
@inproceedings{prasojo2018stuffie,
  title={StuffIE: Semantic Tagging of Unlabeled Facets Using Fine-Grained Information Extraction},
  author={Prasojo, Radityo Eko and Kacimi, Mouna and Nutt, Werner},
  booktitle={Proceedings of the 27th ACM International Conference on Information and Knowledge Management},
  pages={467--476},
  year={2018},
  organization={ACM}
}
```

### Upcoming features
- Integrated model for facet labeling
- Inter-sentence nested relation and facet extraction