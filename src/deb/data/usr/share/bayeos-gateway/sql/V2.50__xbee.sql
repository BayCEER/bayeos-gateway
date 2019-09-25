drop table if exists notification;
drop table if exists contact;
drop table if exists service_state_log;

create table contact (
  id serial primary key,
  email text not null,
  domain_id bigint,
  CONSTRAINT fk_contact_domain FOREIGN KEY (domain_id)
      REFERENCES domain(id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
);

ALTER TABLE users
   ADD COLUMN contact_id bigint;

ALTER TABLE users
  ADD CONSTRAINT fk_users_contact FOREIGN KEY (contact_id)
      REFERENCES contact(id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE SET NULL;

CREATE TABLE notification
(
  id bigserial primary key,
  contact_id bigint NOT NULL,
  board_group_id bigint,
  board_id bigint, 
  CONSTRAINT fk_notification_contact FOREIGN KEY (contact_id)
      REFERENCES contact(id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT fk_notification_board_group FOREIGN KEY (board_group_id)
      REFERENCES board_group(id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT fk_notification_board FOREIGN KEY (board_id)
      REFERENCES board(id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE 
);
 

CREATE TABLE service_state_log
(
	id bigserial,
	last_state_change timestamptz not null default now(),
	last_check_time timestamptz not null default now(),
	domain_id bigint,
	board_group_id bigint,
	board_id bigint,		
	soft_state_count smallint not null default 0,	
	hard_state SMALLINT NOT NULL DEFAULT 0,
	CONSTRAINT pk_service_state_log PRIMARY KEY (id),
	CONSTRAINT fk_service_state_log_board FOREIGN KEY (board_id)
      REFERENCES board (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE cascade,
    CONSTRAINT fk_service_state_log_domain FOREIGN KEY (domain_id)
      REFERENCES domain (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE cascade,
    CONSTRAINT fk_service_state_log_board_group FOREIGN KEY (board_group_id)
      references board_group (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE cascade
);


-- Fixed issue #49  
CREATE OR REPLACE FUNCTION real_value(
    adc double precision,
    id bigint)
  RETURNS real AS
$BODY$
	use strict;
	if (! defined $_[1]) {
		return $_[0];
	}
	if(! exists $_SHARED{"spline$_[1]"}){
		spi_exec_query("select init_spline($_[1])");
	}
	
	if (@{$_SHARED{"spline$_[1]"}->[0]} > 0){
		return $_SHARED{"spline$_[1]"}->evaluate($_[0]);
	} else {
		return $_[0];
	}
	$BODY$
  LANGUAGE plperlu VOLATILE
  COST 100;



