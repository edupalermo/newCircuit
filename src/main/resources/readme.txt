set MAVEN_OPTS="-Xmx3G"
mvn clean install exec:java -Dexec.mainClass="org.circuit.Hive"


#connect 'jdbc:derby://localhost:1527/CircuitDb;create=true';
connect 'jdbc:derby://localhost:1527/CircuitDb';

mvn dependency:copy-dependencies




Start o database 
c:\java\db-derby-10.13.1.1-bin\bin\startNetworkServer
Execute queries in the database
connect 'jdbc:derby://localhost:1527/CircuitDb';
