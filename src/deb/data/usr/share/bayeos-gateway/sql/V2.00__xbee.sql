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

