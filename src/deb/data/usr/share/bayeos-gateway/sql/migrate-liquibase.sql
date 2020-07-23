--
-- Migrate to flyway 
--
DO
$do$
DECLARE
r record;
BEGIN
	CREATE TABLE IF NOT EXISTS schema_version (
		installed_rank integer PRIMARY KEY,
		version character varying(50),
		description character varying(200) NOT NULL,
		type character varying(20) NOT NULL,
		script character varying(1000) NOT NULL,
		checksum integer,
		installed_by character varying(100) NOT NULL,
		installed_on timestamp without time zone DEFAULT now() NOT NULL,
		execution_time integer NOT NULL,
		success boolean NOT NULL
	);
	ALTER TABLE schema_version OWNER TO xbee;
	UPDATE schema_version set checksum = 1861378496 where script like 'V1.99__xbee.sql';
	CREATE INDEX IF NOT EXISTS schema_version_s_idx ON schema_version USING btree (success);
	DROP TABLE IF EXISTS databasechangelog;
	DROP TABLE IF EXISTS databasechangeloglock;
END
$do$;
