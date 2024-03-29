#!/bin/bash
# set -x
# trap read debug
# ----------------------------------------------------------------
# Evaluate an entailment model (DGEM/DecompAtt) on the QA dataset
# ----------------------------------------------------------------

input_file=$1
model_dir=$2

# name of the corpuses to be used -	 joined
run_name=$3
retrain=$4
if ! [ -d "results/$run_name" ]; then
	echo "Creating Directory $run_name"
	mkdir -p "results/$run_name"
fi 

if [ -z $model_dir ] ; then
  echo "USAGE: ./scripts/evaluate_solver.sh question_file.jsonl model_dir/"
  exit 1
fi

input_file_name=$(basename ${input_file})
input_file_prefix=${input_file_name%.jsonl}
echo $input_file
model_name=$(basename ${model_dir})

# File containing retrieved hits per choice (using the key "support")
input_file_with_hits="results/${run_name}/${input_file_prefix}_with_hits.jsonl"
# File containing the entailment examples per choice (using the keys "premise" and "hypothesis")
input_file_as_entailment="results/${run_name}/${input_file_prefix}_as_entailment.jsonl"
# File containing Open IE structure for the hypothesis (using the key "hypothesisStructure")
input_file_as_entailment_with_struct="results/${run_name}/${input_file_prefix}_as_entailment_with_struct.jsonl"
# File containing the entailment predictions per HIT and answer choice (using the key "score")
entailment_predictions="results/${run_name}/${input_file_prefix}_predictions_${model_name}.jsonl"
# File containing the QA predictions per question (using the key "selected_answers")
qa_predictions="results/${run_name}/${input_file_prefix}_qapredictions_${model_name}.jsonl"

# Collect hits from ElasticSearch for each question + answer choice
if [ ! -f ${input_file_with_hits} ]; then
  python arc_solvers/processing/add_retrieved_text.py \
    ${input_file} \
    ${input_file_with_hits}.$$
  mv ${input_file_with_hits}.$$ ${input_file_with_hits}
fi

# Convert the dataset into an entailment dataset i.e. add "premise" and "hypothesis" fields to
# the JSONL file where premise is the retrieved HIT for each answer choice and hypothesis is the
# question + answer choice converted into a statement.
if [ ! -f ${input_file_as_entailment} ]; then
  python arc_solvers/processing/convert_to_entailment.py \
    ${input_file_with_hits} \
    ${input_file_as_entailment}.$$
  mv ${input_file_as_entailment}.$$ ${input_file_as_entailment}
fi

# Add structure to the entailment data
if [ ! -f ${input_file_as_entailment_with_struct} ]; then
  java -Xmx8G -jar data/ARC-V1-Models-Feb2018/question-tuplizer.jar \
    ${input_file_as_entailment} \
    ${input_file_as_entailment_with_struct}.$$
  mv ${input_file_as_entailment_with_struct}.$$ ${input_file_as_entailment_with_struct}
fi

# Compute entailment predictions for each premise and hypothesis
if [ ! -f ${entailment_predictions} ]; then
  python arc_solvers/run.py predict \
    --output-file ${entailment_predictions}.$$ --silent \
    ${model_dir}/model.tar.gz ${input_file_as_entailment_with_struct}
  mv ${entailment_predictions}.$$ ${entailment_predictions}
fi

# Compute qa predictions by aggregating the entailment predictions for each question+answer
# choice (using max)
if [ ! -f ${qa_predictions} ]; then
  python arc_solvers/processing/evaluate_predictions.py \
    ${entailment_predictions} \
    ${input_file} \
    ${qa_predictions}.$$
  mv ${qa_predictions}.$$ ${qa_predictions}
fi

python arc_solvers/processing/calculate_scores.py ${qa_predictions}
