#!/usr/bin/env bash

# set -x
# trap read debug

if [[ $# -eq 0 ]] ; then
    echo 'Please give two arguments to the script: [input_filename] [output_filename].'
    exit 0
fi

# On Mac OS - brew install coreutils
# On linux: split

TMP_DIR=/tmp/openie/large_corpus

rm -rf $TMP_DIR
mkdir -p $TMP_DIR

if [ "$(uname)" == "Darwin" ]; then
    gsplit -b 10k --numeric-suffixes $1 ${TMP_DIR}/small_
else
    split -b 10k --numeric-suffixes $1 ${TMP_DIR}/small_
fi

file_list="/tmp/openie/file_list.txt"

find ${TMP_DIR}/small_* > "$file_list"


if ! [ -f "$2" ]; then
	if [ "$3" == "coref" ]; then
		echo "With Anaphora Resolution"
		java -mx4g -cp "stanford-corenlp/*" edu.stanford.nlp.naturalli.OpenIE -threads 16 -resolve_coref true -annotators tokenize,ssplit,pos,lemma,ner,depparse,natlog,coref,openie -filelist "$file_list" -format ollie
	else
		echo "Without Anaphora Resolution"
		java -mx4g -cp "stanford-corenlp/*" edu.stanford.nlp.naturalli.OpenIE -threads 16 -filelist "$file_list" -format ollie > "$2"
	fi
else
	echo "$2 already exists"
fi
