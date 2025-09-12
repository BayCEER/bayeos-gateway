--
-- PostgreSQL database dump
-- pg_dump --exclude-table=databasechangelog --exclude-table=databasechangeloglock --inserts -Fp -f V1.99__xbee.sql xbee 
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plperl; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plperl WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plperl; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plperl IS 'PL/Perl procedural language';


--
-- Name: plperlu; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plperlu WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plperlu; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plperlu IS 'PL/PerlU untrusted procedural language';


--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- Name: tablefunc; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS tablefunc WITH SCHEMA public;


--
-- Name: EXTENSION tablefunc; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION tablefunc IS 'functions that manipulate whole tables, including crosstab';


SET search_path = public, pg_catalog;

--
-- Name: bayeos_observation; Type: TYPE; Schema: public; Owner: xbee
--

CREATE TYPE bayeos_observation AS (
	db_series_id integer,
	result_time timestamp with time zone,
	result_value real
);


ALTER TYPE public.bayeos_observation OWNER TO xbee;

--
-- Name: check_result; Type: TYPE; Schema: public; Owner: xbee
--

CREATE TYPE check_result AS (
	code integer,
	msg text
);


ALTER TYPE public.check_result OWNER TO xbee;

--
-- Name: time_value; Type: TYPE; Schema: public; Owner: xbee
--

CREATE TYPE time_value AS (
	result_time timestamp with time zone,
	result_value real
);


ALTER TYPE public.time_value OWNER TO xbee;

--
-- Name: time_value_flag; Type: TYPE; Schema: public; Owner: xbee
--

CREATE TYPE time_value_flag AS (
	result_time timestamp with time zone,
	result_value real,
	flag numeric
);


ALTER TYPE public.time_value_flag OWNER TO xbee;

--
--
-- Name: date_truncate(timestamp with time zone, interval); Type: FUNCTION; Schema: public; Owner: xbee
--

CREATE FUNCTION date_truncate(timestamp with time zone, interval) RETURNS timestamp with time zone
    LANGUAGE sql
    AS $_$
select to_timestamp(
  floor(extract(epoch from $1)/extract(epoch from $2))*extract(epoch from $2)
  );
$_$;


ALTER FUNCTION public.date_truncate(timestamp with time zone, interval) OWNER TO xbee;

--
-- Name: delete_obs(timestamp with time zone, bigint); Type: FUNCTION; Schema: public; Owner: xbee
--

CREATE FUNCTION delete_obs(ctime timestamp with time zone, maxid bigint) RETURNS void
    LANGUAGE plpgsql
    AS $$
declare
 cha record;
begin
 insert into observation_exp select o.* from observation o, channel_detail c 
 where o.id<=maxid and c.db_series_id is not null
 and c.id=o.channel_id and (
   (c.i_name is null and o.result_time<ctime) or 
   (c.i_name is not null and o.result_time< date_truncate(ctime, c.i_name::interval) )
 );

 delete from observation as o
 using channel_detail c 
 where o.id<=maxid and c.db_series_id is not null
 and c.id=o.channel_id and (
   (c.i_name is null and o.result_time<ctime) or 
   (c.i_name is not null and o.result_time< date_truncate(ctime, c.i_name::interval) )
 ); 

return;
end;$$;


ALTER FUNCTION public.delete_obs(ctime timestamp with time zone, maxid bigint) OWNER TO xbee;

--
-- Name: get_bayeos_obs(timestamp with time zone); Type: FUNCTION; Schema: public; Owner: xbee
--

CREATE FUNCTION get_bayeos_obs(_ctime timestamp with time zone) RETURNS SETOF bayeos_observation
    LANGUAGE plpgsql
    AS $$
declare
b bayeos_observation%rowtype;
cha record;
begin
for cha in select * from channel_detail where db_series_id is not null 
loop 
 for b in select cha.db_series_id, c.result_time, c.result_value from 
	get_channel_time_value_flag(_ctime,cha.id,cha.spline_id,cha.f_name,cha.i_name) as c where c.flag < 1
 loop
  return next b;
 end loop;
end loop;
return;
end;$$;


ALTER FUNCTION public.get_bayeos_obs(_ctime timestamp with time zone) OWNER TO xbee;

--
-- Name: get_channel_count(bigint); Type: FUNCTION; Schema: public; Owner: xbee
--

CREATE FUNCTION get_channel_count(_id bigint) RETURNS integer
    LANGUAGE plpgsql
    AS $$
declare
 ret int;
begin
select into ret get_channel_count(c.last_count, c.last_result_time, COALESCE(c.sampling_interval, b.sampling_interval), COALESCE(c.check_delay, b.check_delay, 0)) 
  from channel c join board b on c.board_id = b.id where c.id = _id; 
return ret;
end;
$$;


ALTER FUNCTION public.get_channel_count(_id bigint) OWNER TO xbee;

--
-- Name: get_channel_count(integer, timestamp with time zone, integer, integer); Type: FUNCTION; Schema: public; Owner: xbee
--

