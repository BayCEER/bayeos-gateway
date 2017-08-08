DROP RULE IF EXISTS observation_inserts ON observation;

CREATE OR REPLACE FUNCTION obs_insert()
  RETURNS trigger AS
$BODY$  
declare 
_e boolean;
begin
 select exists(select 1 from observation where result_time = NEW.result_time and channel_id = NEW.channel_id) into _e;
 if _e then
  return null;
 else 
  return NEW;          
 end if;         
end;
$BODY$
LANGUAGE plpgsql VOLATILE
COST 100;

DROP TRIGGER IF EXISTS trg_obs_inserts on observation;

CREATE TRIGGER trg_obs_inserts
  BEFORE INSERT 
  ON observation
  FOR EACH ROW
  EXECUTE PROCEDURE obs_insert();
