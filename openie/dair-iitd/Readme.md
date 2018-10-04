#### Preprocess text file.

- The model raises null point exception on some sentences that do not end with `[. ? !]`.
- Therefore add fullstop at the end of every sentences.

```bash
python remove_all_single_word_lines.py datset.txt
```

This creates a new preprocessed file with the name `dataset_dair.txt`

---

#### Running the model

```bash
time java -jar openie-assembly.jar -b --split --ignore-errors input_file output_file
```

**Takes forever to complete on a large file. Uses only single core. No MultiCore Support**

---

#### Running Several Instances of the model

```bash
./split-and-run.sh infile outfile num_instances
```

This would split infile into num_instances + 1 files. And run one process on the model on each file parallely. 

**Since model requires 10GB just to start. Memory usage increases exponentially with increment in num_of_instances**