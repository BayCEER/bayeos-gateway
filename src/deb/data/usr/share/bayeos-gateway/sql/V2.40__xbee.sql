-- Issue#34 Domain concept
create table "domain" (
	id bigserial not null ,
	name text not null,
	db_folder_id integer, 	
	constraint domain_name_key unique(name),
	constraint domain_pkey primary key (id)	
);

-- users 
alter table users add column domain_id bigint;
alter table users
  add constraint fk_users_domain foreign key (domain_id)
      references "domain" (id) match simple
      on update no action on delete cascade;  
alter table users drop constraint users_username_key;
alter table users add constraint users_ukey unique (domain_id,username);

-- board 
drop index board_origin_idx;
alter table board add column domain_id bigint;
alter table board
  add constraint fk_board_domain foreign key (domain_id)
      references "domain" (id) match simple
      on update no action on delete cascade;
alter table board drop constraint board_origin_key;
alter table board add constraint board_ukey unique(domain_id, origin);
alter table board add column domain_id_created bigint;

     
-- board_group
alter table board_group add column domain_id bigint;
alter table board_group
  add constraint fk_board_group_domain foreign key (domain_id)
      references "domain" (id) match simple
      on update no action on delete cascade;
alter table board_group add constraint board_group_ukey unique(domain_id, name);      
      
-- board_template      
alter table board_template add column domain_id bigint;
alter table board_template
  add constraint fk_board_template_domain foreign key (domain_id)
      references "domain" (id) match simple
      on update no action on delete cascade;
alter table board_template add constraint board_template_ukey unique(domain_id, name);      

-- function
alter table function add column domain_id bigint;
alter table function
  add constraint fk_function_domain foreign key (domain_id)
      references "domain" (id) match simple
      on update no action on delete cascade;
alter table function add constraint function_ukey unique(domain_id, name);      

-- interval
alter table interval add column domain_id bigint;
alter table interval
  add constraint fk_interval_domain foreign key (domain_id)
      references "domain" (id) match simple
      on update no action on delete cascade;
alter table interval add constraint interval_ukey unique(domain_id, name);      

-- channel_function
alter table channel_function add column domain_id bigint;
alter table channel_function
  add constraint fk_channel_function_domain foreign key (domain_id)
      references "domain" (id) match simple
      on update no action on delete cascade;
alter table channel_function add constraint channel_function_ukey unique(domain_id, name);      

-- spline
alter table spline add column domain_id bigint;
alter table spline
  add constraint fk_spline_domain foreign key (domain_id)
      references "domain" (id) match simple
      on update no action on delete cascade;
alter table spline add constraint spline_ukey unique(domain_id, name);

-- knotpoint
alter table knot_point drop constraint fk7b8a01197d458b24;
alter table knot_point
  add constraint fk7b8a01197d458b24 foreign key (spline_id)
      references spline (id) match simple
      on update no action on delete cascade;


-- unit
alter table unit add column domain_id bigint;
alter table unit
  add constraint fk_unit_domain foreign key (domain_id)
      references "domain" (id) match simple
      on update no action on delete cascade;
alter table unit add constraint unit_ukey unique(domain_id, name);

-- message 
alter table message add column domain_id bigint;
alter table message
  add constraint fk_message_domain foreign key (domain_id)
      references "domain" (id) match simple
      on update no action on delete cascade;
      
      
-- grafana
DROP VIEW channel_path;
CREATE OR REPLACE VIEW channel_path AS 
 SELECT board.domain_id, board.id AS board_id, board.name AS board_name, channel.id AS channel_id, channel.name AS channel_name,
    (COALESCE(board.name, board.origin) || '/'::text) || COALESCE(channel.name, channel.nr) AS path
   FROM channel, board WHERE board.id = channel.board_id;
      
drop function get_grafana_obs(text, text, timestamp with time zone, timestamp with time zone);      
create or replace function get_grafana_obs(
	_domain_id bigint, 
    _path text,
    _interval text,
    _from timestamp with time zone,
    _to timestamp with time zone)
  returns setof time_value as
