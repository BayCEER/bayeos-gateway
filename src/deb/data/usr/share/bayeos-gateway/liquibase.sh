#!/bin/bash
echo "Running liquibase command .."
java -jar liquibase.jar \
--driver=org.postgresql.Driver \
--classpath=postgresql-9.0-801.jdbc4.jar \
--changeLogFile=changelog.xml \
--url="jdbc:postgresql://localhost/xbee" \
--username="xbee" \
--password="0b64debf8fae4239a7ca845f39878a3d" \
$1
echo "Finished."