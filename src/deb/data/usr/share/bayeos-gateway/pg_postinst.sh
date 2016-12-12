#!/usr/bin/env bash
set -e

PRG="$0"
INSTALLDIR=`dirname "$PRG"`

if ! psql -c '\q' ${db.user} > /dev/null 2>&1
then 
 echo "Create database and user ..."
 createuser -r -S -D ${db.user} 2>&1
 psql -c "ALTER ROLE ${db.user} PASSWORD '${db.password}'" 2>&1
 createdb -O ${db.user} -E UTF-8 ${db.user} 2>&1
else 
 echo "Drop liquibase and add flyway tables if not exist"
 psql -f $INSTALLDIR/sql/migrate-liquibase.sql ${db.user}
fi

psql -c "ALTER ROLE ${db.user} SUPERUSER" 2>&1
echo "Update database ..."
/usr/bin/java -Djava.security.egd=file:/dev/../dev/urandom -cp "$INSTALLDIR/lib/*" org.flywaydb.commandline.Main migrate
psql -c "ALTER ROLE ${db.user} NOSUPERUSER" 2>&1