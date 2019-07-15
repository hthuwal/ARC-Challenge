# Attempting to use bidirsearch somehow with the graphs produced using [stuffie](../openie/stuffie).

# BidirSearch

Bidirectional Search algorithm for keyword search on graphs based on the paper by Kacholia et al.

## How to Use:
* Check `conf/config.example.properties` file for details on the config properties to be initialized. Change the `conf/config.properties` file to point to paths of the required files.
* The `main.BidirSearchExample` file is an example file on how to use the `search.BidirSearch` class. It prompts the user to enter a query and execute. An example query is of the form: "Angelina_Jolie Brad_Pitt" without the inverted commas. The number of answers computed is currently set to 5. It can be changed as per requirement.
* This is a maven project having its `pom.xml`. So one can simply clone the repository and execute `mvn clean compile install` to get the executable jar file in target/ folder. You may then run the jar file from anywhere using `java -jar <jar-file>`. Ensure that the `conf/` folder is in the same folder as the jar and the paths to the required files are correct in the config.properties file.
* The tree results displayed are in a customized format. You may edit the `utils.MyTreeResult` class to print them as per requirement.

## Contact:
You may reach me over email at madhulikam@cse.iitd.ac.in for any further questions.