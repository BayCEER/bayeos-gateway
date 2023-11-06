-- Types 

CREATE TYPE check_result AS (
	code int4,
	msg text);

CREATE TYPE time_value AS (
	result_time timestamptz,
	result_value float4);


CREATE TYPE time_value_flag AS (
	result_time timestamptz,
	result_value float4,
	flag numeric);

-- Functions 

CREATE OR REPLACE FUNCTION _float_median(_value anyarray)
 RETURNS double precision
 LANGUAGE plpgsql
AS $function$
declare
n bigint;
a float[];
begin
a = sort(_value);
n = array_upper(a,1);
if (n%2=0) then
 return 0.5*(a[n/2] + a[n/2+1]);	
else 
 return a[(n+1)/2];
end if;
end;$function$
;


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
$function$
;


CREATE OR REPLACE FUNCTION date_truncate(timestamp with time zone, interval)
 RETURNS timestamp with time zone
 LANGUAGE sql
AS $function$
select to_timestamp(
  floor(extract(epoch from $1)/extract(epoch from $2))*extract(epoch from $2)
  );
$function$
;

CREATE OR REPLACE FUNCTION get_board_status(_id bigint)
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
declare
 ret int;
begin
 select into ret max(GREATEST(channel.status_valid, get_completeness_status(get_channel_count(channel.id)))) from channel where channel.board_id = _id and not channel.exclude_from_nagios;
return ret;
end;
$function$
;

CREATE OR REPLACE FUNCTION get_channel_count(_id bigint)
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
declare
 ret int;
begin
select into ret get_channel_count(c.last_count, c.last_result_time, COALESCE(c.sampling_interval, b.sampling_interval), COALESCE(c.check_delay, b.check_delay, 0)) 
  from channel c join board b on c.board_id = b.id where c.id = _id; 
return ret;
end;
$function$
;

CREATE OR REPLACE FUNCTION get_channel_count(_counts integer, _lrt timestamp with time zone, _sampling_interval integer, _check_delay integer)
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
		DECLARE
			c int;
			id interval;			
		BEGIN
			if _sampling_interval is null then
				return null;
			end if;
			
			id:= _check_delay * interval '1 second';			
			if (_lrt < now()-id) then		
			   c:= ceil(_counts - extract(EPOCH from now()-id-_lrt)/_sampling_interval);
			else 
			   c:= _counts;
			end if;
			if (c < 0) THEN 
				c:= 0;
			end if;
			return c;
		END; 
		$function$
;

CREATE OR REPLACE FUNCTION get_channel_time_value_flag(_ctime timestamp with time zone, _channel_id bigint, _spline_id bigint, _afunction text, _ainterval text, _filter_critical_values boolean, _critical_min real, _critical_max real)
 RETURNS SETOF time_value_flag
 LANGUAGE plpgsql
