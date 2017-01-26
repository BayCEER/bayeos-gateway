#!/usr/bin/env bash
set -e

PRG="$0"
INSTALLDIR=`dirname "$PRG"`

if ! psql -c '\q' xbee > /dev/null 2>&1
then 
 echo "Create database and user ..."
 createuser -r -S -D xbee 2>&1
 psql -c "ALTER ROLE xbee PASSWORD '0b64debf8fae4239a7ca845f39878a3d'" 2>&1
 createdb -O xbee -E UTF-8 xbee 2>&1
else 
 echo "Drop liquibase and add flyway tables if not exist"
 psql -f $INSTALLDIR/sql/migrate-liquibase.sql xbee
fi

psql -c "ALTER ROLE xbee SUPERUSER" 2>&1
java -Djava.security.egd=file:/dev/../dev/urandom -cp $INSTALLDIR/lib/*:$INSTALLDIR/drivers/* org.flywaydb.commandline.Main migrate
psql -c "ALTER ROLE xbee NOSUPERUSER" 2>&1
