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



