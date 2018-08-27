from tqdm import tqdm


new = True


def do(inp, func):
    mode = "a+"
    global new
    if new:
        mode = "w+"
        new = False

    with open(inp, "r", errors="ignore") as f, open("web_child.txt", mode) as out:
        header = True
        for line in tqdm(f):
            if not header:
                final_strings = func(line)
                # print(line)

                for each in final_strings:
                    out.write(each + "\n")
            header = False


def clean_gloss(line):
    line = line.split('\t')
    word = line[0]
    meanings_examples = line[-1].split(";")
    meanings = []
    for each in meanings_examples:
        each = each.strip()
        if each.startswith('"') and each.endswith('"'):
            continue
        meanings.append(each)

    meanings = [word + " is " + each for each in meanings]
    return meanings


def clean_member_of(line):
    line = line.split("\t")
    return [line[4] + " is a member of " + line[1] + "."]


def clean_physical_of(line):
    line = line.split("\t")
    return [line[4] + " is a part of " + line[1] + "."]


def clean_substance_of(line):
    line = line.split("\t")
    return [line[1] + " has substance " + line[4] + " in it."]


def clean_comparative(line):
    line = line.split("\t")
    if len(line) >= 6:
        return [line[0] + " " + line[2] + " " + line[5] + "."]
    else:
        return []


def clean_property(line):
    line = line.split("\t")
    if line[1] == '-':
        return [line[3] + " is " + line[4] + "."]
    else:
        return [line[3] + " has " + line[4] + " " + line[1][0:-3] + ".", line[3] + " is " + line[4] + "."]


def clean_spatial(line):
    line = line.split("\t")
    # print(line)
    # input()
    a = line[0][:-4].strip()
    b = line[1][:-4].strip()
    relations = line[2].split(",")
    relations = [each.split(":")[0].strip() for each in relations]

    return [a + " is " + each + " " + b for each in relations]


do("noun.gloss", clean_gloss)
do("webchild_partof_memberof.txt", clean_member_of)
do("webchild_partof_physical.txt", clean_physical_of)
do("webchild_partof_substanceof.txt", clean_substance_of)
do("comparative-cw0912.txt", clean_comparative)
do("property.txt", clean_property)
do("spatial.txt", clean_spatial)
