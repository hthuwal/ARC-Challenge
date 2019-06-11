import json
import os
import time

def timeit(function):
    def inner(*args, **kwargs):
        st = time.time()
        out = function(*args, **kwargs)
        et = time.time()
        time_taken = et - st
        unit = "seconds" if time_taken <= 60 else "minutes"
        time_taken = time_taken if time_taken <= 60 else time_taken / 60
        print(f"\nTime take by {function.__name__} is {time_taken} {unit}.\n")
        return out
    return inner

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

        if triplets:
            yield triplets


def read_all(directory='results/triplets'):
    for file in os.listdir(directory):
        yield from read_stuffie_output(os.path.join(directory, file))