CREATE FUNCTION get_channel_count(_counts integer, _lrt timestamp with time zone, _sampling_interval integer, _check_delay integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
		$$;


ALTER FUNCTION public.get_channel_count(_counts integer, _lrt timestamp with time zone, _sampling_interval integer, _check_delay integer) OWNER TO xbee;

--
-- Name: get_channel_time_value_flag(timestamp with time zone, bigint, bigint, text, text); Type: FUNCTION; Schema: public; Owner: xbee
--

CREATE FUNCTION get_channel_time_value_flag(_ctime timestamp with time zone, _channel_id bigint, _spline_id bigint, _afunction text, _ainterval text) RETURNS SETOF time_value_flag
    LANGUAGE plpgsql
    AS $_$
declare
ret time_value_flag%rowtype;
rec record;
-- Function will return time - value - flag. 
-- If flag ==0 -> addByteRows, 
-- elsif flag<1 ->  upsertByteRows 
-- else ->  flag == 1 only "exported" -> ignore'
begin
  if (_ctime is null or _channel_id is null) then 
	return;
  end if;

-- check if there are new values in the observation table
  select into rec channel_id from observation where channel_id=_channel_id and result_time<_ctime limit 1;
  if not found then
    return;
  end if;

  if (_spline_id is null and _afunction is null) then 
	-- original			
	return query execute 'select result_time,result_value,0::numeric as flag from observation where result_time < $1 and channel_id = $2 order by 1'
	using _ctime,_channel_id;	
  elsif (_spline_id is not null and _afunction is null)  then 
	-- interpolation 
	return query execute 'select result_time,real_value(result_value,$1) as result_value, 0::numeric as flag
		from observation where result_time < $2 and channel_id = $3 order by 1'
		using _spline_id, _ctime, _channel_id;	
  elsif (_spline_id is null and _afunction is not null)  then 
	-- original and aggregation 
	-- union form observation and observeration_exp
	return query execute 'select date_truncate(result_time,interval ''' || _ainterval || ''')  as result_time, ' || _afunction || '(result_value)::real as result_value,avg(flag)::numeric as flag from 
 (select result_time,result_value,0::numeric as flag from observation
  where result_time < date_truncate($1, interval ''' || _ainterval || ''') and channel_id = $2
union
 select result_time,result_value,1::numeric as flag from observation_exp
 where channel_id = $2 and result_time < date_truncate($1, interval ''' || _ainterval || ''') 
 and result_time>= (select min(date_truncate(result_time, interval ''' || _ainterval || ''')) from observation where result_time < date_truncate($1, interval ''' || _ainterval || ''') and channel_id = $2) ) a

                         group by date_truncate(result_time, interval ''' || _ainterval || ''') order by 1'
               using _ctime, _channel_id;
  elsif (_spline_id is not null and _afunction is not null)  then 
	-- interpolation and aggregation 	
	return query execute 'select date_truncate(result_time,interval ''' || _ainterval || ''')  as result_time, ' || _afunction || '(real_value(result_value,$1))::real as result_value,avg(flag)::numeric from 	(select result_time,result_value,0::numeric as flag from observation
  where result_time < date_truncate($2, interval ''' || _ainterval || ''') and channel_id = $3
 union
 select result_time,result_value,1 as flag from observation_exp
  where result_time < date_truncate($2, interval ''' || _ainterval || ''') 
  and result_time>= (select min(date_truncate(result_time, interval ''' || _ainterval || ''')) from observation where result_time < date_truncate($2, interval ''' || _ainterval || ''') and channel_id = $3) and channel_id = $3) a
  group by date_truncate(result_time, interval ''' || _ainterval || ''') order by 1'
               using _spline_id, _ctime, _channel_id;	
  end if;
return;
end;$_$;


ALTER FUNCTION public.get_channel_time_value_flag(_ctime timestamp with time zone, _channel_id bigint, _spline_id bigint, _afunction text, _ainterval text) OWNER TO xbee;

--
-- Name: get_completeness_msg(integer); Type: FUNCTION; Schema: public; Owner: xbee
--

CREATE FUNCTION get_completeness_msg(_count integer) RETURNS text
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.get_completeness_msg(_count integer) OWNER TO xbee;

--
-- Name: get_completeness_status(integer); Type: FUNCTION; Schema: public; Owner: xbee
--

CREATE FUNCTION get_completeness_status(_count integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.get_completeness_status(_count integer) OWNER TO xbee;

--
-- Name: get_order_number(text); Type: FUNCTION; Schema: public; Owner: xbee
--

CREATE FUNCTION get_order_number(text) RETURNS text
    LANGUAGE plpgsql
    AS $_$
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
$_$;


ALTER FUNCTION public.get_order_number(text) OWNER TO xbee;

--
-- Name: init_spline(integer); Type: FUNCTION; Schema: public; Owner: xbee
--

CREATE FUNCTION init_spline(id integer) RETURNS void
    LANGUAGE plperlu
    AS $_X$use Math::Derivative;
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
	$_X$;


ALTER FUNCTION public.init_spline(id integer) OWNER TO xbee;

--
-- Name: real_value(double precision, bigint); Type: FUNCTION; Schema: public; Owner: xbee
--

CREATE FUNCTION real_value(adc double precision, id bigint) RETURNS real
    LANGUAGE plperlu
    AS $_X$
	use strict;
	if (! defined $_[1]) {
		return $_[0];
	}
	if(! exists $_SHARED{"spline$_[1]"}){
		spi_exec_query("select init_spline($_[1])");
	}
	return $_SHARED{"spline$_[1]"}->evaluate($_[0]);
	$_X$;


ALTER FUNCTION public.real_value(adc double precision, id bigint) OWNER TO xbee;

--
-- Name: sort(anyarray); Type: FUNCTION; Schema: public; Owner: xbee
--

CREATE FUNCTION sort(anyarray) RETURNS anyarray
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	select array(select $1[i] from generate_series(array_lower($1,1),
	array_upper($1,1)) g(i) order by 1)
	$_$;


ALTER FUNCTION public.sort(anyarray) OWNER TO xbee;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: acl_class; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE acl_class (
    id bigint NOT NULL,
    class character varying(255) NOT NULL
);


ALTER TABLE public.acl_class OWNER TO xbee;

--
-- Name: acl_class_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE acl_class_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.acl_class_id_seq OWNER TO xbee;

--
-- Name: acl_class_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE acl_class_id_seq OWNED BY acl_class.id;


--
-- Name: acl_entry; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE acl_entry (
    id bigint NOT NULL,
    ace_order integer NOT NULL,
    acl_object_identity bigint NOT NULL,
    audit_failure boolean NOT NULL,
    audit_success boolean NOT NULL,
    granting boolean NOT NULL,
    mask integer NOT NULL,
    sid bigint NOT NULL
);


ALTER TABLE public.acl_entry OWNER TO xbee;

--
-- Name: acl_entry_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE acl_entry_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.acl_entry_id_seq OWNER TO xbee;

--
-- Name: acl_entry_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE acl_entry_id_seq OWNED BY acl_entry.id;


--
-- Name: acl_object_identity; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE acl_object_identity (
    id bigint NOT NULL,
    object_id_class bigint NOT NULL,
    entries_inheriting boolean NOT NULL,
    object_id_identity bigint NOT NULL,
    owner_sid bigint,
    parent_object bigint
);


ALTER TABLE public.acl_object_identity OWNER TO xbee;

--
-- Name: acl_object_identity_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE acl_object_identity_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.acl_object_identity_id_seq OWNER TO xbee;

--
-- Name: acl_object_identity_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE acl_object_identity_id_seq OWNED BY acl_object_identity.id;


--
-- Name: acl_sid; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE acl_sid (
    id bigint NOT NULL,
    principal boolean NOT NULL,
    sid character varying(255) NOT NULL
);


ALTER TABLE public.acl_sid OWNER TO xbee;

--
-- Name: acl_sid_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE acl_sid_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.acl_sid_id_seq OWNER TO xbee;

--
-- Name: acl_sid_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE acl_sid_id_seq OWNED BY acl_sid.id;


--
-- Name: observation; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE observation (
    id bigint NOT NULL,
    channel_id integer NOT NULL,
    result_time timestamp with time zone NOT NULL,
    result_value real NOT NULL,
    status_valid integer,
    insert_time timestamp with time zone DEFAULT now()
);


ALTER TABLE public.observation OWNER TO xbee;

--
-- Name: observation_exp; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE observation_exp (
    id bigint NOT NULL,
    channel_id integer NOT NULL,
    result_time timestamp with time zone NOT NULL,
    result_value real NOT NULL,
    status_valid integer,
    insert_time timestamp with time zone
);


ALTER TABLE public.observation_exp OWNER TO xbee;

--
-- Name: all_observation; Type: VIEW; Schema: public; Owner: xbee
--

CREATE VIEW all_observation AS
    SELECT observation.id, observation.channel_id, observation.result_time, observation.result_value, observation.status_valid FROM observation UNION SELECT observation_exp.id, observation_exp.channel_id, observation_exp.result_time, observation_exp.result_value, observation_exp.status_valid FROM observation_exp;


ALTER TABLE public.all_observation OWNER TO xbee;

--
-- Name: board; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE board (
    id bigint NOT NULL,
    critical_max real,
    critical_min real,
    exclude_from_nagios boolean DEFAULT false NOT NULL,
    filter_critical_values boolean DEFAULT false NOT NULL,
    last_result_time timestamp with time zone,
    last_rssi smallint,
    origin text NOT NULL,
    name text,
    sampling_interval integer,
    status_valid integer,
    warning_max real,
    warning_min real,
    db_folder_id integer,
    db_auto_export boolean DEFAULT false NOT NULL,
    board_group_id integer,
    check_delay integer,
    deny_new_channels boolean DEFAULT false NOT NULL
);


ALTER TABLE public.board OWNER TO xbee;

--
-- Name: channel; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE channel (
    id bigint NOT NULL,
    aggr_function_id bigint,
    aggr_interval_id bigint,
    board_id bigint NOT NULL,
    critical_max real,
    critical_min real,
    db_series_id integer,
    exclude_from_nagios boolean DEFAULT false NOT NULL,
    filter_critical_values boolean DEFAULT false NOT NULL,
    label text,
    last_count integer,
    last_result_time timestamp with time zone,
    nr text NOT NULL,
    phenomena character varying(255),
    sampling_interval integer,
    spline_id bigint,
    status_valid integer,
    status_valid_msg character varying(255),
    unit_id bigint,
    warning_max real,
    warning_min real,
    db_exclude_auto_export boolean DEFAULT false NOT NULL,
    last_result_value real,
    check_delay integer
);


ALTER TABLE public.channel OWNER TO xbee;

--
-- Name: channel_check; Type: VIEW; Schema: public; Owner: xbee
--

CREATE VIEW channel_check AS
    SELECT channel.id, channel.board_id, GREATEST(channel.status_valid, get_completeness_status(get_channel_count(channel.id))) AS status, channel.status_valid, channel.status_valid_msg, get_completeness_msg(get_channel_count(channel.id)) AS status_complete_msg, get_completeness_status(get_channel_count(channel.id)) AS status_complete, channel.exclude_from_nagios FROM channel;


ALTER TABLE public.channel_check OWNER TO xbee;

--
-- Name: board_check; Type: VIEW; Schema: public; Owner: xbee
--

CREATE VIEW board_check AS
    SELECT b.id, b.board_group_id, a.status FROM ((SELECT bo.id, max(chk.status) AS status FROM (channel_check chk JOIN board bo ON ((bo.id = chk.board_id))) WHERE ((chk.exclude_from_nagios = false) AND (bo.exclude_from_nagios = false)) GROUP BY bo.id) a RIGHT JOIN (SELECT board.id, board.board_group_id, board.exclude_from_nagios FROM board) b ON ((b.id = a.id))) ORDER BY b.id;


ALTER TABLE public.board_check OWNER TO xbee;

--
-- Name: board_comment; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE board_comment (
    board_comments_id bigint,
    comment_id bigint
);


ALTER TABLE public.board_comment OWNER TO xbee;

--
-- Name: board_group; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE board_group (
    id bigint NOT NULL,
    name text NOT NULL,
    db_folder_id integer
);


ALTER TABLE public.board_group OWNER TO xbee;

--
-- Name: board_group_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE board_group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.board_group_id_seq OWNER TO xbee;

--
-- Name: board_group_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE board_group_id_seq OWNED BY board_group.id;


--
-- Name: board_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE board_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.board_id_seq OWNER TO xbee;

--
-- Name: board_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE board_id_seq OWNED BY board.id;


--
-- Name: board_template; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE board_template (
    id bigint NOT NULL,
    name text NOT NULL,
    description text,
    data_sheet text,
    revision text,
    last_updated timestamp with time zone,
    date_created timestamp with time zone,
    sampling_interval integer,
    critical_max real,
    critical_min real,
    warning_max real,
    warning_min real,
    check_delay integer
);


ALTER TABLE public.board_template OWNER TO xbee;

--
-- Name: board_template_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE board_template_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.board_template_id_seq OWNER TO xbee;

--
-- Name: board_template_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE board_template_id_seq OWNED BY board_template.id;


--
-- Name: function; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE function (
    id bigint NOT NULL,
    name text NOT NULL
);


ALTER TABLE public.function OWNER TO xbee;

--
-- Name: interval; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE "interval" (
    id bigint NOT NULL,
    name text NOT NULL
);


ALTER TABLE public."interval" OWNER TO xbee;

--
-- Name: channel_detail; Type: VIEW; Schema: public; Owner: xbee
--

CREATE VIEW channel_detail AS
    SELECT c.id, c.db_series_id, c.spline_id, f.name AS f_name, i.name AS i_name, COALESCE(c.filter_critical_values, b.filter_critical_values) AS filter_critical_values FROM (((board b JOIN channel c ON ((c.board_id = b.id))) LEFT JOIN function f ON ((f.id = c.aggr_function_id))) LEFT JOIN "interval" i ON ((i.id = c.aggr_interval_id)));


ALTER TABLE public.channel_detail OWNER TO xbee;

--
-- Name: channel_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE channel_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.channel_id_seq OWNER TO xbee;

--
-- Name: channel_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE channel_id_seq OWNED BY channel.id;


--
-- Name: channel_path; Type: VIEW; Schema: public; Owner: xbee
--

CREATE VIEW channel_path AS
    SELECT board.id AS board_id, board.name AS board_name, channel.id AS channel_id, channel.label AS channel_label, ((COALESCE(board.name, board.origin) || '/'::text) || COALESCE(channel.label, channel.nr)) AS path FROM channel, board WHERE (board.id = channel.board_id);


ALTER TABLE public.channel_path OWNER TO xbee;

--
-- Name: channel_template; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE channel_template (
    id bigint NOT NULL,
    nr text NOT NULL,
    label text NOT NULL,
    phenomena character varying(255),
    unit_id bigint,
    spline_id bigint,
    aggr_interval_id bigint,
    aggr_function_id bigint,
    board_template_id bigint NOT NULL,
    channel_templates_idx integer,
    sampling_interval integer,
    critical_max real,
    critical_min real,
    warning_max real,
    warning_min real,
    check_delay integer
);


ALTER TABLE public.channel_template OWNER TO xbee;

--
-- Name: channel_template_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE channel_template_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.channel_template_id_seq OWNER TO xbee;

--
-- Name: channel_template_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE channel_template_id_seq OWNED BY channel_template.id;


--
-- Name: comment; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE comment (
    id bigint NOT NULL,
    content text NOT NULL,
    insert_time timestamp without time zone NOT NULL,
    user_id bigint NOT NULL
);


ALTER TABLE public.comment OWNER TO xbee;

--
-- Name: comment_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE comment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.comment_id_seq OWNER TO xbee;

--
-- Name: comment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE comment_id_seq OWNED BY comment.id;


--
-- Name: delete_job_config; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE delete_job_config (
    id bigint NOT NULL,
    delay_interval integer NOT NULL,
    enabled boolean NOT NULL,
    max_message_interval_name text NOT NULL,
    max_result_interval_name text NOT NULL,
    max_stat_interval_name text DEFAULT '60 days'::text
);


ALTER TABLE public.delete_job_config OWNER TO xbee;

--
-- Name: delete_job_config_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE delete_job_config_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.delete_job_config_id_seq OWNER TO xbee;

--
-- Name: delete_job_config_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE delete_job_config_id_seq OWNED BY delete_job_config.id;


--
-- Name: export_job_config; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE export_job_config (
    id bigint NOT NULL,
    db_home_folder_id integer,
    db_home_unit_id integer,
    sleep_interval integer NOT NULL,
    enabled boolean NOT NULL,
    password text,
    records_per_bulk integer NOT NULL,
    url text NOT NULL,
    user_name text
);


ALTER TABLE public.export_job_config OWNER TO xbee;

--
-- Name: export_job_config_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE export_job_config_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.export_job_config_id_seq OWNER TO xbee;

--
-- Name: export_job_config_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE export_job_config_id_seq OWNED BY export_job_config.id;


--
-- Name: export_job_stat; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE export_job_stat (
    id bigint NOT NULL,
    start_time timestamp with time zone,
    end_time timestamp with time zone,
    exported integer,
    status integer
);


ALTER TABLE public.export_job_stat OWNER TO xbee;

--
-- Name: COLUMN export_job_stat.status; Type: COMMENT; Schema: public; Owner: xbee
--

COMMENT ON COLUMN export_job_stat.status IS 'Unix like exit status: 0:=success; 1:= error';


--
-- Name: export_job_stat_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE export_job_stat_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.export_job_stat_id_seq OWNER TO xbee;

--
-- Name: export_job_stat_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE export_job_stat_id_seq OWNED BY export_job_stat.id;


--
-- Name: function_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE function_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.function_id_seq OWNER TO xbee;

--
-- Name: function_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE function_id_seq OWNED BY function.id;


--
-- Name: group_check; Type: VIEW; Schema: public; Owner: xbee
--

CREATE VIEW group_check AS
    SELECT board_group.id, max(board_check.status) AS status FROM (board_check RIGHT JOIN board_group ON ((board_group.id = board_check.board_group_id))) GROUP BY board_group.id;


ALTER TABLE public.group_check OWNER TO xbee;

--
-- Name: interval_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE interval_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.interval_id_seq OWNER TO xbee;

--
-- Name: interval_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE interval_id_seq OWNED BY "interval".id;


--
-- Name: knot_point; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE knot_point (
    id bigint NOT NULL,
    spline_id bigint NOT NULL,
    x real NOT NULL,
    y real NOT NULL
);


ALTER TABLE public.knot_point OWNER TO xbee;

--
-- Name: knot_point_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE knot_point_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.knot_point_id_seq OWNER TO xbee;

--
-- Name: knot_point_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE knot_point_id_seq OWNED BY knot_point.id;


--
-- Name: message; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE message (
    id bigint NOT NULL,
    content text NOT NULL,
    origin text NOT NULL,
    result_time timestamp with time zone NOT NULL,
    type character varying(255) NOT NULL,
    insert_time timestamp with time zone DEFAULT now()
);


ALTER TABLE public.message OWNER TO xbee;

--
-- Name: message_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE message_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.message_id_seq OWNER TO xbee;

--
-- Name: message_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE message_id_seq OWNED BY message.id;


--
-- Name: nagios_status; Type: VIEW; Schema: public; Owner: xbee
--

CREATE VIEW nagios_status AS
    SELECT board_group.id AS group_id, board_group.name AS group_name, b.id AS board_id, b.origin AS board_origin, c.id AS channel_id, c.nr AS channel_nr, c.label AS channel_label, chk.status_complete, chk.status_complete_msg, chk.status_valid, chk.status_valid_msg FROM (board b LEFT JOIN board_group ON ((board_group.id = b.board_group_id))), channel c, channel_check chk WHERE ((((b.id = c.board_id) AND (chk.id = c.id)) AND ((chk.status_complete > 0) OR (chk.status_valid > 0))) AND (NOT (b.exclude_from_nagios OR c.exclude_from_nagios))) ORDER BY board_group.name, b.origin, c.nr;


ALTER TABLE public.nagios_status OWNER TO xbee;

--
-- Name: observation_exp_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE observation_exp_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.observation_exp_id_seq OWNER TO xbee;

--
-- Name: observation_exp_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE observation_exp_id_seq OWNED BY observation_exp.id;


--
-- Name: observation_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE observation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.observation_id_seq OWNER TO xbee;

--
-- Name: observation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE observation_id_seq OWNED BY observation.id;


--
-- Name: role; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE role (
    id bigint NOT NULL,
    version bigint,
    authority character varying(255) NOT NULL
);


ALTER TABLE public.role OWNER TO xbee;

--
-- Name: role_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.role_id_seq OWNER TO xbee;

--
-- Name: role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE role_id_seq OWNED BY role.id;


--
-- Name: spline; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE spline (
    id bigint NOT NULL,
    name text NOT NULL
);


ALTER TABLE public.spline OWNER TO xbee;

--
-- Name: spline_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE spline_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.spline_id_seq OWNER TO xbee;

--
-- Name: spline_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE spline_id_seq OWNED BY spline.id;


--
-- Name: unit; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE unit (
    id bigint NOT NULL,
    abbrevation character varying(255),
    name text NOT NULL,
    db_unit_id integer
);


ALTER TABLE public.unit OWNER TO xbee;

--
-- Name: unit_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE unit_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.unit_id_seq OWNER TO xbee;

--
-- Name: unit_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE unit_id_seq OWNED BY unit.id;


--
-- Name: user_role; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE user_role (
    role_id bigint NOT NULL,
    user_id bigint NOT NULL
);


ALTER TABLE public.user_role OWNER TO xbee;

--
-- Name: user_role_role_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE user_role_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.user_role_role_id_seq OWNER TO xbee;

--
-- Name: user_role_role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE user_role_role_id_seq OWNED BY user_role.role_id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: xbee; Tablespace: 
--

CREATE TABLE users (
    id bigint NOT NULL,
    version bigint,
    account_expired boolean NOT NULL,
    account_locked boolean NOT NULL,
    enabled boolean NOT NULL,
    password character varying(255) NOT NULL,
    password_expired boolean NOT NULL,
    username character varying(255) NOT NULL
);


ALTER TABLE public.users OWNER TO xbee;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: xbee
--

CREATE SEQUENCE users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.users_id_seq OWNER TO xbee;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: xbee
--

ALTER SEQUENCE users_id_seq OWNED BY users.id;


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY acl_class ALTER COLUMN id SET DEFAULT nextval('acl_class_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY acl_entry ALTER COLUMN id SET DEFAULT nextval('acl_entry_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY acl_object_identity ALTER COLUMN id SET DEFAULT nextval('acl_object_identity_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY acl_sid ALTER COLUMN id SET DEFAULT nextval('acl_sid_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY board ALTER COLUMN id SET DEFAULT nextval('board_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY board_group ALTER COLUMN id SET DEFAULT nextval('board_group_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY board_template ALTER COLUMN id SET DEFAULT nextval('board_template_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY channel ALTER COLUMN id SET DEFAULT nextval('channel_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY channel_template ALTER COLUMN id SET DEFAULT nextval('channel_template_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY comment ALTER COLUMN id SET DEFAULT nextval('comment_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY delete_job_config ALTER COLUMN id SET DEFAULT nextval('delete_job_config_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY export_job_config ALTER COLUMN id SET DEFAULT nextval('export_job_config_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY export_job_stat ALTER COLUMN id SET DEFAULT nextval('export_job_stat_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY function ALTER COLUMN id SET DEFAULT nextval('function_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY "interval" ALTER COLUMN id SET DEFAULT nextval('interval_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY knot_point ALTER COLUMN id SET DEFAULT nextval('knot_point_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY message ALTER COLUMN id SET DEFAULT nextval('message_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY observation ALTER COLUMN id SET DEFAULT nextval('observation_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY observation_exp ALTER COLUMN id SET DEFAULT nextval('observation_exp_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY role ALTER COLUMN id SET DEFAULT nextval('role_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY spline ALTER COLUMN id SET DEFAULT nextval('spline_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY unit ALTER COLUMN id SET DEFAULT nextval('unit_id_seq'::regclass);


--
-- Name: role_id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY user_role ALTER COLUMN role_id SET DEFAULT nextval('user_role_role_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY users ALTER COLUMN id SET DEFAULT nextval('users_id_seq'::regclass);


--
-- Data for Name: acl_class; Type: TABLE DATA; Schema: public; Owner: xbee
--

INSERT INTO acl_class VALUES (1, 'gateway.Board');
INSERT INTO acl_class VALUES (2, 'gateway.BoardGroup');
INSERT INTO acl_class VALUES (3, 'gateway.BoardTemplate');
INSERT INTO acl_class VALUES (4, 'gateway.Channel');
INSERT INTO acl_class VALUES (5, 'gateway.ChannelTemplate');
INSERT INTO acl_class VALUES (6, 'gateway.FileFormat');
INSERT INTO acl_class VALUES (7, 'gateway.Function');
INSERT INTO acl_class VALUES (8, 'gateway.Interval');
INSERT INTO acl_class VALUES (9, 'gateway.KnotPoint');
INSERT INTO acl_class VALUES (10, 'gateway.Message');
INSERT INTO acl_class VALUES (11, 'gateway.MessageType');
INSERT INTO acl_class VALUES (12, 'gateway.Observation');
INSERT INTO acl_class VALUES (13, 'gateway.Spline');
INSERT INTO acl_class VALUES (14, 'gateway.Unit');
INSERT INTO acl_class VALUES (15, 'gateway.User');
INSERT INTO acl_class VALUES (16, 'gateway.DeleteJobConfig');
INSERT INTO acl_class VALUES (17, 'gateway.ExportJobConfig');


--
-- Name: acl_class_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('acl_class_id_seq', 17, true);


--
-- Data for Name: acl_entry; Type: TABLE DATA; Schema: public; Owner: xbee
--



--
-- Name: acl_entry_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('acl_entry_id_seq', 1, false);


--
-- Data for Name: acl_object_identity; Type: TABLE DATA; Schema: public; Owner: xbee
--



--
-- Name: acl_object_identity_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('acl_object_identity_id_seq', 1, false);


--
-- Data for Name: acl_sid; Type: TABLE DATA; Schema: public; Owner: xbee
--



--
-- Name: acl_sid_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('acl_sid_id_seq', 1, false);


--
-- Data for Name: board; Type: TABLE DATA; Schema: public; Owner: xbee
--



--
-- Data for Name: board_comment; Type: TABLE DATA; Schema: public; Owner: xbee
--



--
-- Data for Name: board_group; Type: TABLE DATA; Schema: public; Owner: xbee
--



--
-- Name: board_group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('board_group_id_seq', 1, false);


--
-- Name: board_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('board_id_seq', 1, false);


--
-- Data for Name: board_template; Type: TABLE DATA; Schema: public; Owner: xbee
--



--
-- Name: board_template_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('board_template_id_seq', 1, false);


--
-- Data for Name: channel; Type: TABLE DATA; Schema: public; Owner: xbee
--



--
-- Name: channel_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('channel_id_seq', 1, false);


--
-- Data for Name: channel_template; Type: TABLE DATA; Schema: public; Owner: xbee
--



--
-- Name: channel_template_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('channel_template_id_seq', 1, false);


--
-- Data for Name: comment; Type: TABLE DATA; Schema: public; Owner: xbee
--



--
-- Name: comment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('comment_id_seq', 1, false);


--
-- Data for Name: delete_job_config; Type: TABLE DATA; Schema: public; Owner: xbee
--

INSERT INTO delete_job_config VALUES (1, 120, true, '60 days', '60 days', '60 days');


--
-- Name: delete_job_config_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('delete_job_config_id_seq', 1, true);


--
-- Data for Name: export_job_config; Type: TABLE DATA; Schema: public; Owner: xbee
--

INSERT INTO export_job_config VALUES (1, NULL, NULL, 10, true, 'UlBLVltG', 10000, 'http://localhost/BayEOS-Server/XMLServlet', 'root');


--
-- Name: export_job_config_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('export_job_config_id_seq', 1, true);


--
-- Data for Name: export_job_stat; Type: TABLE DATA; Schema: public; Owner: xbee
--



--
-- Name: export_job_stat_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('export_job_stat_id_seq', 1, false);


--
-- Data for Name: function; Type: TABLE DATA; Schema: public; Owner: xbee
--

INSERT INTO function VALUES (1, 'avg');
INSERT INTO function VALUES (2, 'count');
INSERT INTO function VALUES (3, 'median');
INSERT INTO function VALUES (4, 'sum');
INSERT INTO function VALUES (5, 'min');
INSERT INTO function VALUES (6, 'max');
INSERT INTO function VALUES (7, 'stddev');
INSERT INTO function VALUES (8, 'sum');
INSERT INTO function VALUES (9, 'variance');


--
-- Name: function_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('function_id_seq', 9, true);


--
-- Data for Name: interval; Type: TABLE DATA; Schema: public; Owner: xbee
--

INSERT INTO "interval" VALUES (1, '1 min');
INSERT INTO "interval" VALUES (2, '10 min');
INSERT INTO "interval" VALUES (3, '30 min');
INSERT INTO "interval" VALUES (4, '1 hour');
INSERT INTO "interval" VALUES (5, '15 min');
INSERT INTO "interval" VALUES (6, '45 min');
INSERT INTO "interval" VALUES (7, '2 hour');
INSERT INTO "interval" VALUES (8, '3 hour');
INSERT INTO "interval" VALUES (9, '4 hour');
INSERT INTO "interval" VALUES (10, '1 day');
INSERT INTO "interval" VALUES (11, '1 week');
INSERT INTO "interval" VALUES (12, '1 month');


--
-- Name: interval_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('interval_id_seq', 12, true);


--
-- Data for Name: knot_point; Type: TABLE DATA; Schema: public; Owner: xbee
--

INSERT INTO knot_point VALUES (1, 1, 106.910004, 48);
INSERT INTO knot_point VALUES (2, 1, 114.550003, 46);
INSERT INTO knot_point VALUES (3, 1, 122.709999, 44);
INSERT INTO knot_point VALUES (4, 1, 131.5, 42);
INSERT INTO knot_point VALUES (5, 1, 140.949997, 40);
INSERT INTO knot_point VALUES (6, 1, 151.100006, 38);
INSERT INTO knot_point VALUES (7, 1, 161.889999, 36);
INSERT INTO knot_point VALUES (8, 1, 173.470001, 34);
INSERT INTO knot_point VALUES (9, 1, 185.919998, 32);
INSERT INTO knot_point VALUES (10, 1, 199.130005, 30);
INSERT INTO knot_point VALUES (11, 1, 213.220001, 28);
INSERT INTO knot_point VALUES (12, 1, 228.25, 26);
INSERT INTO knot_point VALUES (13, 1, 236.080002, 25);
INSERT INTO knot_point VALUES (14, 1, 244.160004, 24);
INSERT INTO knot_point VALUES (15, 1, 260.98999, 22);
INSERT INTO knot_point VALUES (16, 1, 278.839996, 20);
INSERT INTO knot_point VALUES (17, 1, 297.619995, 18);
INSERT INTO knot_point VALUES (18, 1, 317.429993, 16);
INSERT INTO knot_point VALUES (19, 1, 338.170013, 14);
INSERT INTO knot_point VALUES (20, 1, 359.829987, 12);
INSERT INTO knot_point VALUES (21, 1, 382.420013, 10);
INSERT INTO knot_point VALUES (22, 1, 405.809998, 8);
INSERT INTO knot_point VALUES (23, 1, 429.959991, 6);
INSERT INTO knot_point VALUES (24, 1, 454.790009, 4);
INSERT INTO knot_point VALUES (25, 1, 480.23999, 2);
INSERT INTO knot_point VALUES (26, 1, 506.200012, 0);
INSERT INTO knot_point VALUES (27, 1, 532.450012, -2);
INSERT INTO knot_point VALUES (28, 1, 558.950012, -4);
INSERT INTO knot_point VALUES (29, 1, 585.52002, -6);
INSERT INTO knot_point VALUES (30, 1, 612.059998, -8);
INSERT INTO knot_point VALUES (31, 1, 638.400024, -10);
INSERT INTO knot_point VALUES (32, 1, 664.27002, -12);
INSERT INTO knot_point VALUES (33, 1, 689.630005, -14);
INSERT INTO knot_point VALUES (34, 1, 714.380005, -16);
INSERT INTO knot_point VALUES (35, 1, 738.369995, -18);
INSERT INTO knot_point VALUES (36, 1, 761.5, -20);
INSERT INTO knot_point VALUES (37, 1, 783.570007, -22);
INSERT INTO knot_point VALUES (38, 1, 804.590027, -24);
INSERT INTO knot_point VALUES (39, 1, 824.51001, -26);
INSERT INTO knot_point VALUES (40, 1, 843.289978, -28);
INSERT INTO knot_point VALUES (41, 1, 860.880005, -30);
INSERT INTO knot_point VALUES (42, 1, 877.169983, -32);
INSERT INTO knot_point VALUES (43, 1, 892.289978, -34);
INSERT INTO knot_point VALUES (44, 1, 906.25, -36);
INSERT INTO knot_point VALUES (45, 1, 919.070007, -38);


--
-- Name: knot_point_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('knot_point_id_seq', 45, true);


--
-- Data for Name: message; Type: TABLE DATA; Schema: public; Owner: xbee
--



--
-- Name: message_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('message_id_seq', 1, false);


--
-- Data for Name: observation; Type: TABLE DATA; Schema: public; Owner: xbee
--



--
-- Data for Name: observation_exp; Type: TABLE DATA; Schema: public; Owner: xbee
--



--
-- Name: observation_exp_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('observation_exp_id_seq', 1, false);


--
-- Name: observation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('observation_id_seq', 1, false);


--
-- Data for Name: role; Type: TABLE DATA; Schema: public; Owner: xbee
--

INSERT INTO role VALUES (1, 0, 'ROLE_ADMIN');
INSERT INTO role VALUES (2, 0, 'ROLE_USER');
INSERT INTO role VALUES (3, 0, 'ROLE_CHECK');
INSERT INTO role VALUES (4, 0, 'ROLE_IMPORT');


--
-- Name: role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('role_id_seq', 4, true);


--
-- Data for Name: spline; Type: TABLE DATA; Schema: public; Owner: xbee
--

INSERT INTO spline VALUES (1, 'Siemens 836');


--
-- Name: spline_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('spline_id_seq', 1, true);


--
-- Data for Name: unit; Type: TABLE DATA; Schema: public; Owner: xbee
--

INSERT INTO unit VALUES (1, NULL, 'Celsius', NULL);
INSERT INTO unit VALUES (2, NULL, 'Volts', NULL);


--
-- Name: unit_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('unit_id_seq', 2, true);


--
-- Data for Name: user_role; Type: TABLE DATA; Schema: public; Owner: xbee
--

INSERT INTO user_role VALUES (1, 1);
INSERT INTO user_role VALUES (3, 2);
INSERT INTO user_role VALUES (4, 3);


--
-- Name: user_role_role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('user_role_role_id_seq', 1, false);


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: xbee
--

INSERT INTO users VALUES (2, 0, false, true, true, 'zZZSEKOCeaHyuf4TDN8/vubx+8g=', false, 'nagios');
INSERT INTO users VALUES (1, 0, false, false, true, '8340uWG/1+Ca8V7rqyBRdQE8QTE=', false, 'root');
INSERT INTO users VALUES (3, 0, false, false, true, 'Yv371V0ZsqRnEQKte8oX2HX4IHo=', false, 'import');


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: xbee
--

SELECT pg_catalog.setval('users_id_seq', 3, true);


--
-- Name: acl_class_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY acl_class
    ADD CONSTRAINT acl_class_pkey PRIMARY KEY (id);


--
-- Name: acl_entry_acl_object_identity_ace_order_key; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY acl_entry
    ADD CONSTRAINT acl_entry_acl_object_identity_ace_order_key UNIQUE (acl_object_identity, ace_order);


--
-- Name: acl_entry_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY acl_entry
    ADD CONSTRAINT acl_entry_pkey PRIMARY KEY (id);


--
-- Name: acl_object_identity_object_id_class_object_id_identity_key; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY acl_object_identity
    ADD CONSTRAINT acl_object_identity_object_id_class_object_id_identity_key UNIQUE (object_id_class, object_id_identity);


--
-- Name: acl_object_identity_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY acl_object_identity
    ADD CONSTRAINT acl_object_identity_pkey PRIMARY KEY (id);


--
-- Name: acl_sid_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY acl_sid
    ADD CONSTRAINT acl_sid_pkey PRIMARY KEY (id);


--
-- Name: acl_sid_sid_principal_key; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY acl_sid
    ADD CONSTRAINT acl_sid_sid_principal_key UNIQUE (sid, principal);


--
-- Name: board_group_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY board_group
    ADD CONSTRAINT board_group_pkey PRIMARY KEY (id);


--
-- Name: board_origin_key; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY board
    ADD CONSTRAINT board_origin_key UNIQUE (origin);


--
-- Name: board_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY board
    ADD CONSTRAINT board_pkey PRIMARY KEY (id);


--
-- Name: board_template_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY board_template
    ADD CONSTRAINT board_template_pkey PRIMARY KEY (id);


--
-- Name: channel_board_nr_unique; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY channel
    ADD CONSTRAINT channel_board_nr_unique UNIQUE (board_id, nr);


--
-- Name: channel_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY channel
    ADD CONSTRAINT channel_pkey PRIMARY KEY (id);


--
-- Name: channel_template_board_nr_unique; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY channel_template
    ADD CONSTRAINT channel_template_board_nr_unique UNIQUE (board_template_id, nr);


--
-- Name: channel_template_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY channel_template
    ADD CONSTRAINT channel_template_pkey PRIMARY KEY (id);


--
-- Name: comment_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY comment
    ADD CONSTRAINT comment_pkey PRIMARY KEY (id);


--
-- Name: delete_job_config_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY delete_job_config
    ADD CONSTRAINT delete_job_config_pkey PRIMARY KEY (id);


--
-- Name: export_job_config_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY export_job_config
    ADD CONSTRAINT export_job_config_pkey PRIMARY KEY (id);


--
-- Name: export_job_stat_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY export_job_stat
    ADD CONSTRAINT export_job_stat_pkey PRIMARY KEY (id);


--
-- Name: fk_ch_template_nr; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY channel_template
    ADD CONSTRAINT fk_ch_template_nr UNIQUE (board_template_id, nr);


--
-- Name: function_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY function
    ADD CONSTRAINT function_pkey PRIMARY KEY (id);


--
-- Name: interval_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY "interval"
    ADD CONSTRAINT interval_pkey PRIMARY KEY (id);


--
-- Name: knot_point_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY knot_point
    ADD CONSTRAINT knot_point_pkey PRIMARY KEY (id);


--
-- Name: message_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY message
    ADD CONSTRAINT message_pkey PRIMARY KEY (id);


--
-- Name: observation_exp_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY observation_exp
    ADD CONSTRAINT observation_exp_pkey PRIMARY KEY (id);


--
-- Name: observation_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY observation
    ADD CONSTRAINT observation_pkey PRIMARY KEY (id);


--
-- Name: role_authority_key; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY role
    ADD CONSTRAINT role_authority_key UNIQUE (authority);


--
-- Name: role_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY role
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);


--
-- Name: spline_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY spline
    ADD CONSTRAINT spline_pkey PRIMARY KEY (id);


--
-- Name: unit_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY unit
    ADD CONSTRAINT unit_pkey PRIMARY KEY (id);


--
-- Name: user_role_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY user_role
    ADD CONSTRAINT user_role_pkey PRIMARY KEY (role_id, user_id);


--
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users_username_key; Type: CONSTRAINT; Schema: public; Owner: xbee; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: board_origin_idx; Type: INDEX; Schema: public; Owner: xbee; Tablespace: 
--

CREATE UNIQUE INDEX board_origin_idx ON board USING btree (origin);


--
-- Name: channel_board_id_nr; Type: INDEX; Schema: public; Owner: xbee; Tablespace: 
--

CREATE INDEX channel_board_id_nr ON channel USING btree (board_id, nr);


--
-- Name: knot_point_spline_id_x; Type: INDEX; Schema: public; Owner: xbee; Tablespace: 
--

CREATE UNIQUE INDEX knot_point_spline_id_x ON knot_point USING btree (spline_id, x);


--
-- Name: knote_point_idx; Type: INDEX; Schema: public; Owner: xbee; Tablespace: 
--

CREATE INDEX knote_point_idx ON knot_point USING btree (id, x);


--
-- Name: obs_exp_ch_id_result_time; Type: INDEX; Schema: public; Owner: xbee; Tablespace: 
--

CREATE INDEX obs_exp_ch_id_result_time ON observation_exp USING btree (channel_id, result_time);


--
-- Name: obs_exp_cha_id; Type: INDEX; Schema: public; Owner: xbee; Tablespace: 
--

CREATE INDEX obs_exp_cha_id ON observation_exp USING btree (channel_id);


--
-- Name: obs_exp_res_time; Type: INDEX; Schema: public; Owner: xbee; Tablespace: 
--

CREATE INDEX obs_exp_res_time ON observation_exp USING btree (result_time);


--
-- Name: observation_ch_id_id; Type: INDEX; Schema: public; Owner: xbee; Tablespace: 
--

CREATE INDEX observation_ch_id_id ON observation USING btree (channel_id, id);


--
-- Name: observation_ch_id_result_time; Type: INDEX; Schema: public; Owner: xbee; Tablespace: 
--

CREATE INDEX observation_ch_id_result_time ON observation USING btree (channel_id, result_time);


--
-- Name: observation_inserts; Type: RULE; Schema: public; Owner: xbee
--

CREATE RULE observation_inserts AS ON INSERT TO observation WHERE (SELECT true AS bool FROM observation WHERE ((observation.channel_id = new.channel_id) AND (observation.result_time = new.result_time))) DO INSTEAD NOTHING;


--
-- Name: fk143bf46a2b8bc784; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY user_role
    ADD CONSTRAINT fk143bf46a2b8bc784 FOREIGN KEY (role_id) REFERENCES role(id);


--
-- Name: fk143bf46ad0b68b64; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY user_role
    ADD CONSTRAINT fk143bf46ad0b68b64 FOREIGN KEY (user_id) REFERENCES users(id);


--
-- Name: fk2a2bb00970422cc5; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY acl_object_identity
    ADD CONSTRAINT fk2a2bb00970422cc5 FOREIGN KEY (object_id_class) REFERENCES acl_class(id);


--
-- Name: fk2a2bb00990ec1949; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY acl_object_identity
    ADD CONSTRAINT fk2a2bb00990ec1949 FOREIGN KEY (owner_sid) REFERENCES acl_sid(id);


--
-- Name: fk2a2bb009a50290b8; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY acl_object_identity
    ADD CONSTRAINT fk2a2bb009a50290b8 FOREIGN KEY (parent_object) REFERENCES acl_object_identity(id);


--
-- Name: fk2c0b7d037d458b24; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY channel
    ADD CONSTRAINT fk2c0b7d037d458b24 FOREIGN KEY (spline_id) REFERENCES spline(id);


--
-- Name: fk2c0b7d03a1cb9076; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY channel
    ADD CONSTRAINT fk2c0b7d03a1cb9076 FOREIGN KEY (aggr_interval_id) REFERENCES "interval"(id);


--
-- Name: fk2c0b7d03b4ecbfb0; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY channel
    ADD CONSTRAINT fk2c0b7d03b4ecbfb0 FOREIGN KEY (board_id) REFERENCES board(id);


--
-- Name: fk2c0b7d03c86784c4; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY channel
    ADD CONSTRAINT fk2c0b7d03c86784c4 FOREIGN KEY (unit_id) REFERENCES unit(id);


--
-- Name: fk2c0b7d03cc75d396; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY channel
    ADD CONSTRAINT fk2c0b7d03cc75d396 FOREIGN KEY (aggr_function_id) REFERENCES function(id);


--
-- Name: fk38a5ee5fd0b68b64; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY comment
    ADD CONSTRAINT fk38a5ee5fd0b68b64 FOREIGN KEY (user_id) REFERENCES users(id);


--
-- Name: fk5302d47d8fdb88d5; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY acl_entry
    ADD CONSTRAINT fk5302d47d8fdb88d5 FOREIGN KEY (sid) REFERENCES acl_sid(id);


--
-- Name: fk5302d47db0d9dc4d; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY acl_entry
    ADD CONSTRAINT fk5302d47db0d9dc4d FOREIGN KEY (acl_object_identity) REFERENCES acl_object_identity(id);


--
-- Name: fk7b8a01197d458b24; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY knot_point
    ADD CONSTRAINT fk7b8a01197d458b24 FOREIGN KEY (spline_id) REFERENCES spline(id);


--
-- Name: fk877c3e0628a1f209; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY board_comment
    ADD CONSTRAINT fk877c3e0628a1f209 FOREIGN KEY (board_comments_id) REFERENCES board(id);


--
-- Name: fk877c3e062f2e4b90; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY board_comment
    ADD CONSTRAINT fk877c3e062f2e4b90 FOREIGN KEY (comment_id) REFERENCES comment(id);


--
-- Name: fk_board_board_group; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY board
    ADD CONSTRAINT fk_board_board_group FOREIGN KEY (board_group_id) REFERENCES board_group(id) ON DELETE SET NULL;


--
-- Name: fk_ch_template_brd_template; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY channel_template
    ADD CONSTRAINT fk_ch_template_brd_template FOREIGN KEY (board_template_id) REFERENCES board_template(id) ON DELETE CASCADE;


--
-- Name: fk_ch_template_function; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY channel_template
    ADD CONSTRAINT fk_ch_template_function FOREIGN KEY (aggr_function_id) REFERENCES function(id);


--
-- Name: fk_ch_template_interval; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY channel_template
    ADD CONSTRAINT fk_ch_template_interval FOREIGN KEY (aggr_interval_id) REFERENCES "interval"(id);


--
-- Name: fk_ch_template_spline; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY channel_template
    ADD CONSTRAINT fk_ch_template_spline FOREIGN KEY (spline_id) REFERENCES spline(id);


--
-- Name: fk_ch_template_unit; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY channel_template
    ADD CONSTRAINT fk_ch_template_unit FOREIGN KEY (unit_id) REFERENCES unit(id);


--
-- Name: fk_obs_channel; Type: FK CONSTRAINT; Schema: public; Owner: xbee
--

ALTER TABLE ONLY observation
    ADD CONSTRAINT fk_obs_channel FOREIGN KEY (channel_id) REFERENCES channel(id) ON DELETE CASCADE;


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

