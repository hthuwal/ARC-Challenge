import nltk
import spacy

nltk_stopwords = nltk.corpus.stopwords.words('english')
# nlp = spacy.load('en_core_web_sm')
nlp = spacy.load('en_core_web_lg')

class GSA(object):

    @staticmethod
    def compare_strings(a, b):
        a = a.lower().split()
        b = b.lower().split()
        score = 0
        for each in a:
            if each in b:
                score += 1
        return score / (1 + len(a))

    @staticmethod
    def compare_strings_glove(a, b):
        # tokens = nltk.tokenize.word_tokenize(a)
        # tokens = [token for token in tokens if not token in nltk_stopwords]
        # a = " ".join(tokens)

        # tokens = nltk.tokenize.word_tokenize(b)
        # tokens = [token for token in tokens if not token in nltk_stopwords]
        # b = " ".join(tokens)

        doc1 = nlp(a)
        doc2 = nlp(b)
        return doc1.similarity(doc2)

    @staticmethod
    def compare_edge_labels(e_a_list, e_b_list):
        score = 0
        for ea in e_a_list:
            for eb in e_b_list:
                score += GSA.compare_strings_glove(ea, eb)
                # score += max(0, (1 - nltk.edit_distance(ea, eb) / max(len(ea), len(eb))))
                # score += max(0, (1 - nltk.jaccard_distance(set(ea), set(eb))))  # jaccard_distance based on each character as element
                # score += max(0, (1 - nltk.jaccard_distance(set(ea.split()), set(eb.split()))))  # jaccard_distance based on each character as element
                # score += GSA.compare_strings(ea, eb)
        return score / (1 + len(e_a_list))

    @staticmethod
    def compare_edges(e_a, e_b):
        score = 0
        nbrs = {}
        for nbr in e_a:
            if nbr in e_b:
                e_a_list = [each for each in e_a[nbr] if not each.startswith('rev')]
                e_b_list = [each for each in e_b[nbr] if not each.startswith('rev')]
                nbrs[nbr] = {"hypo": e_a_list, "corpus": e_b_list}
                score += GSA.compare_edge_labels(e_a_list, e_b_list)
        return score / (1 + len(e_a)), nbrs

    @staticmethod
    def compare_graph(g1, g2):
        score = {}
        score['nodes'] = 0
        score['edges'] = 0

        match = {}
        for node in g2.adj:
            if node in g1.adj:
                score['nodes'] += 1
                a, b = GSA.compare_edges(g2.adj[node], g1.adj[node])
                score['edges'] += a
                match[node] = b

        return (score['nodes'] / (1 + len(g2.adj))) + score['edges'], match
