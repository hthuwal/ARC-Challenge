import sys
import os
import csv
from tqdm import tqdm

path = os.path.dirname(sys.argv[1])
corpus_name, _ = os.path.splitext(os.path.basename(sys.argv[1]))
print(corpus_name)

corpus_file = open(sys.argv[1], "r")
entities_file = open("results/named_entities.txt", "r")

entities = {word: 0 for word in entities_file.read().splitlines()}
entities_file.close()

for line in tqdm(corpus_file):
    line = line.lower().split()
    for word in line:
        if word in entities:
            entities[word] += 1

out_file = open(os.path.join("results", corpus_name + "_analysis.csv"), "w")
csvw = csv.writer(out_file)
csvw.writerow(["Word", "num_occurrences"])

entities = [(key, entities[key]) for key in entities]
entities.sort(key=lambda x: x[0])

for a, b in entities:
    csvw.writerow([a, b])

corpus_file.close()
out_file.close()
