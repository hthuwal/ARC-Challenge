import json
import re
import spacy

from nltk.corpus import stopwords
from nltk.tag import StanfordNERTagger
from nltk.tokenize import word_tokenize
from tqdm import tqdm

QUESTIONS_FILE = "../data/ARC-V1-Feb2018-2/ARC-Challenge/ARC-Challenge-Test.jsonl"
STANFORD_NER_Class7_JAR_FILE = "../bin/stanford-ner-2018-10-16/classifiers/english.all.3class.distsim.crf.ser.gz"
STANFORD_NER_JAR_FILE = "../bin/stanford-ner-2018-10-16/stanford-ner-3.9.2.jar"


stanford_tagger = StanfordNERTagger(
    STANFORD_NER_Class7_JAR_FILE,
    STANFORD_NER_JAR_FILE,
    encoding='utf-8',
)
stop_words = set(stopwords.words('english')) | set([".", ","])
regex = re.compile('[^a-zA-Z]')

nlp = spacy.load('en_core_web_lg')


def load_questions(file=QUESTIONS_FILE):
    questions = {}
    with open(file, "r") as in_file:
        for line in (in_file):
            line = json.loads(line)
            question = line['question']['stem']
            options = {}

            for choice in line['question']['choices']:
                label = choice['label']
                if label not in options:
                    options[label] = choice['text']

            questions[line['id']] = [question, options]
    return questions


def get_sentences():
    questions = load_questions()

    sentences = []
    for qid in tqdm(questions, ascii=True):
        ques_state = questions[qid][0]
        options = list(questions[qid][1].values())
        sentences.append(ques_state)
        sentences.extend(options)

    sentences = [sentence.lower() for sentence in sentences]
    return sentences


def ner_using_spacy(sentences):
    entities = []
    ignore = ['PERCENT', 'MONEY', 'ORDINAL', 'CARDINAL']
    for sentence in tqdm(sentences, ascii=True):
        ent = list(nlp(sentence).ents)
        ent = [each.text for each in ent if each.label_ not in ignore]
        entities.extend(ent)
    return entities


def ner_using_nltk_stanford(sentences):
    entities = []
    for sentence in tqdm(sentences, ascii=True):
        tokens = word_tokenize(sentence)
        tokens = [regex.sub('', token) for token in tokens]
        tokens = [token for token in tokens if token not in stop_words]
        ent = stanford_tagger.tag(tokens)
        ent = [each[0].strip() for each in ent if each[1] != 'O']
        entities.extend(ent)

    return entities


print("Collecting Sentences...")
sentences = get_sentences()

print("NER Using Spacy...")
entities_spacy = ner_using_spacy(sentences)

print("NER Using StanfordNERTagger and NLTK")
entities_stanford = ner_using_nltk_stanford(sentences[:5])

# entities = set(entities_spacy + entities_stanford)

print(entities_spacy)
print(len(entities_spacy))
