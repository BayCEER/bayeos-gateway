#!/usr/bin/env bash
set -e
PRG="$0"
INSTALLDIR=`dirname "$PRG"`

if ! psql -c '\q' xbee > /dev/null 2>&1
then 
 echo "Create database and user ..."
 dropuser --if-exists xbee 2>&1 
 createuser -r -S -D xbee 2>&1
 psql -c "ALTER ROLE xbee PASSWORD '0b64debf8fae4239a7ca845f39878a3d'" 2>&1
 createdb -O xbee -E UTF-8 xbee 2>&1
fi

echo "Try to upgrade flyway table name"
psql -d xbee -c "alter table if exists schema_version rename TO flyway_schema_history;" 2>&1

psql -c "ALTER ROLE xbee SUPERUSER" 2>&1
java -Djava.security.egd=file:/dev/../dev/urandom -cp $INSTALLDIR/lib/*:$INSTALLDIR/drivers/* org.flywaydb.commandline.Main -url=jdbc:postgresql://localhost/xbee -driver=org.postgresql.Driver -user=xbee -password=0b64debf8fae4239a7ca845f39878a3d -locations=filesystem:$INSTALLDIR/sql migrate
psql -c "ALTER ROLE xbee NOSUPERUSER" 2>&1
