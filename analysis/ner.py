from nltk.tag import StanfordNERTagger, StanfordPOSTagger
from nltk.tokenize import word_tokenize
from nltk.corpus import stopwords
import json
from tqdm import tqdm
import re

sp = StanfordPOSTagger('/home/harish/dell_storage/MTP/stanford-postagger-full-2018-02-27/models/english-bidirectional-distsim.tagger',
                       '/home/harish/dell_storage/MTP/stanford-ner-2018-02-27/stanford-ner-3.9.1.jar',
                       encoding='utf-8')

class3 = '/home/harish/dell_storage/MTP/stanford-ner-2018-02-27/classifiers/english.all.3class.distsim.crf.ser.gz'
class7 = '/home/harish/dell_storage/MTP/stanford-ner-2018-02-27/classifiers/english.muc.7class.distsim.crf.ser.gz'

st = StanfordNERTagger(class7,
                       '/home/harish/dell_storage/MTP/stanford-ner-2018-02-27/stanford-ner-3.9.1.jar',
                       encoding='utf-8')
stop_words = set(stopwords.words('english')) | set([".", ","])
regex = re.compile('[^a-zA-Z]')

count = 0


with open("ARC-Challenge-Test_qapredictions_dgem_default.jsonl", "r") as in_file, open("results/named_entities.txt", "w") as out_file:
    data = []
    for line in tqdm(in_file):
        line = json.loads(line)
        question = line['question']['stem']
        correct_answer = line['answerKey']
        selected_answer = line['selected_answers']
        options_text = {}
        options_support = {}

        for choice in line['question']['choices']:
            label = choice['label']
            if label not in options_text:
                options_text[label] = choice['text']
            if label not in options_support:
                options_support[label] = ""
            if choice['support'] != "":
                options_support[label] += (choice['support']['text'] + "||")

        final = {}
        final['Question'] = question
        final['A'] = options_text['A'] if 'A' in options_text else ""
        final['B'] = options_text['B'] if 'B' in options_text else ""
        final['C'] = options_text['C'] if 'C' in options_text else ""
        final['D'] = options_text['D'] if 'D' in options_text else ""
        final['E'] = options_text['E'] if 'E' in options_text else ""

        for key in final:
            text = final[key]
            tokenized_text = word_tokenize(text.lower())
            data = data + tokenized_text

    cleaned_data = [regex.sub('', token) for token in data if token not in stop_words]
    classified_data = st.tag(cleaned_data)
    classified_data = set([token[0] for token in classified_data])

    out_file.write("\n".join(classified_data))
