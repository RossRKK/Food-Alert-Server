cp libs/mysql-connector-java-5.1.6.jar build/libs/mysql-connector-java-5.1.6.jar
cd build/libs/
##export CLASSPATH=mysql-connector-java-5.1.6.jar:.

java -Dserver.port=$PORT $JAVA_OPTS -jar Food-Alert-Server-0.1.0.jar $PORT
