import json
import csv
import sys

count = 0
file = sys.argv[1]
output = sys.argv[2]
with open(file, "r") as in_file, open("results/" + output, "w") as out_file:
    csvw = csv.writer(out_file)
    for line in in_file:
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

        final['support_A'] = options_support['A'] if 'A' in options_text else ""
        final['support_B'] = options_support['B'] if 'B' in options_text else ""
        final['support_C'] = options_support['C'] if 'C' in options_text else ""
        final['support_D'] = options_support['D'] if 'D' in options_text else ""
        final['support_E'] = options_support['E'] if 'E' in options_text else ""

        final['correct_answer'] = correct_answer
        final['predicted_answers'] = selected_answer
        final['score'] = line['question_score']

        if count == 0:
            csvw.writerow(final.keys())
        count += 1
        csvw.writerow(final.values())
        print("\r%d" % count, end="")
