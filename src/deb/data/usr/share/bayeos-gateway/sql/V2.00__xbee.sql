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


