import os
import re

from tqdm import tqdm, trange
from utils_stuffie import read_stuffie_output


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


def convert_to_stanford(infile, ofile, disable_progress_bars=False):
    """Convert the stuffIE output to stanfordopenIE format

    Arguments:
        infile {string} -- [path to stuffIE file]
        ofile {string} -- [path to output file]
    """
    regex = re.compile(r"<ctx*.*>")
    converted_triplets = []
    for triplets in tqdm(read_stuffie_output(infile), ascii=True, disable=disable_progress_bars):
        triplets = replace_references(triplets)
        triplets = clean_triplets(triplets)
        for key in triplets.keys():
            string = f"1.00: ({'; '.join(triplets[key])})"
            string = re.sub(regex, "", string)
            converted_triplets.append(string)

    print(f"Writing {len(converted_triplets)} triplets to {ofile}")
    with open(ofile, "w") as f:
        for i in trange(0, len(converted_triplets), 10000, ascii=True, disable=disable_progress_bars):
            f.write("\n".join(converted_triplets[i:i + 10000]) + "\n")


if __name__ == '__main__':

    print("Converting Corpus Stuffie Output into stanford format")
    convert_to_stanford("results/combined_triplets.txt", "results/stuffie_triplets_in_stanford_format.txt")

    print("Converting QA hypothesis Stuffie Output into stanford format")
    hypo_dir_stuffie = "results/questions/stuffie_hypothesis"
    hypo_dir_stanford = "results/questions/stuffie_hypothesis_stanford_format"
    if not os.path.exists(hypo_dir_stanford):
        os.makedirs(hypo_dir_stanford)

    count = 0
    for file in os.listdir(hypo_dir_stuffie):
        name, ext = os.path.splitext(file)
        if ext == ".openie":
            count += 1
            print(f"{count}:", end=' ')
            convert_to_stanford(os.path.join(hypo_dir_stuffie, file), os.path.join(hypo_dir_stanford, name), disable_progress_bars=True)
