for file in ../results/*
do
	corpus=$(basename $file)
	printf "\n$corpus\n"
 	python csvfy_result.py "$file/ARC-Challenge-Test_qapredictions_dgem.jsonl" "all/$corpus.tsv"
done