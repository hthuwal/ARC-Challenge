#!/usr/bin/env bash

### Function to monitor the number of lines in the output files
### Untill all open-ie tmux windows are closed
function monitor()
{
	num_windows=$(tmux list-sessions | awk '{print $2}')
	initial_number_of_tmux_windows=$2
	while [[ "$num_windows" -ne "$initial_number_of_tmux_windows" ]]; do
		clear
		echo "$num_windows Running"
		for file in "$1"/*
		do
			count=$(wc -l "$file" 2> /dev/null)
			echo "$count"
		done
		num_windows=$(tmux list-sessions | awk '{print $2}')
		sleep 1
	done
	
}

if [[ $# -eq 0 ]] ; then
    echo 'Please give two arguments to the script: [input_filename] [output_filename].'
    exit 0
fi


TMP_DIR=/tmp/dair-iitd/large_corpus
TMP_OUT=/tmp/dair-iitd/out
initial_number_of_tmux_windows=$(tmux list-sessions | awk '{print $2}')
echo "$initial_number_of_tmux_windows"

rm -rf $TMP_DIR $TMP_OUT
mkdir -p $TMP_DIR $TMP_OUT

if ! [ -z $3 ]
then
	num_process=$3
else
	num_cores=$(grep ^cpu\\scores /proc/cpuinfo | uniq |  awk '{print $4}')
	num_process=$(($num_cores / 2))
fi
# num_virtual_cores=$(grep -c ^processor /proc/cpuinfo)

file_size=$(stat --printf="%s" "$1")
file_size=$(($file_size / 1024))
split_size=$(($file_size / $num_process))

echo -e "Splitting the data into small $split_size KB files\n"

time split -b $split_size"k" --numeric-suffixes $1 ${TMP_DIR}/small_

num_files=$(ls ${TMP_DIR}/ | wc -l)
echo "Splitted into $num_files"

for file in "$TMP_DIR"/*
do
	part=$(basename "$file")
	tmux new-window -n:"$part" java -jar openie-assembly.jar -b --split --ignore-errors "$file" "${TMP_OUT}/$part"
	echo $!
done

monitor "$TMP_OUT" "$initial_number_of_tmux_windows"

touch "$2"
for file in "$TMP_OUT"/*
do
	cat "$file" >> "$2"
done
