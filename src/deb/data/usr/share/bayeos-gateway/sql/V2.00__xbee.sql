ALTER TABLE users ADD COLUMN role text;
UPDATE users set role = substring(role.authority from 6) from role, user_role where role.id = user_role.role_id and user_role.user_id = users.id;
ALTER TABLE users ALTER COLUMN role SET NOT NULL;
ALTER TABLE users DROP COLUMN account_expired;
ALTER TABLE users DROP COLUMN enabled;
ALTER TABLE users DROP COLUMN password_expired;

drop table user_role;
drop table role;

-- Externalize Configuration to System Variables
ALTER TABLE export_job_config DROP COLUMN sleep_interval;
ALTER TABLE export_job_config DROP COLUMN enabled;
ALTER TABLE export_job_config DROP COLUMN password;
ALTER TABLE export_job_config DROP COLUMN records_per_bulk;
ALTER TABLE export_job_config DROP COLUMN url;
ALTER TABLE export_job_config DROP COLUMN user_name;
DROP TABLE delete_job_config;


-- Add missing inherited attributes
ALTER TABLE board_template ADD COLUMN exclude_from_nagios boolean;
ALTER TABLE board_template ADD COLUMN filter_critical_values boolean;

ALTER TABLE channel_template ADD COLUMN exclude_from_nagios boolean;
ALTER TABLE channel_template ADD COLUMN filter_critical_values boolean;

-- Custom Status Functions
CREATE OR REPLACE FUNCTION get_board_status(_id bigint)
  RETURNS integer AS
$BODY$
declare
 ret int;
begin
 select into ret max(GREATEST(channel.status_valid, get_completeness_status(get_channel_count(channel.id)))) from channel where channel.board_id = _id and not channel.exclude_from_nagios;
return ret;
end;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

CREATE OR REPLACE FUNCTION get_group_status(_id bigint)
  RETURNS integer AS
$BODY$
declare
 ret int;
begin
 select into ret max(get_board_status(board.id)) from board where board.board_group_id = _id and not board.exclude_from_nagios;
return ret;
end;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

ALTER TABLE board_comment DROP CONSTRAINT fk877c3e0628a1f209;
ALTER TABLE board_comment
  ADD CONSTRAINT fk_board_comment_board FOREIGN KEY (board_comments_id)
      REFERENCES board (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE board_comment DROP CONSTRAINT fk877c3e062f2e4b90;
ALTER TABLE board_comment
  ADD CONSTRAINT fk_board_comment_comment FOREIGN KEY (comment_id)
      REFERENCES comment (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;      

ALTER TABLE board_comment
  ADD CONSTRAINT board_comment_pkey PRIMARY KEY(comment_id);

     
ALTER TABLE comment DROP CONSTRAINT fk38a5ee5fd0b68b64;

ALTER TABLE comment
  ADD CONSTRAINT fk_comment_users FOREIGN KEY (user_id)
      REFERENCES users (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;
      
CREATE OR REPLACE FUNCTION get_grafana_obs(
    _path text,
    _interval text,
    _from timestamp with time zone,
    _to timestamp with time zone)
  RETURNS SETOF time_value AS
$BODY$
declare
b time_value%rowtype;
cha record;
begin
select into cha c.id, c.spline_id, COALESCE(c.sampling_interval, b.sampling_interval,1)*1000 AS sampling_interval, coalesce(f.name,'avg') as function_name
  from channel_path cp join channel c on (c.id = cp.channel_id) 
  join board b on (b.id = c.board_id) 
  left join function f on (f.id = c.aggr_function_id)
  where cp.path = _path;
  if extract(epoch from _interval::interval) < cha.sampling_interval then
	return query execute 'select result_time, real_value(result_value,$1) as result_value from all_observation where channel_id = $2 and result_time between $3 and $4' using cha.spline_id, cha.id, _from, _to;
  else
	return query execute 'select date_truncate(result_time,interval ''' || _interval || ''') as result_time, ' || cha.function_name || '(real_value(result_value,$1))::real as result_value 
	from all_observation where channel_id = $2 and result_time between $3 and $4 group by date_truncate(result_time, interval ''' || _interval || ''')' using cha.spline_id, cha.id, _from, _to;
  end if;
return;
end;$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;