$body$
declare
b time_value%rowtype;
cha record;
begin
select into cha c.id, c.spline_id, coalesce(c.sampling_interval, b.sampling_interval,1)*1000 as sampling_interval, coalesce(f.name,'avg') as function_name
  from channel_path cp join channel c on (c.id = cp.channel_id) 
  join board b on (b.id = c.board_id) 
  left join function f on (f.id = c.aggr_function_id)
  where cp.path = _path and cp.domain_id = _domain_id;
  if extract(epoch from _interval::interval) < cha.sampling_interval then
	return query execute 'select result_time, real_value(result_value,$1) as result_value from all_observation where channel_id = $2 and result_time between $3 and $4' using cha.spline_id, cha.id, _from, _to;
  else
	return query execute 'select date_truncate(result_time,interval ''' || _interval || ''') as result_time, ' || cha.function_name || '(real_value(result_value,$1))::real as result_value 
	from all_observation where channel_id = $2 and result_time between $3 and $4 group by date_truncate(result_time, interval ''' || _interval || ''')' using cha.spline_id, cha.id, _from, _to;
  end if;
return;
end;$body$
  language plpgsql volatile;
  
-- drop acl 
drop table if exists acl_entry;
drop table if exists acl_object_identity;
drop table if exists acl_sid;
drop table if exists acl_class;


-- channel
alter table channel drop constraint fk2c0b7d03b4ecbfb0;
alter table channel
  add constraint fk2c0b7d03b4ecbfb0 foreign key (board_id)
      references board (id) match simple
      on update no action on delete cascade;

-- issue 45: simplify board/tempalte attributes
create or replace view channel_detail as 
 select c.id,
    c.db_series_id,
    c.spline_id,
    f.name as f_name,
    i.name as i_name,
    c.filter_critical_values,
    c.critical_min,
    c.critical_max
   from channel c      
     left join function f on f.id = c.aggr_function_id
     left join "interval" i on i.id = c.aggr_interval_id;
alter table board drop column critical_min;
alter table board drop column critical_max;
alter table board drop column warning_min;
alter table board drop column warning_max;
alter table board_template drop column critical_min;
alter table board_template drop column critical_max;
alter table board_template drop column warning_min;
alter table board_template drop column warning_max;
alter table board_template add column deny_new_channels boolean;
UPDATE board_template SET deny_new_channels = false;
alter table board_template alter column deny_new_channels set not null;
alter table board_template alter column deny_new_channels set default false;

-- Minor harmonizations 
alter table virtual_channel  alter column id type bigint;
alter table users rename username  to name;


-- Changes to include domain information in nagios status 
DROP VIEW nagios_status;

CREATE OR REPLACE VIEW nagios_status AS 
 select 
    d.id as domain_id,
    d.name as domain_name,
    g.id AS group_id,
    g.name AS group_name,
    b.id as board_id,
    b.origin as board_origin,
    c.id AS channel_id,
    c.nr AS channel_nr,
    c.name AS channel_name,
    chk.status_complete,
    chk.status_complete_msg,
    chk.status_valid,
    chk.status_valid_msg
from channel c join channel_check chk on c.id = chk.id
join board b on b.id = c.board_id
left outer join domain d on d.id = b.domain_id
left outer join board_group g on g.id = b.board_group_id
where (chk.status_complete > 0 OR chk.status_valid > 0)
AND NOT (b.exclude_from_nagios OR c.exclude_from_nagios)
ORDER BY d.name, g.name, b.origin, c.nr;


-- issue #37: System statistics as $SYS Board 
DROP TABLE IF exists export_job_stat;

DROP FUNCTION if exists delete_obs(timestamp with time zone, bigint);

CREATE OR REPLACE FUNCTION delete_obs(
    ctime timestamp with time zone,
    maxid bigint)
  RETURNS bigint AS
$BODY$
declare
 counts bigint;
begin 
 create temporary table channel_detail_temp on commit drop as select * from channel_detail where db_series_id is not null;
 insert into observation_exp select o.* from observation o, channel_detail_temp c 
 where o.id<=maxid and c.id=o.channel_id and (
   (c.i_name is null and o.result_time<ctime) or 
   (c.i_name is not null and o.result_time< date_truncate(ctime, c.i_name::interval) )
 );

 delete from observation as o
 using channel_detail_temp c 
 where o.id<=maxid and c.id=o.channel_id and (
   (c.i_name is null and o.result_time<ctime) or 
   (c.i_name is not null and o.result_time< date_truncate(ctime, c.i_name::interval) )
 ); 
 GET DIAGNOSTICS counts = ROW_COUNT; 
return counts;
end;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;


 
 



