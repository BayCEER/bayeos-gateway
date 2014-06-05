#!/bin/bash
set -e
if ! psql -c '\q' xbee > /dev/null 2>&1
then 
 echo "Create database and user xbee"
 createuser -r -s -D xbee 2>&1
 createdb -O xbee xbee 2>&1
fi

psql -c "ALTER ROLE xbee PASSWORD '0b64debf8fae4239a7ca845f39878a3d'" 2>&1
psql -c "CREATE EXTENSION IF NOT EXISTS tablefunc SCHEMA public;" xbee 2>&1
psql -c "CREATE EXTENSION IF NOT EXISTS plperl SCHEMA pg_catalog;" xbee 2>&1
psql -c "CREATE EXTENSION IF NOT EXISTS plperlu SCHEMA pg_catalog;" xbee 2>&1

