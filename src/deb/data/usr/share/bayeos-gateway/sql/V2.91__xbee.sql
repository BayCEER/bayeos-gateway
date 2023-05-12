CREATE OR REPLACE FUNCTION obs_insert()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$  
declare 
_id bigint;
begin
 select id into _id from observation where result_time = NEW.result_time and channel_id = NEW.channel_id;
 if FOUND then
  delete from observation where id = _id;             
 end if;         
 return NEW;
end;
$function$;   