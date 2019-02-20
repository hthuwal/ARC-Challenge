rm parts/*.openie
rm parts/*.exceptions

echo "Compiling.."

mvn compile
cstatus=$?
echo "Running"

if [ $cstatus -eq 0 ]; then
    mvn exec:java -Dexec.mainClass=it.unibz.inf.stuffie.StuffieConsoleRunner
else
    echo "Compilation Failed.."
fi