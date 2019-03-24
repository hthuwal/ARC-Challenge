import spacy
import json

from tqdm import tqdm
# from nltk.tag import StanfordNERTagger
# from nltk.tokenize import word_tokenize
# from nltk.corpus import stopwords

nlp = spacy.load('en_core_web_lg')

QUESTIONS_FILE = "../data/ARC-V1-Feb2018-2/ARC-Challenge/ARC-Challenge-Test.jsonl"


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
    return sentences


def ner_using_spacy(sentences):
    entities = []
    for sentence in tqdm(sentences, ascii=True):
        ent = list(nlp(sentence.lower()).ents)
        ent = [ent.text for ent in entities]
        if ent:
            entities.append(ent)
    return entities


print("Collecting Sentences...")
sentences = get_sentences()

print("NER Using Spacy...")
print(ner_using_spacy(sentences))
