from tqdm import tqdm

with open("noun.gloss", "r") as f, open("nouns_meanings.txt", "w") as out:
    for line in tqdm(f):
        line = line.split('\t')
        # print(line)
        word = line[0]
        meanings_examples = line[-1].split(";")
        meanings = []
        for each in meanings_examples:
            each = each.strip()
            if each.startswith('"') and each.endswith('"'):
                continue
            meanings.append(each)

        for each in meanings:
            out.write(word + " is " + each + "\n")
