CREATE TABLE IF NOT EXISTS observation_calc
(
  id serial NOT NULL,
  db_series_id integer NOT NULL,
  result_time timestamp with time zone NOT NULL,
  result_value real NOT NULL,
  CONSTRAINT pk_obs_calculated PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);