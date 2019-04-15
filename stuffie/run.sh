rm -rf parts
mkdir parts

echo "Splitting Source file into $1 components..."
time split -n $1 --numeric-suffixes "../data/ARC-V1-Feb2018-2/ARC_Corpus.txt" parts/part_

echo "Compiling.."

mvn compile
cstatus=$?
echo "Running"

if [ $cstatus -eq 0 ]; then
    mvn -X exec:java -Dexec.mainClass=it.unibz.inf.stuffie.StuffieConsoleRunner
else
    echo "Compilation Failed.."
fi