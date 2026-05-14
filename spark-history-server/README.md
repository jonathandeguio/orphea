# spark-history-server : modified for orphea

# Modifications :

* custom orphea logo
* custom css
* base url path to /orphea-shs 

# Taken care by Dockerfile but you can build as below 
## to build custom spark : ./dev/make-distribution.sh --tgz -Dhadoop.version=3.3.0 -DskipTests

Local build and test

./build/mvn -DskipTests clean package; ./bin/spark-class -Dspark.history.fs.logDirectory=file:///Users/commonaccount/orphea/spark-shs   org.apache.spark.deploy.history.HistoryServer

./bin/spark-class -Dspark.history.fs.logDirectory=file:///Users/commonaccount/orphea/spark-shs   org.apache.spark.deploy.history.HistoryServer 
