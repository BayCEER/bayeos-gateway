-- Create new virtual channel types 
alter table virtual_channel add column event text;

update virtual_channel set event = 'insert';

alter table virtual_channel alter column event set default 'insert';

alter table virtual_channel alter column event set not null;

alter table virtual_channel add constraint virtual_table_chk_event 
   check ( event in ('insert','calculate') );

CREATE OR REPLACE FUNCTION obs_insert()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$  
declare 
_id int4;
begin
 select id into _id from observation where result_time = NEW.result_time and channel_id = NEW.channel_id;
 if found then
  delete from observation where id = _id;             
 end if;         
 return NEW;
end;
$function$;   

CREATE TABLE observation_out (
	id bigserial PRIMARY KEY NOT NULL,
	channel_id int4 REFERENCES channel(id) ON DELETE CASCADE NOT NULL,
	result_time timestamptz NOT NULL,
	result_value float4 NOT NULL,
	insert_time timestamptz NULL DEFAULT now()
);

CREATE OR REPLACE FUNCTION create_vc_channel()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$  
declare 
_e boolean;
begin
 select exists(select 1 from channel c join board b on b.id = c.board_id where b.id = NEW.board_id and c.nr = NEW.nr) into _e;
 if not _e then    
  insert into channel (board_id, nr) values (NEW.board_id, NEW.nr);          
 end if;         
 return NEW;
end;
$function$;

create trigger trg_vc_upsert before insert or update on virtual_channel for each row execute function create_vc_channel();


