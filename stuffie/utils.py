import json
import os

from tqdm import tqdm


def pprint(json_data):
    print(json.dumps(json_data, indent=4, sort_keys=True))


def read_stuffie_output(file):
    with open(file) as f:
        triplets, key, nfacet = {}, None, 0
        for line in f:
            line = line.strip()

            if not line:
                continue

            if line.startswith("##"):
                """Beginning of a new parsed sentence."""
                if triplets:
                    yield triplets
                del triplets
                key = None
                triplets = {}

            else:
                line = line.split(":")
                if len(line) == 2:
                    # if key is not None:
                    #     triplets[key].append(nfacet)

                    """A new triplet"""
                    key = line[0]
                    nfacet = 0
                    line = line[1].strip().split('; ')
                    triplets[key] = [each.strip().strip(';') for each in line]

                elif len(line) == 1:
                    """A facet of last triplet"""
                    nfacet += 1
                    new_key = f"{key}.{nfacet}"
                    line = line[0].strip().split('; ')
                    triplets[new_key] = [each.strip().strip(';') for each in line]


def read_all(directory='results/triplets'):
    for file in os.listdir(directory):
        yield from read_stuffie_output(os.path.join(directory, file))


def replace_references(triplets):
    keys = sorted(triplets.keys(), reverse=True)

    # Replacing reference to another triplet with the subject of
    # referred triplet
    for key in keys:
        triplet = triplets[key]
        for i, word in enumerate(triplet):
            if word.startswith("#"):
                fkey = word[1:]
                try:
                    # TODO: Should I use entire triple instead of just the head?
                    triplets[key][i] = triplets[fkey][0]
                except Exception as e:
                    print(fkey)
                    print(triplets)
                    input()
    # Adding subject of parent as the subject of each facet
    for key in keys:
        i = key.find('.')
        j = key.rfind('.')
        if i != j:
            parent = key[:j]
            # TODO: Should I use entire triple instead of just the subject?
            triplets[key].insert(0, triplets[parent][0])

    return triplets


if __name__ == '__main__':
    count = 0
    # for each in tqdm(read_all(), ascii=True):
    for each in tqdm(read_stuffie_output("results/combined_triplets.txt"), ascii=True):
        count += 1
        # pprint(each)
        replace_references(each)
        # input()
    print(count)
