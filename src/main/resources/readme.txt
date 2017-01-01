set MAVEN_OPTS="-Xmx3G"
mvn clean install exec:java -Dexec.mainClass="org.circuit.Hive"


mvn dependency:copy-dependencies