AS $function$
declare
ret time_value_flag%rowtype;
rec record;
-- Function will return time - value - flag. 
-- If flag ==0 -> addByteRows, 
-- elsif flag<1 ->  upsertByteRows 
-- else ->  flag == 1 only "exported" -> ignore'
aggregate boolean;
begin
  aggregate = _afunction is not null and _ainterval is not null;  
  if (_ctime is null or _channel_id is null) then 
	return;
  end if;
  
  -- check if there are new values in the observation table
  select into rec channel_id from observation where channel_id=_channel_id and result_time<_ctime limit 1;
  if not found then
    return;
  end if;

  if (aggregate) then
    return query execute 'select date_truncate(result_time,interval ''' || _ainterval || ''')  as result_time, ' || _afunction || '(result_value)::real,avg(flag)::numeric from 
	(select result_time,get_result_value(result_value,$1,$2,$3,$4) as result_value,0::numeric as flag from observation
	where result_time < date_truncate($5, interval ''' || _ainterval || ''') and channel_id = $6
	union
	select result_time,get_result_value(result_value,$1,$2,$3,$4) as result_value,1 as flag from observation_cache
	where result_time < date_truncate($5, interval ''' || _ainterval || ''') 
	and result_time>= (select min(date_truncate(result_time, interval ''' || _ainterval || ''')) from observation 
	where result_time < date_truncate($5, interval ''' || _ainterval || ''') and channel_id = $6) and channel_id = $6) a where result_value != ''NaN''
	group by date_truncate(result_time, interval ''' || _ainterval || ''') order by 1' using _spline_id,_filter_critical_values,_critical_min, _critical_max, _ctime, _channel_id;	
      
  else     	
	return query execute 'select * from (select result_time,get_result_value(result_value,$1,$2,$3,$4) as result_value, 0::numeric as flag from observation where result_time < $5 and channel_id = $6) a where result_value != ''NaN'' order by 1' 
	using _spline_id,_filter_critical_values,_critical_min, _critical_max, _ctime, _channel_id;	
  end if;
return;
end;$function$
;

CREATE OR REPLACE FUNCTION get_completeness_msg(_count integer)
 RETURNS text
 LANGUAGE plpgsql
AS $function$
declare
 msg text;
begin
 if (_count is null) then
   msg = null;
  else
   msg = (_count * 10) || '% completeness'::text;
  end if;  
 return msg;
 end;
$function$
;

CREATE OR REPLACE FUNCTION get_completeness_status(_count integer)
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
declare
 ret int;
begin
  if (_count is null) then
   ret = null;
  elsif (_count > 8) then
   ret = 0;
  elsif (_count = 0) then
   ret = 2;
  else
   ret = 1;
  end if;
  return ret;
end;
$function$
;

CREATE OR REPLACE FUNCTION get_grafana_obs(_domain_id bigint, _path text, _interval text, _from timestamp with time zone, _to timestamp with time zone)
 RETURNS SETOF time_value
 LANGUAGE plpgsql
AS $function$
declare
cha record;
begin
select into cha c.id, c.spline_id, coalesce(c.sampling_interval, b.sampling_interval,1)*1000 as sampling_interval, coalesce(f.name,'avg') as function_name
  from channel_path cp join channel c on (c.id = cp.channel_id) 
  join board b on (b.id = c.board_id) 
  left join function f on (f.id = c.aggr_function_id)
  where cp.path = _path and (cp.domain_id = _domain_id OR _domain_id IS NULL);  
  if (cha.id is not null) 
  then
	if extract(epoch from _interval::interval) < cha.sampling_interval then	
		return query execute 'select result_time, real_value(result_value,$1) as result_value from all_observation where channel_id = $2 and result_time between $3 and $4' using cha.spline_id, cha.id, _from, _to;
	else	
		return query execute 'select date_truncate(result_time,interval ''' || _interval || ''') as result_time, ' || cha.function_name || '(real_value(result_value,$1))::real as result_value 
	from all_observation where channel_id = $2 and result_time between $3 and $4 group by date_truncate(result_time, interval ''' || _interval || ''')' using cha.spline_id, cha.id, _from, _to;
	end if;
  end if;
return;
end;$function$
;

CREATE OR REPLACE FUNCTION get_group_status(_id bigint)
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
declare
 ret int;
begin
 select into ret max(get_board_status(board.id)) from board where board.board_group_id = _id and not board.exclude_from_nagios;
return ret;
end;
$function$
;

CREATE OR REPLACE FUNCTION get_order_number(text)
 RETURNS text
 LANGUAGE plpgsql
AS $function$
declare
    out text;
    temp text;
    i int;
begin    
    out:='';
    temp:='';   
    i:=1;
    while i<=length($1) loop
      if substr($1,i,1)~'[0-9]' then
        temp:=temp||substr($1,i,1);
      else
        if temp>'' then
          out:=out||to_char(temp::int,'0999999');
          temp:='';
        end if;
        out:=out||substr($1,i,1);
          end if;
      i:=i+1;
    end loop;
    if temp>'' then
     out:=out||to_char(temp::int,'0999999');
    end if;    
    return out;
end;
$function$
;

CREATE OR REPLACE FUNCTION get_result_value(_result_value real, _spline_id bigint, _filter_critical_values boolean, _critical_min real, _critical_max real)
 RETURNS real
 LANGUAGE plpgsql
AS $function$
declare
res real;
begin
if _spline_id is null then
  res:=_result_value;
else
  res:=real_value(_result_value,_spline_id);
end if;

if _filter_critical_values then
 return case when res<_critical_min then 'NaN'::real when res>_critical_max then 'NaN'::real else res end;
else
  return res;
end if;
exception when numeric_value_out_of_range then
  Raise warning 'error captured';
return 'NaN'::real;
end
$function$
;

CREATE OR REPLACE FUNCTION init_spline(id integer)
 RETURNS void
 LANGUAGE plperlu
AS $function$use Math::Derivative;
	use Math::Spline;
	my $id=$_[0];
	my $plan=spi_prepare('select x,y from knot_point where spline_id=$1 order by x','INTEGER');
	my $res=spi_query_prepared($plan,$id);
	my @x=();
	my @y=();
	my $row;
	while (defined ($row = spi_fetchrow($res))) {
		push(@x,$row->{x});
		push(@y,$row->{y});
	}
	$_SHARED{"spline$id"}=new Math::Spline(\@x,\@y);
	$function$
;

CREATE OR REPLACE AGGREGATE median(pg_catalog.anyelement) (
	SFUNC = array_append,
	STYPE = anyarray,
	FINALFUNC = _float_median,
	FINALFUNC_MODIFY = READ_ONLY,
	INITCOND = '{}'
);

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
$function$
;

CREATE OR REPLACE FUNCTION real_value(adc double precision, id bigint)
 RETURNS real
 LANGUAGE plperlu
AS $function$
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
	$function$
;

CREATE OR REPLACE FUNCTION sort(anyarray)
 RETURNS anyarray
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	select array(select $1[i] from generate_series(array_lower($1,1),
	array_upper($1,1)) g(i) order by 1)
	$function$
;



-- Tables 
CREATE TABLE "domain" (
	id bigserial NOT NULL,
	"name" text NOT NULL,
	CONSTRAINT domain_name_key UNIQUE (name),
	CONSTRAINT domain_pkey PRIMARY KEY (id)
);

CREATE TABLE board_group (
	id bigserial NOT NULL,
	"name" text NOT NULL,
	domain_id int8 NOT null default 1,
	CONSTRAINT board_group_pkey PRIMARY KEY (id),
	CONSTRAINT board_group_ukey UNIQUE (domain_id, name),
	CONSTRAINT fk_board_group_domain FOREIGN KEY (domain_id) REFERENCES "domain"(id) ON DELETE CASCADE
);

CREATE TABLE influx_connection (
	domain_id int8 NOT NULL DEFAULT 1,
	id serial4 NOT NULL,
	name text NOT NULL,
	url text NOT NULL DEFAULT 'http://localhost:8086'::text,
	"token" text NOT NULL,
	bucket text NOT NULL DEFAULT 'bayeos'::text,
	is_default bool NOT NULL DEFAULT false,
	CONSTRAINT influx_connection_pkey PRIMARY KEY (id),
	CONSTRAINT influx_connection_uname UNIQUE (domain_id, name),
	CONSTRAINT fk_influx_connection_domain FOREIGN KEY (domain_id) REFERENCES "domain"(id) ON DELETE CASCADE
);

CREATE TABLE board (
	id bigserial NOT NULL,
	exclude_from_nagios bool NOT NULL DEFAULT false,
	filter_critical_values bool NOT NULL DEFAULT false,
	last_result_time timestamptz NULL,
	last_rssi int2 NULL,
	"export" bool not null DEFAULT false,
	origin text NOT NULL,
	"name" text NULL,
	sampling_interval int4 NULL,
	status_valid int4 NULL,
	board_group_id int4 NULL,
	check_delay int4 NULL,
	deny_new_channels bool NOT NULL DEFAULT false,
	domain_id int8 NOT null default 1,
	domain_id_created int8 NOT NULL,
	influx_measurement text NULL,
	influx_connection_id int4 NULL,		
	CONSTRAINT fk_board_influx_con FOREIGN KEY (influx_connection_id) REFERENCES influx_connection(id) ON DELETE SET NULL,
	CONSTRAINT board_pkey PRIMARY KEY (id),
	CONSTRAINT board_ukey UNIQUE (domain_id, origin),
	CONSTRAINT fk_board_board_group FOREIGN KEY (board_group_id) REFERENCES board_group(id) ON DELETE SET NULL,
	CONSTRAINT fk_board_domain FOREIGN KEY (domain_id) REFERENCES "domain"(id) ON DELETE CASCADE
);

CREATE TABLE contact (
	id serial4 NOT NULL,
	email text NOT NULL,
	domain_id int8 NOT null default 1,
	CONSTRAINT contact_pkey PRIMARY KEY (id),
	CONSTRAINT fk_contact_domain FOREIGN KEY (domain_id) REFERENCES "domain"(id) ON DELETE CASCADE
);

CREATE TABLE users (
	id bigserial NOT NULL,
	"version" int8 NULL,
	account_locked bool NOT NULL,
	"password" varchar(255) NULL,
	"name" varchar(255) NOT NULL,
	"role" text NOT NULL,
	domain_id int8 NOT null default 1,
	first_name text NULL,
	last_name text NULL,
	contact_id int8 NULL,
	CONSTRAINT users_pkey PRIMARY KEY (id),
	CONSTRAINT users_ukey UNIQUE (domain_id, name),
	CONSTRAINT fk_users_contact FOREIGN KEY (contact_id) REFERENCES contact(id) ON DELETE SET NULL,
	CONSTRAINT fk_users_domain FOREIGN KEY (domain_id) REFERENCES "domain"(id) ON DELETE CASCADE
);


CREATE TABLE board_command (
	id bigserial NOT NULL,
	kind int4 NOT NULL,
	payload bytea NOT NULL,
	description text NULL,
	response bytea NULL,
	insert_time timestamptz NOT NULL,
	response_time timestamptz NULL,
	user_id int8 NOT NULL,
	board_id int8 NOT NULL,
	status int4 NULL,
	CONSTRAINT board_command_pkey PRIMARY KEY (id),
	CONSTRAINT chk_board_command_kind CHECK ((kind >= 0)),
	CONSTRAINT uk_board_command_board_kind UNIQUE (board_id, kind),
	CONSTRAINT fk_board_command_board FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE,
	CONSTRAINT fk_board_command_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE "comment" (
	id bigserial NOT NULL,
	"content" text NOT NULL,
	insert_time timestamp NOT NULL,
	user_id int8 NOT NULL,
	CONSTRAINT comment_pkey PRIMARY KEY (id),
	CONSTRAINT fk_comment_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE board_comment (
	board_comments_id int8 NULL,
	comment_id int8 NOT NULL,
	CONSTRAINT board_comment_pkey PRIMARY KEY (comment_id),
	CONSTRAINT fk_board_comment_board FOREIGN KEY (board_comments_id) REFERENCES board(id) ON DELETE CASCADE,
	CONSTRAINT fk_board_comment_comment FOREIGN KEY (comment_id) REFERENCES "comment"(id)
);

CREATE TABLE board_template (
	id bigserial NOT NULL,
	"name" text NOT NULL,
	description text NULL,
	data_sheet text NULL,
	revision text NULL,
	last_updated timestamptz NULL,
	date_created timestamptz NULL,
	sampling_interval int4 NULL,
	check_delay int4 NULL,
	exclude_from_nagios bool NULL,
	filter_critical_values bool NULL,
	domain_id int8 NOT null default 1,
	deny_new_channels bool NOT NULL DEFAULT false,
	influx_measurement text NULL,
	CONSTRAINT board_template_pkey PRIMARY KEY (id),
	CONSTRAINT fk_board_template_domain FOREIGN KEY (domain_id) REFERENCES "domain"(id) ON DELETE CASCADE
);




CREATE OR REPLACE FUNCTION influx_default_connection()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$    
    begin
	    if (new.is_default) then 
    	    update influx_connection set is_default = false where domain_id = new.domain_id and id != new.id;
	    end if;
    return new;
    end;
$function$
;

create trigger trg_influx_default_connection before
insert
    or
update
    on
    public.influx_connection for each row execute function influx_default_connection();


CREATE TABLE spline (
	id bigserial NOT NULL,
	"name" text NOT NULL,
	domain_id int8 NOT null default 1,
	CONSTRAINT spline_pkey PRIMARY KEY (id),
	CONSTRAINT spline_ukey UNIQUE (domain_id, name),
	CONSTRAINT fk_spline_domain FOREIGN KEY (domain_id) REFERENCES "domain"(id) ON DELETE CASCADE
);

CREATE TABLE knot_point (
	id bigserial NOT NULL,
	spline_id int8 NOT NULL,
	x float4 NOT NULL,
	y float4 NOT NULL,
	CONSTRAINT knot_point_pkey PRIMARY KEY (id),
	CONSTRAINT fk7b8a01197d458b24 FOREIGN KEY (spline_id) REFERENCES spline(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX knot_point_spline_id_x ON knot_point USING btree (spline_id, x);
CREATE INDEX knote_point_idx ON knot_point USING btree (id, x);

CREATE TABLE "interval" (
	id bigserial NOT NULL,
	"name" text NOT NULL,
	domain_id int8 NOT null default 1,
	CONSTRAINT interval_pkey PRIMARY KEY (id),
	CONSTRAINT interval_ukey UNIQUE (domain_id, name),
	CONSTRAINT fk_interval_domain FOREIGN KEY (domain_id) REFERENCES "domain"(id) ON DELETE CASCADE
);

CREATE TABLE unit (
	id bigserial NOT NULL,
	abbrevation varchar(255) NULL,
	"name" text NOT NULL,	
	domain_id int8 NOT null default 1,
	CONSTRAINT unit_pkey PRIMARY KEY (id),
	CONSTRAINT unit_ukey UNIQUE (domain_id, name),
	CONSTRAINT fk_unit_domain FOREIGN KEY (domain_id) REFERENCES "domain"(id) ON DELETE CASCADE
);

CREATE TABLE "function" (
	id bigserial NOT NULL,
	"name" text NOT NULL,
	domain_id int8 NOT null default 1,
	CONSTRAINT function_pkey PRIMARY KEY (id),
	CONSTRAINT function_ukey UNIQUE (domain_id, name),
	CONSTRAINT fk_function_domain FOREIGN KEY (domain_id) REFERENCES "domain"(id) ON DELETE CASCADE
);

CREATE TABLE channel (
	id bigserial NOT NULL,
	aggr_function_id int8 NULL,
	aggr_interval_id int8 NULL,
	board_id int8 NOT NULL,
	critical_max float4 NULL,
	critical_min float4 NULL,	
	exclude_from_nagios bool NOT NULL DEFAULT false,
	filter_critical_values bool NOT NULL DEFAULT false,
	"name" text NULL,
	last_count int4 NULL,
	last_result_time timestamptz NULL,
	nr text NOT NULL,
	phenomena varchar(255) NULL,
	sampling_interval int4 NULL,
	spline_id int8 NULL,
	status_valid int4 NULL,
	status_valid_msg varchar(255) NULL,
	unit_id int8 NULL,
	warning_max float4 NULL,
	warning_min float4 NULL,
	export bool NOT NULL DEFAULT true,
	last_result_value float4 NULL,
	check_delay int4 NULL,
	CONSTRAINT channel_board_nr_unique UNIQUE (board_id, nr),
	CONSTRAINT channel_pkey PRIMARY KEY (id),
	CONSTRAINT fk2c0b7d037d458b24 FOREIGN KEY (spline_id) REFERENCES spline(id),
	CONSTRAINT fk2c0b7d03a1cb9076 FOREIGN KEY (aggr_interval_id) REFERENCES "interval"(id),
	CONSTRAINT fk2c0b7d03b4ecbfb0 FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE,
	CONSTRAINT fk2c0b7d03c86784c4 FOREIGN KEY (unit_id) REFERENCES unit(id),
	CONSTRAINT fk2c0b7d03cc75d396 FOREIGN KEY (aggr_function_id) REFERENCES "function"(id)
);
CREATE INDEX channel_board_id_nr ON channel USING btree (board_id, nr);

CREATE TABLE channel_function (
	id serial4 NOT NULL,
	body text NULL,
	"name" text NULL,
	domain_id int8 null default 1,
	CONSTRAINT channel_function_pkey PRIMARY KEY (id),
	CONSTRAINT channel_function_ukey UNIQUE (domain_id, name),
	CONSTRAINT fk_channel_function_domain FOREIGN KEY (domain_id) REFERENCES "domain"(id) ON DELETE CASCADE
);

CREATE TABLE channel_function_parameter (
	id serial4 NOT NULL,
	"name" text NOT NULL,
	description text NULL,
	channel_function_id int8 NOT NULL,
	CONSTRAINT channel_function_parameter_pkey PRIMARY KEY (id),
	CONSTRAINT fk_channel_function_parameter_channel_function_id FOREIGN KEY (channel_function_id) REFERENCES channel_function(id) ON DELETE CASCADE
);

CREATE TABLE virtual_channel (
	id bigserial NOT NULL,
	nr text NOT NULL,
	channel_function_id int8 NOT NULL,
	board_id int8 NOT NULL,
	"event" text NOT NULL DEFAULT 'insert'::text,
	CONSTRAINT virtual_channel_pkey PRIMARY KEY (id),
	CONSTRAINT virtual_table_chk_event CHECK ((event = ANY (ARRAY['insert'::text, 'calculate'::text]))),
	CONSTRAINT fk_virtual_channel_board_id FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE,
	CONSTRAINT fk_virtual_channel_channelfunction_id FOREIGN KEY (channel_function_id) REFERENCES channel_function(id) ON DELETE CASCADE
);

CREATE TABLE channel_binding (
	id serial4 NOT NULL,
	nr text NULL,
	channel_function_param_id int8 NOT NULL,
	virtual_channel_id int8 NOT NULL,
	value float4 NULL,
	CONSTRAINT channel_binding_pkey PRIMARY KEY (id),
	CONSTRAINT fk_channel_binding_channel_function_param_id FOREIGN KEY (channel_function_param_id) REFERENCES channel_function_parameter(id) ON DELETE CASCADE,
	CONSTRAINT fk_channel_binding_virtual_channel_id FOREIGN KEY (virtual_channel_id) REFERENCES virtual_channel(id) ON DELETE CASCADE
);

CREATE TABLE channel_template (
	id bigserial NOT NULL,
	nr text NOT NULL,
	"name" text NOT NULL,
	phenomena varchar(255) NULL,
	unit_id int8 NULL,
	spline_id int8 NULL,
	aggr_interval_id int8 NULL,
	aggr_function_id int8 NULL,
	board_template_id int8 NOT NULL,
	channel_templates_idx int4 NULL,
	sampling_interval int4 NULL,
	critical_max float4 NULL,
	critical_min float4 NULL,
	warning_max float4 NULL,
	warning_min float4 NULL,
	check_delay int4 NULL,
	exclude_from_nagios bool NULL,
	filter_critical_values bool NULL,
	CONSTRAINT channel_template_board_nr_unique UNIQUE (board_template_id, nr),
	CONSTRAINT channel_template_pkey PRIMARY KEY (id),
	CONSTRAINT fk_ch_template_nr UNIQUE (board_template_id, nr),
	CONSTRAINT fk_ch_template_brd_template FOREIGN KEY (board_template_id) REFERENCES board_template(id) ON DELETE CASCADE,
	CONSTRAINT fk_ch_template_function FOREIGN KEY (aggr_function_id) REFERENCES "function"(id),
	CONSTRAINT fk_ch_template_interval FOREIGN KEY (aggr_interval_id) REFERENCES "interval"(id),
	CONSTRAINT fk_ch_template_spline FOREIGN KEY (spline_id) REFERENCES spline(id),
	CONSTRAINT fk_ch_template_unit FOREIGN KEY (unit_id) REFERENCES unit(id)
);

CREATE TABLE message (
	id bigserial NOT NULL,
	"content" text NOT NULL,
	result_time timestamptz NOT NULL,
	"type" varchar(255) NOT NULL,
	insert_time timestamptz NULL DEFAULT now(),
	board_id int8 NOT NULL,
	CONSTRAINT message_pkey PRIMARY KEY (id),
	CONSTRAINT fk_message_board FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE notification (
	id bigserial NOT NULL,
	contact_id int8 NOT NULL,
	board_group_id int8 NULL,
	board_id int8 NULL,
	CONSTRAINT notification_pkey PRIMARY KEY (id),
	CONSTRAINT fk_notification_board FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE,
	CONSTRAINT fk_notification_board_group FOREIGN KEY (board_group_id) REFERENCES board_group(id) ON DELETE CASCADE,
	CONSTRAINT fk_notification_contact FOREIGN KEY (contact_id) REFERENCES contact(id) ON DELETE CASCADE
);


CREATE TABLE observation (
	id bigserial NOT NULL,
	channel_id int4 NOT NULL,
	result_time timestamptz NOT NULL,
	result_value float4 NOT NULL,
	status_valid int4 NULL,
	insert_time timestamptz NULL DEFAULT now(),
	CONSTRAINT observation_pkey PRIMARY KEY (id),
	CONSTRAINT fk_obs_channel FOREIGN KEY (channel_id) REFERENCES channel(id) ON DELETE CASCADE
);
CREATE INDEX obs_cha_id_id ON observation USING btree (channel_id, id);
CREATE INDEX obs_cha_id_result_time ON observation USING btree (channel_id, result_time);
CREATE UNIQUE INDEX obs_id_cha_idx ON observation USING btree (id, channel_id);

-- Table Triggers

create trigger trg_obs_inserts before
insert
    on
    observation for each row execute function obs_insert();
    
    
CREATE TABLE observation_cache (
	id bigserial not NULL,
	channel_id int4 NOT NULL,
	result_time timestamptz NOT NULL,
	result_value float4 NOT NULL,
	status_valid int4 NULL,
	insert_time timestamptz NULL,
	CONSTRAINT observation_cache_pkey PRIMARY KEY (id),
	CONSTRAINT fk_observation_cache_cha FOREIGN KEY (channel_id) REFERENCES channel(id) ON DELETE CASCADE
);
CREATE INDEX obs_cache_ch_id_result_time ON observation_cache USING btree (channel_id, result_time);
CREATE UNIQUE INDEX obs_cache_id_cha_idx ON observation_cache USING btree (id, channel_id);
CREATE INDEX obs_cache_insert_time_idx ON observation_cache USING btree (insert_time);


CREATE TABLE observation_calc (
	id serial4 NOT NULL,
	channel_id int4 NOT NULL,
	result_time timestamptz NOT NULL,
	result_value float4 NOT NULL,
	CONSTRAINT pk_obs_calc PRIMARY KEY (id),
	CONSTRAINT fk_obs_calc_cha FOREIGN KEY (channel_id) REFERENCES channel(id) ON DELETE CASCADE
);

CREATE TABLE observation_out (
	id bigserial NOT NULL,
	channel_id int4 NOT NULL,
	result_time timestamptz NOT NULL,
	result_value float4 NOT NULL,
	insert_time timestamptz NULL DEFAULT now(),
	CONSTRAINT pk_obs_out PRIMARY KEY (id),
	CONSTRAINT fk_obs_out_channel FOREIGN KEY (channel_id) REFERENCES channel(id) ON DELETE CASCADE
);

CREATE TABLE persistent_logins (
	username text NOT NULL,
	series text NOT NULL,
	"token" text NOT NULL,
	last_used timestamp NOT NULL,
	CONSTRAINT persistent_logins_pkey PRIMARY KEY (series)
);

CREATE TABLE service_state_log (
	id bigserial NOT NULL,
	last_state_change timestamptz NOT NULL DEFAULT now(),
	last_check_time timestamptz NOT NULL DEFAULT now(),
	domain_id int8 NOT null default 1,
	board_group_id int8 NULL,
	board_id int8 NULL,
	soft_state_count int2 NOT NULL DEFAULT 0,
	hard_state int2 NOT NULL DEFAULT 0,
	CONSTRAINT pk_service_state_log PRIMARY KEY (id),
	CONSTRAINT fk_service_state_log_board FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE,
	CONSTRAINT fk_service_state_log_board_group FOREIGN KEY (board_group_id) REFERENCES board_group(id) ON DELETE CASCADE,
	CONSTRAINT fk_service_state_log_domain FOREIGN KEY (domain_id) REFERENCES "domain"(id) ON DELETE CASCADE
);

CREATE TABLE upload (
	domain_id int8 NOT null default 1,
	id serial4 NOT NULL,
	uuid uuid NOT NULL,
	"name" text NOT NULL,
	"size" int8 NOT NULL,
	upload_time timestamptz NOT NULL DEFAULT now(),
	import_time timestamptz NULL,
	import_message text NULL,
	user_id int8 NOT NULL,
	import_status int2 NULL,
	CONSTRAINT upload_pkey PRIMARY KEY (id),
	CONSTRAINT fk_upload_domain FOREIGN KEY (domain_id) REFERENCES "domain"(id) ON DELETE CASCADE,
	CONSTRAINT fk_upload_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

--  Views
-- all_observation source

CREATE OR REPLACE VIEW all_observation
AS SELECT observation.id,
    observation.channel_id,
    observation.result_time,
    observation.result_value,
    observation.status_valid
   FROM observation
UNION
 SELECT observation_cache.id,
    observation_cache.channel_id,
    observation_cache.result_time,
    observation_cache.result_value,
    observation_cache.status_valid
   FROM observation_cache;

-- channel_check source

CREATE OR REPLACE VIEW channel_check
AS SELECT channel.id,
    channel.board_id,
    GREATEST(channel.status_valid, get_completeness_status(get_channel_count(channel.id))) AS status,
    channel.status_valid,
    channel.status_valid_msg,
    get_completeness_msg(get_channel_count(channel.id)) AS status_complete_msg,
    get_completeness_status(get_channel_count(channel.id)) AS status_complete,
    channel.exclude_from_nagios
   FROM channel;


-- board_check source

CREATE OR REPLACE VIEW board_check
AS SELECT b.id,
    b.board_group_id,
    a.status
   FROM ( SELECT bo.id,
            max(chk.status) AS status
           FROM channel_check chk
             JOIN board bo ON bo.id = chk.board_id
          WHERE chk.exclude_from_nagios = false AND bo.exclude_from_nagios = false
          GROUP BY bo.id) a
     RIGHT JOIN ( SELECT board.id,
            board.board_group_id,
            board.exclude_from_nagios
           FROM board) b ON b.id = a.id
  ORDER BY b.id;


-- channel_detail source

CREATE OR REPLACE VIEW channel_detail
AS SELECT c.id,    
    c.spline_id,
    f.name AS f_name,
    i.name AS i_name,
    c.filter_critical_values,
    c.critical_min,
    c.critical_max
   FROM channel c
     LEFT JOIN function f ON f.id = c.aggr_function_id
     LEFT JOIN "interval" i ON i.id = c.aggr_interval_id;


-- channel_path source

CREATE OR REPLACE VIEW channel_path
AS SELECT board.domain_id,
    board.id AS board_id,
    board.name AS board_name,
    channel.id AS channel_id,
    channel.name AS channel_name,
    ((COALESCE(board_group.name, 'NO-GROUP'::text) || '/'::text) || (COALESCE(board.name, board.origin) || '/'::text)) || COALESCE(channel.name, channel.nr) AS path,
    board_group.name AS board_group
   FROM channel
     JOIN board ON board.id = channel.board_id
     LEFT JOIN board_group ON board.board_group_id = board_group.id;


-- group_check source

CREATE OR REPLACE VIEW group_check
AS SELECT board_group.id,
    max(board_check.status) AS status
   FROM board_check
     RIGHT JOIN board_group ON board_group.id = board_check.board_group_id
  GROUP BY board_group.id;


-- nagios_status source

CREATE OR REPLACE VIEW nagios_status
AS SELECT d.id AS domain_id,
    d.name AS domain_name,
    g.id AS group_id,
    g.name AS group_name,
    b.id AS board_id,
    b.origin AS board_origin,
    c.id AS channel_id,
    c.nr AS channel_nr,
    c.name AS channel_name,
    chk.status_complete,
    chk.status_complete_msg,
    chk.status_valid,
    chk.status_valid_msg
   FROM channel c
     JOIN channel_check chk ON c.id = chk.id
     JOIN board b ON b.id = c.board_id
     LEFT JOIN domain d ON d.id = b.domain_id
     LEFT JOIN board_group g ON g.id = b.board_group_id
  WHERE (chk.status_complete > 0 OR chk.status_valid > 0) AND NOT (b.exclude_from_nagios OR c.exclude_from_nagios)
  ORDER BY d.name, g.name, b.origin, c.nr;

  
-- inserts 
insert into domain(name) values ('DEFAULT');

INSERT INTO "interval" ("name",domain_id) VALUES
	 ('1 min',1),
	 ('10 min',1),
	 ('30 min',1),
	 ('1 hour',1),
	 ('15 min',1),
	 ('45 min',1),
	 ('2 hour',1),
	 ('3 hour',1),
	 ('4 hour',1),
	 ('1 day',1);
INSERT INTO "interval" ("name",domain_id) VALUES
	 ('1 week',1),
	 ('1 month',1);
INSERT INTO "unit"("name","abbrevation", domain_id)values
('Celsius','°C',1),
('Volt','V',1);
INSERT INTO "function" ("name",domain_id) VALUES
	 ('avg',1),
	 ('count',1),
	 ('median',1),
	 ('sum',1),
	 ('min',1),
	 ('max',1),
	 ('stddev',1),	 
	 ('variance',1);

INSERT INTO users ("version",account_locked,"password","name","role",domain_id,first_name,last_name,contact_id) VALUES
	 (0,true,'zZZSEKOCeaHyuf4TDN8/vubx+8g=','nagios','CHECK',1,NULL,NULL,NULL),
	 (0,false,'Yv371V0ZsqRnEQKte8oX2HX4IHo=','import','IMPORT',1,NULL,NULL,NULL),
	 (0,false,'8340uWG/1+Ca8V7rqyBRdQE8QTE=','root','USER',1,NULL,NULL,NULL);

INSERT INTO spline("name",domain_id) values ('Siemens 836',1);

INSERT INTO knot_point (spline_id,x,y) VALUES
	 (1,106.91,48.0),
	 (1,114.55,46.0),
	 (1,122.71,44.0),
	 (1,131.5,42.0),
	 (1,140.95,40.0),
	 (1,151.1,38.0),
	 (1,161.89,36.0),
	 (1,173.47,34.0),
	 (1,185.92,32.0),
	 (1,199.13,30.0);
INSERT INTO knot_point (spline_id,x,y) VALUES
	 (1,213.22,28.0),
	 (1,228.25,26.0),
	 (1,236.08,25.0),
	 (1,244.16,24.0),
	 (1,260.99,22.0),
	 (1,278.84,20.0),
	 (1,297.62,18.0),
	 (1,317.43,16.0),
	 (1,338.17,14.0),
	 (1,359.83,12.0);
INSERT INTO knot_point (spline_id,x,y) VALUES
	 (1,382.42,10.0),
	 (1,405.81,8.0),
	 (1,429.96,6.0),
	 (1,454.79,4.0),
	 (1,480.24,2.0),
	 (1,506.2,0.0),
	 (1,532.45,-2.0),
	 (1,558.95,-4.0),
	 (1,585.52,-6.0),
	 (1,612.06,-8.0);
INSERT INTO knot_point (spline_id,x,y) VALUES
	 (1,638.4,-10.0),
	 (1,664.27,-12.0),
	 (1,689.63,-14.0),
	 (1,714.38,-16.0),
	 (1,738.37,-18.0),
	 (1,761.5,-20.0),
	 (1,783.57,-22.0),
	 (1,804.59,-24.0),
	 (1,824.51,-26.0),
	 (1,843.29,-28.0);
INSERT INTO knot_point (spline_id,x,y) VALUES
	 (1,860.88,-30.0),
	 (1,877.17,-32.0),
	 (1,892.29,-34.0),
	 (1,906.25,-36.0),
	 (1,919.07,-38.0);





