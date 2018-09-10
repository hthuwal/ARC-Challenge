import nltk
import sys
from tqdm import tqdm
from collections import defaultdict, Counter

with open("stopwords_en.txt", "r") as f:
    stopwords = [each.strip() for each in f.readlines()]


def process_entity_relations(entity_relations_str, verbose=True):
    # format is ollie.
    entity_relations = list()
    for s in entity_relations_str:
        entity_relations.append(s[s.find("(") + 1:s.find(")")].split(';'))
    return entity_relations


def strip(list_of_strings, stem=False):
    stemmer = nltk.stem.porter.PorterStemmer()
    if stem:
        list_of_strings = [" ".join([stemmer.stem(word) for word in each.strip().split() if word not in stopwords]) for each in list_of_strings]
    else:
        list_of_strings = [each.strip().lower() for each in list_of_strings]
    # list_of_strings = [stemmer.stem(each) for each in list_of_strings if each not in stopwords]
    return list_of_strings


class Graph(object):
    def __init__(self, file, stem=False):
        self.adj = defaultdict(lambda: defaultdict(set))
        self.source_file = file
        self.build(stem)

    def read(self, stem=False):
        relations = open(self.source_file, "r").readlines()
        relations = process_entity_relations(relations)
        relations = [strip(entity_relations, stem) for entity_relations in relations]
        return relations

    def build(self, stem=False):
        # print("Reading and cleaning relations")
        relations = self.read(stem)
        # print("Building Graph")
        for entity_relation in relations:
            subj = entity_relation[0]
            pred = entity_relation[1]
            obj = entity_relation[2]
            self.adj[subj][obj].add(pred)
            self.adj[obj][subj].add("rev_" + pred)

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
        return Counter(num)

    def num_nodes(self):
        return len(self.adj)

    def num_edges(self):
        edge = 0
        for node in self.adj:
            edge += len(self.adj[node])
        return edge / 2


if __name__ == '__main__':
    file = sys.argv[1]
    g = Graph(file)
    print(g.num_nodes())
    print(g.num_edges())
    print(g.connectedness())
