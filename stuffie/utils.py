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
    for file in tqdm(os.listdir(directory), ascii=True):
        yield from read_stuffie_output(os.path.join(directory, file))


if __name__ == '__main__':
    count = 0
    for each in read_all():
        count += 1
        # pprint(each)
        # input()
    print(count)
