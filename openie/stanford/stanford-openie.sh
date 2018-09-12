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

echo -e "Splitting the data into small 10kb files\n"

if [ "$(uname)" == "Darwin" ]; then
    gsplit -b 10k --numeric-suffixes $1 ${TMP_DIR}/small_
else
    time split -b 10k --numeric-suffixes $1 ${TMP_DIR}/small_
fi

num_files=$(ls ${TMP_DIR}/ | wc -l)
echo -e "Splitted $1 into $num_files 10kb files\n"

echo -e "Adding small file_names to a file.\n"
file_list="/tmp/openie/file_list.txt"

find ${TMP_DIR}/ > "$file_list"
sed '1d' "$file_list" > tmpfile; mv tmpfile "$file_list"

if ! [ -f "$2" ]; then
	if [ "$3" == "coref" ]; then
		echo "With Anaphora Resolution"
		time java -mx30g -cp "stanford-corenlp/*" edu.stanford.nlp.naturalli.OpenIE -threads 16 -resolve_coref true -annotators tokenize,ssplit,pos,lemma,ner,depparse,natlog,coref,openie -filelist "$file_list" -format ollie > "$2"
	else
		echo "Without Anaphora Resolution"
		time java -mx30g -cp "stanford-corenlp/*" edu.stanford.nlp.naturalli.OpenIE -threads 16 -filelist "$file_list" -format ollie > "$2"
	fi
else
	echo "$2 already exists"
fi
