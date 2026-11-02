drop table if exists upload;

create table upload (
  domain_id bigint,
  id serial primary key,
  uuid uuid not null,
  name text not null,
  size bigint not null,
  upload_time timestamptz not null default now(),
  user_name text not null,
  CONSTRAINT fk_upload_domain FOREIGN KEY (domain_id)
      REFERENCES domain(id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
);