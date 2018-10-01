#### Preprocess text file.

- The model raises null point exception on some sentences that do not end with `[. ? !]`.
- Therefore add fullstop at the end of every sentences.

```bash
python remove_all_single_word_lines.py datset.txt
```

This creates a new preprocessed file with the name `dataset_dair.txt`

#### Running the model

```bash
java -jar openie-assembly.jar text_file >  output_file
```
