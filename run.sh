function run_dgem()
{
	corpus="$1"
	retrain="$2"
	bash scripts/evaluate_solver.sh data/ARC-V1-Feb2018-2/ARC-Challenge/ARC-Challenge-Test.jsonl data/ARC-V1-Models-Feb2018/dgem "$corpus" "$2"
}

run_dgem $1 $2