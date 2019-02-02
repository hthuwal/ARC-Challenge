import sys
from collections import defaultdict, Counter
from tqdm import tqdm
import nltk
import json

with open("stopwords_en.txt", "r") as f:
    stopwords = [each.strip() for each in f.readlines()]


def process_entity_relations(entity_relations_str, disable=False):
    # format is ollie.
    entity_relations_str = list(entity_relations_str)
    entity_relations = list()
    for s in tqdm(entity_relations_str, ascii=True, disable=disable):
        temp = s[s.find("(") + 1:s.find(")")].split(';')
        entity_relations.append(temp)
    return entity_relations


def strip(list_of_strings, stem=False):
    stemmer = nltk.stem.porter.PorterStemmer()
    if stem:
        list_of_strings = [" ".join([stemmer.stem(word) for word in each.strip().split()]) for each in list_of_strings]
    else:
        list_of_strings = [each.strip().lower() for each in list_of_strings]
    # list_of_strings = [stemmer.stem(each) for each in list_of_strings if each not in stopwords]
    return list_of_strings


class Graph(object):
    def __init__(self, file=None, stem=False, disable=False):
        self.adj = defaultdict(lambda: defaultdict(set))
        self.source_file = file
        if file is not None:
            self.build(stem=stem, disable=disable)

    def save(self, file):
        json.dump(self.adj, open(file, "w"))

    def load(self, file):
        self.source_file = file
        self.adj = json.load(open(file, "r"))

    def read(self, stem=False, disable=False):
        relations = open(self.source_file, "r").readlines()
        if not disable:
            print("Converting into list of triplets")
        relations = process_entity_relations(relations, disable=disable)
        if not disable:
            print("Stripping")
        relations = [strip(entity_relations, stem) for entity_relations in tqdm(relations, ascii=True, disable=disable)]
        return relations

    def build(self, stem=False, disable=True):
        if not disable:
            print("Reading and cleaning relations")
        relations = self.read(stem=stem, disable=disable)
        if not disable:
            print("Building Graph")
        for entity_relation in tqdm(relations, disable=disable, ascii=True):
            if(len(entity_relation) == 3):
                subj = entity_relation[0]
                pred = entity_relation[1]
                obj = entity_relation[2]
                self.adj[subj][obj].add(pred)
                self.adj[obj][subj].add("rev_" + pred)
        del relations

        if not disable:
            print("Converting sets of edges to lists")

        for subj in tqdm(self.adj, disable=disable, ascii=True):
            for obj in self.adj[subj]:
                self.adj[subj][obj] = list(self.adj[subj][obj])

        if not disable:
            print("Graph Building Completed")

    def dfs(self, start, visited):
        st = []
        visited[start] = True
        st.append(start)
        num_of_members = 1
        while(len(st) > 0):
            top = st.pop()
            for key in self.adj[top]:
                if not visited[key]:
                    visited[key] = True
                    num_of_members += 1
                    st.append(key)
        return num_of_members

    def connectedness(self):
        visited = defaultdict(lambda: False)
        num = []
        for key in self.adj:
            if not visited[key]:
                num.append(self.dfs(key, visited))
        components = list(Counter(num).items())
        components.sort(key=lambda x: x[0], reverse=True)

        return components

    def num_nodes(self):
        return len(self.adj)

    def num_edges(self):
        edge = 0
        for node in self.adj:
            edge += len(self.adj[node])
        return edge / 2
        # return score

    def __repr__(self):
        representation = "Number of Nodes: %d \
            \nNumber of Edges: %d \
            \nComponents: \
            \nNodes\tNumber of components \
            """ % (self.num_nodes(), self.num_edges())

        components = self.connectedness()
        for a, b in components:
            representation += "\n%d\t%d" % (a, b)
        return representation + "\n"


if __name__ == '__main__':
    file = sys.argv[1]
    stem = sys.argv[2]
    stem = True if stem == "True" else False
    print(stem)
    g = Graph(file, stem)
    print(g.num_nodes())
    print(g.num_edges())
    print(g.connectedness())
