set MAVEN_OPTS="-Xmx3G"
mvn clean install exec:java -Dexec.mainClass="org.circuit.Hive"


#connect 'jdbc:derby://localhost:1527/CircuitDb;create=true';
connect 'jdbc:derby://localhost:1527/CircuitDb';

mvn dependency:copy-dependencies

