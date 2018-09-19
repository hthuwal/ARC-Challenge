import nltk
import re
from subprocess import Popen
from graph import Graph
import os
import sys
from stanfordcorenlp import StanfordCoreNLP
import json

JAVA_BIN_PATH = 'java'
STANFORD_IE_FOLDER = 'stanford-corenlp'
BLANK_STR = "___"


# Create a hypothesis statement from the the input fill-in-the-blank statement and answer choice.
def create_hypothesis(fitb, choice):
    if ". " + BLANK_STR in fitb or fitb.startswith(BLANK_STR):
        choice = choice[0].upper() + choice[1:]
    else:
        choice = choice.lower()
    # Remove period from the answer choice, if the question doesn't end with the blank
    if not fitb.endswith(BLANK_STR):
        choice = choice.rstrip(".")
    # Some questions already have blanks indicated with 2+ underscores
    hypothesis = re.sub("__+", choice, fitb)
    return hypothesis


# Identify the wh-word in the question and replace with a blank
def replace_wh_word_with_blank(question_str: str):
    wh_word_offset_matches = []
    wh_words = ["which", "what", "where", "when", "how", "who", "why"]
    for wh in wh_words:
        # Some Turk-authored SciQ questions end with wh-word
        # E.g. The passing of traits from parents to offspring is done through what?
        m = re.search(wh + "\?[^\.]*[\. ]*$", question_str.lower())
        if m:
            wh_word_offset_matches = [(wh, m.start())]
            break
        else:
            # Otherwise, find the wh-word in the last sentence
            m = re.search(wh + "[ ,][^\.]*[\. ]*$", question_str.lower())
            if m:
                wh_word_offset_matches.append((wh, m.start()))

    # If a wh-word is found
    if len(wh_word_offset_matches):
        # Pick the first wh-word as the word to be replaced with BLANK
        # E.g. Which is most likely needed when describing the change in position of an object?
        wh_word_offset_matches.sort(key=lambda x: x[1])
        wh_word_found = wh_word_offset_matches[0][0]
        wh_word_start_offset = wh_word_offset_matches[0][1]
        # Replace the last question mark with period.
        question_str = re.sub("\?$", ".", question_str.strip())
        # Introduce the blank in place of the wh-word
        fitb_question = (question_str[:wh_word_start_offset] + BLANK_STR +
                         question_str[wh_word_start_offset + len(wh_word_found):])
        # Drop "of the following" as it doesn't make sense in the absence of a multiple-choice
        # question. E.g. "Which of the following force ..." -> "___ force ..."
        return fitb_question.replace(BLANK_STR + " of the following", BLANK_STR)
    elif re.match(".*[^\.\?] *$", question_str):
        # If no wh-word is found and the question ends without a period/question, introduce a
        # blank at the end. e.g. The gravitational force exerted by an object depends on its
        return question_str + " " + BLANK_STR
    else:
        # If all else fails, assume "this ?" indicates the blank. Used in Turk-authored questions
        # e.g. Virtually every task performed by living organisms requires this?
        return re.sub(" this[ \?]", " ___ ", question_str)


def stanford_ie(string, coref=False):
    tmp_file = "/tmp/hypo.txt"
    out = "/tmp/hypo_out"
    with open(tmp_file, "w") as f:
        f.write(string)

    absolute_path_to_script = os.path.dirname(os.path.realpath(__file__)) + '/'
    command = 'cd {};'.format(absolute_path_to_script)
    if coref:
        command += '{} -mx4g -cp "stanford-corenlp/*" ' \
                   'edu.stanford.nlp.naturalli.OpenIE -resolve_coref true -annotators tokenize,ssplit,pos,lemma,ner,depparse,natlog,coref,openie {} -format ollie > {}'. \
            format(JAVA_BIN_PATH, tmp_file, out)
    else:
        command += '{} -mx4g -cp "stanford-corenlp/*" ' \
                   'edu.stanford.nlp.naturalli.OpenIE {} -format ollie > {}'. \
            format(JAVA_BIN_PATH, tmp_file, out)

    print('Executing command = {}'.format(command))
    java_process = Popen(command, stdout=sys.stderr, shell=True)
    java_process.wait()

    assert not java_process.returncode, 'ERROR: Call to stanford_ie exited with a non-zero code status.'

    return Graph(out)


class StanfordNLP:
    def __init__(self, host='http://localhost', port=9000, coref=False):
        self.nlp = StanfordCoreNLP(host, port=port,
                                   timeout=30000)  # , quiet=False, logging_level=logging.DEBUG)
        self.props = {
            'annotators': 'natlog,openie',
            'pipelineLanguage': 'en',
            'openie.format': 'ollie'
        }
        if coref:
            self.props['annotators'] = 'natlog,openie,coref'

    def word_tokenize(self, sentence):
        return self.nlp.word_tokenize(sentence)

    def pos(self, sentence):
        return self.nlp.pos_tag(sentence)

    def ner(self, sentence):
        return self.nlp.ner(sentence)

    def parse(self, sentence):
        return self.nlp.parse(sentence)

    def dependency_parse(self, sentence):
        return self.nlp.dependency_parse(sentence)

    def annotate(self, sentence):
        return json.loads(self.nlp.annotate(sentence, properties=self.props))

    @staticmethod
    def tokens_to_dict(_tokens):
        tokens = defaultdict(dict)
        for token in _tokens:
            tokens[int(token['index'])] = {
                'word': token['word'],
                'lemma': token['lemma'],
                'pos': token['pos'],
                'ner': token['ner']
            }
        return tokens


def stanford_ie_v2(string, coref=False):
    out = "/tmp/hypo_out"

    sNLP = StanfordNLP(coref=coref)
    ans = sNLP.annotate(string)

    with open(out, "w") as f:
        for each in ans['sentences']:
            if len(each['openie']) != 0:
                for triplet in each['openie']:
                    subj = triplet['subject']
                    pred = triplet['relation']
                    obj = triplet['object']
                    f.write("1.000: (%s; %s; %s)\n" % (subj, pred, obj))
    # assert not java_process.returncode, 'ERROR: Call to stanford_ie exited with a non-zero code status.'
    return Graph(out)

if __name__ == '__main__':
    sNLP = StanfordNLP()
    text = 'A blog post using Stanford CoreNLP Server. Visit www.khalidalnajjar.com for more details.'
    print("Annotate:", sNLP.annotate(text))
