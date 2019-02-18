echo "Compiling.."

mvn compile

echo "Running"

mvn exec:java -Dexec.mainClass=it.unibz.inf.stuffie.StuffieConsoleRunner