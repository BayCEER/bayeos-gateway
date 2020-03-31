alter table upload add column import_time timestamptz;
alter table upload add column import_message text;
alter table upload add column user_id bigint not null;
alter table upload add CONSTRAINT fk_upload_user FOREIGN KEY (user_id)
      REFERENCES users(id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;
alter table upload drop column user_name;
