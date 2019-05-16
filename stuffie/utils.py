import json
import os
import re

from tqdm import trange, tqdm

# ############################################################################ #
#                                StuffIE Parsing                               #
# ############################################################################ #


def pprint(json_data):
    """Pretty Print Json data

    Arguments:
        json_data {[json_formatted]} -- [Json data to be pretty printed]
    """
    print(json.dumps(json_data, indent=4, sort_keys=True))


def read_stuffie_output(file):
    """Read the output of the stuffIE process.
       Generate triplets for each sentence one by one.

    Arguments:
        file {[string]} -- [File containing the results of stuffIE]
    """
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
    """Replace the references in triplets with subject 
    of triplet.

    Arguments:
        triplets {[dict - (key: string, value: list)]} -- [Triplets of one line]

    Returns:
        [dict] -- [triplets with resolved references]
    """
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
                    # StuffIE references triplets which do not exist
                    # Ignore StuffIE errors and skip these triplets
                    continue

    # Adding subject of parent as the subject of each facet
    for key in keys:
        i = key.find('.')
        j = key.rfind('.')
        if i != j:
            parent = key[:j]
            # TODO: Should I use entire triple instead of just the subject?
            triplets[key].insert(0, triplets[parent][0])

    # Delete Triplets which contain dangling references
    for key in keys:
        triplet = triplets[key]
        for i, word in enumerate(triplet):
            if word.startswith("#"):
                del triplets[key]
                break

    return triplets


def clean_triplets(triplets):
    """
    Remove the triplet which contains <_>
    """
    tobedeleted = []
    for key in triplets:
        for each in triplets[key]:
            if each == '<_>':
                tobedeleted.append(key)
                break

    for key in tobedeleted:
        del triplets[key]

    return triplets


def convert_to_stanford(infile, ofile):
    """Convert the stuffIE output to stanfordopenIE format

    Arguments:
        infile {string} -- [path to stuffIE file]
        ofile {string} -- [path to output file]
    """
    regex = re.compile(r"<ctx*.*>")
    converted_triplets = []
    for triplets in tqdm(read_stuffie_output(infile), ascii=True):
        triplets = replace_references(triplets)
        triplets = clean_triplets(triplets)
        for key in triplets.keys():
            string = f"1.00: ({'; '.join(triplets[key])})"
            string = re.sub(regex, "", string)
            converted_triplets.append(string)

    print(f"Writing {len(converted_triplets)} triplets to {ofile}")
    with open(ofile, "w") as f:
        for i in trange(0, len(converted_triplets), 10000, ascii=True):
            f.write("\n".join(converted_triplets[i:i + 10000]) + "\n")


if __name__ == '__main__':
    convert_to_stanford("results/combined_triplets.txt", "results/triplets_in_stanford_format.txt")
