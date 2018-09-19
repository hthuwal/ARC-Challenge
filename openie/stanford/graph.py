import sys
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
        list_of_strings = [" ".join([stemmer.stem(word) for word in each.strip().split()]) for each in list_of_strings]
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

    def compare_strings(self, a, b):
        a = a.lower().split()
        b = b.lower().split()
        score = 0
        for each in a:
            if each in b:
                score += 1
        return score / (1 + len(a))

    def compare_edge_labels(self, e_a_list, e_b_list):
        score = 0
        e_a_list = [each for each in e_a_list if not each.startswith('rev')]
        e_b_list = [each for each in e_b_list if not each.startswith('rev')]
        for ea in e_a_list:
            for eb in e_b_list:
                score += self.compare_strings(ea, eb)
        return score / (1 + len(e_a_list) * len(e_b_list))

    def compare_edges(self, e_a, e_b):
        score = 0
        for nbr in e_a:
            if nbr in e_b:
                score += self.compare_edge_labels(list(e_a[nbr]), list(e_b[nbr]))
        return score / (1 + len(e_a))

    def compare_graph(self, g):
        score = {}
        score['nodes'] = 0
        score['edges'] = 0

        for node in g.adj:
            if node in self.adj:
                score['nodes'] += 1
                score['edges'] += self.compare_edges(g.adj[node], self.adj[node])

        return (score['nodes'] / (1 + len(g.adj))) + score['edges']
        # return score


if __name__ == '__main__':
    file = sys.argv[1]
    stem = sys.argv[2]
    stem = True if stem == "True" else False
    print(stem)
    g = Graph(file, stem)
    print(g.num_nodes())
    print(g.num_edges())
    print(g.connectedness())
