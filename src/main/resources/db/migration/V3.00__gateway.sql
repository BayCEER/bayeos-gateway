--
-- PostgreSQL database dump
--

-- Dumped from database version 17.11 (Debian 17.11-1.pgdg13+2)
-- Dumped by pg_dump version 17.0

SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: check_result; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.check_result AS (
	code integer,
	msg text
);


--
-- Name: time_value; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.time_value AS (
	result_time timestamp with time zone,
	result_value real
);


--
-- Name: time_value_flag; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.time_value_flag AS (
	result_time timestamp with time zone,
	result_value real,
	flag numeric
);


--
-- Name: _real_median(real[]); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public._real_median(_value real[]) RETURNS real
    LANGUAGE plpgsql
    AS $$
declare
n bigint;
a real[];
begin
a = sort(_value);
n = array_upper(a,1);
if (n%2=0) then
 return 0.5*(a[n/2] + a[n/2+1]);	
else 
 return a[(n+1)/2];
end if;
end;$$;


--
-- Name: create_vc_channel(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.create_vc_channel() RETURNS trigger
    LANGUAGE plpgsql
    AS $$  
declare 
_e boolean;
begin
 select exists(select 1 from channel c join board b on b.id = c.board_id where b.id = NEW.board_id and c.nr = NEW.nr) into _e;
 if not _e then    
  insert into channel (board_id, nr) values (NEW.board_id, NEW.nr);          
 end if;         
 return NEW;
end;
$$;


--
-- Name: date_truncate(timestamp with time zone, interval); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.date_truncate(timestamp with time zone, interval) RETURNS timestamp with time zone
    LANGUAGE sql
    AS $_$
select to_timestamp(
  floor(extract(epoch from $1)/extract(epoch from $2))*extract(epoch from $2)
  );
$_$;


--
-- Name: double2float(double precision); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.double2float(value double precision) RETURNS real
    LANGUAGE plpgsql
    AS $$
declare fvalue float4;
begin 
	begin 
		fvalue:= value::float4;
	exception 
		when others then fvalue:= 'NaN'::float4;
	end;
return fvalue;
end;
$$;


--
-- Name: get_board_status(bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.get_board_status(_id bigint) RETURNS integer
    LANGUAGE plpgsql
    AS $$
declare
 ret int;
begin
 select into ret max(GREATEST(channel.status_valid, get_completeness_status(get_channel_count(channel.id)))) from channel where channel.board_id = _id and not channel.exclude_from_nagios;
return ret;
end;
$$;


--
-- Name: get_channel_count(bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.get_channel_count(_id bigint) RETURNS integer
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


--
-- Name: get_channel_count(integer, timestamp with time zone, integer, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.get_channel_count(_counts integer, _lrt timestamp with time zone, _sampling_interval integer, _check_delay integer) RETURNS integer
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


--
-- Name: get_channel_time_value_flag(timestamp with time zone, bigint, bigint, text, text, boolean, real, real); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.get_channel_time_value_flag(_ctime timestamp with time zone, _channel_id bigint, _spline_id bigint, _afunction text, _ainterval text, _filter_critical_values boolean, _critical_min real, _critical_max real) RETURNS SETOF public.time_value_flag
    LANGUAGE plpgsql
    AS $_$
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
	select result_time,get_result_value(result_value,$1,$2,$3,$4) as result_value,1 as flag from observation_exp
	where result_time < date_truncate($5, interval ''' || _ainterval || ''') 
	and result_time>= (select min(date_truncate(result_time, interval ''' || _ainterval || ''')) from observation 
	where result_time < date_truncate($5, interval ''' || _ainterval || ''') and channel_id = $6) and channel_id = $6) a where result_value != ''NaN''
	group by date_truncate(result_time, interval ''' || _ainterval || ''') order by 1' using _spline_id,_filter_critical_values,_critical_min, _critical_max, _ctime, _channel_id;	
      
  else     	
	return query execute 'select * from (select result_time,get_result_value(result_value,$1,$2,$3,$4) as result_value, 0::numeric as flag from observation where result_time < $5 and channel_id = $6) a where result_value != ''NaN'' order by 1' 
	using _spline_id,_filter_critical_values,_critical_min, _critical_max, _ctime, _channel_id;	
  end if;
return;
end;$_$;


--
-- Name: get_completeness_msg(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.get_completeness_msg(_count integer) RETURNS text
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


--
-- Name: get_completeness_status(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.get_completeness_status(_count integer) RETURNS integer
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


--
-- Name: get_grafana_obs(bigint, text, text, timestamp with time zone, timestamp with time zone); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.get_grafana_obs(_domain_id bigint, _path text, _interval text, _from timestamp with time zone, _to timestamp with time zone) RETURNS SETOF public.time_value
    LANGUAGE plpgsql
    AS $_$
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
end;$_$;


--
-- Name: get_group_status(bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.get_group_status(_id bigint) RETURNS integer
    LANGUAGE plpgsql
    AS $$
declare
 ret int;
begin
 select into ret max(get_board_status(board.id)) from board where board.board_group_id = _id and not board.exclude_from_nagios;
return ret;
end;
$$;


--
-- Name: get_order_number(text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.get_order_number(text) RETURNS text
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


--
-- Name: get_result_value(real, bigint, boolean, real, real); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.get_result_value(_result_value real, _spline_id bigint, _filter_critical_values boolean, _critical_min real, _critical_max real) RETURNS real
    LANGUAGE plpgsql
    AS $$
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
$$;


--
-- Name: init_spline(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.init_spline(id integer) RETURNS void
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


--
-- Name: obs_insert(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.obs_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$  
declare 
_id bigint;
begin
 select id into _id from observation where result_time = NEW.result_time and channel_id = NEW.channel_id;
 if FOUND then
  delete from observation where id = _id;             
 end if;         
 return NEW;
end;
$$;


--
-- Name: real_value(double precision, bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.real_value(adc double precision, id bigint) RETURNS real
    LANGUAGE plpgsql
    AS $$
begin
	return double2float(spline_value(adc,id));
end;
$$;


--
-- Name: sort(anyarray); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sort(anyarray) RETURNS anyarray
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	select array(select $1[i] from generate_series(array_lower($1,1),
	array_upper($1,1)) g(i) order by 1)
	$_$;


--
-- Name: spline_value(double precision, bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.spline_value(adc double precision, id bigint) RETURNS double precision
    LANGUAGE plperlu
    AS $_X$
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
	$_X$;


--
-- Name: median(real); Type: AGGREGATE; Schema: public; Owner: -
--

CREATE AGGREGATE public.median(real) (
    SFUNC = array_append,
    STYPE = real[],
    INITCOND = '{}',
    FINALFUNC = public._real_median
);


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: observation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.observation (
    id bigint NOT NULL,
    channel_id integer NOT NULL,
    result_time timestamp with time zone NOT NULL,
    result_value real NOT NULL,
    status_valid integer,
    insert_time timestamp with time zone DEFAULT now()
);


--
-- Name: observation_exp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.observation_exp (
    id bigint NOT NULL,
    channel_id integer NOT NULL,
    result_time timestamp with time zone NOT NULL,
    result_value real NOT NULL,
    status_valid integer,
    insert_time timestamp with time zone
);


--
-- Name: all_observation; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.all_observation AS
 SELECT observation.id,
    observation.channel_id,
    observation.result_time,
    observation.result_value,
    observation.status_valid
   FROM public.observation
UNION
 SELECT observation_exp.id,
    observation_exp.channel_id,
    observation_exp.result_time,
    observation_exp.result_value,
    observation_exp.status_valid
   FROM public.observation_exp;


--
-- Name: board; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.board (
    id bigint NOT NULL,
    exclude_from_nagios boolean DEFAULT false NOT NULL,
    filter_critical_values boolean DEFAULT false NOT NULL,
    last_result_time timestamp with time zone,
    last_rssi smallint,
    origin text NOT NULL,
    name text,
    sampling_interval integer,
    status_valid integer,
    db_folder_id integer,
    db_auto_export boolean DEFAULT false NOT NULL,
    board_group_id integer,
    check_delay integer,
    deny_new_channels boolean DEFAULT false NOT NULL,
    domain_id bigint,
    domain_id_created bigint,
    lat real,
    lon real,
    alt real,
    last_insert_time timestamp with time zone,
    date_created timestamp with time zone DEFAULT now() NOT NULL,
    force_sync boolean DEFAULT false NOT NULL,
    CONSTRAINT board_check_delay_gt0 CHECK ((check_delay > 0)),
    CONSTRAINT board_sampling_interval_gt0 CHECK ((sampling_interval > 0))
);


--
-- Name: channel; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.channel (
    id bigint NOT NULL,
    aggr_function_id bigint,
    aggr_interval_id bigint,
    board_id bigint NOT NULL,
    critical_max real,
    critical_min real,
    db_series_id integer,
    exclude_from_nagios boolean DEFAULT false NOT NULL,
    filter_critical_values boolean DEFAULT false NOT NULL,
    name text,
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
    check_delay integer,
    force_sync boolean DEFAULT false NOT NULL,
    CONSTRAINT channel_check_delay_gt0 CHECK ((check_delay > 0)),
    CONSTRAINT channel_sampling_interval_gt0 CHECK ((sampling_interval > 0)),
    CONSTRAINT channel_valid_aggregation CHECK ((((aggr_function_id IS NULL) AND (aggr_interval_id IS NULL)) OR ((aggr_function_id IS NOT NULL) AND (aggr_interval_id IS NOT NULL))))
);


--
-- Name: channel_check; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.channel_check AS
 SELECT id,
    board_id,
    GREATEST(status_valid, public.get_completeness_status(public.get_channel_count(id))) AS status,
    status_valid,
    status_valid_msg,
    public.get_completeness_msg(public.get_channel_count(id)) AS status_complete_msg,
    public.get_completeness_status(public.get_channel_count(id)) AS status_complete,
    exclude_from_nagios
   FROM public.channel;


--
-- Name: board_check; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.board_check AS
 SELECT b.id,
    b.board_group_id,
    a.status
   FROM (( SELECT bo.id,
            max(chk.status) AS status
           FROM (public.channel_check chk
             JOIN public.board bo ON ((bo.id = chk.board_id)))
          WHERE ((chk.exclude_from_nagios = false) AND (bo.exclude_from_nagios = false))
          GROUP BY bo.id) a
     RIGHT JOIN ( SELECT board.id,
            board.board_group_id,
            board.exclude_from_nagios
           FROM public.board) b ON ((b.id = a.id)))
  ORDER BY b.id;


--
-- Name: board_command; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.board_command (
    id bigint NOT NULL,
    kind integer NOT NULL,
    payload bytea NOT NULL,
    description text,
    response bytea,
    insert_time timestamp with time zone NOT NULL,
    response_time timestamp with time zone,
    user_id bigint NOT NULL,
    board_id bigint NOT NULL,
    status integer,
    CONSTRAINT chk_board_command_kind CHECK ((kind >= 0))
);


--
-- Name: board_command_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.board_command_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: board_command_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.board_command_id_seq OWNED BY public.board_command.id;


--
-- Name: board_comment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.board_comment (
    board_comments_id bigint,
    comment_id bigint NOT NULL
);


--
-- Name: board_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.board_group (
    id bigint NOT NULL,
    name text NOT NULL,
    db_folder_id integer,
    domain_id bigint,
    date_created timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: board_group_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.board_group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: board_group_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.board_group_id_seq OWNED BY public.board_group.id;


--
-- Name: board_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.board_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: board_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.board_id_seq OWNED BY public.board.id;


--
-- Name: board_template; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.board_template (
    id bigint NOT NULL,
    name text NOT NULL,
    description text,
    data_sheet text,
    revision text,
    last_updated timestamp with time zone,
    date_created timestamp with time zone,
    sampling_interval integer,
    check_delay integer,
    exclude_from_nagios boolean,
    filter_critical_values boolean,
    domain_id bigint,
    deny_new_channels boolean DEFAULT false NOT NULL,
    last_applied timestamp with time zone
);


--
-- Name: board_template_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.board_template_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: board_template_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.board_template_id_seq OWNED BY public.board_template.id;


--
-- Name: channel_binding; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.channel_binding (
    id integer NOT NULL,
    nr text,
    channel_function_param_id bigint NOT NULL,
    virtual_channel_id bigint NOT NULL,
    value real
);


--
-- Name: channel_binding_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.channel_binding_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: channel_binding_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.channel_binding_id_seq OWNED BY public.channel_binding.id;


--
-- Name: function; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.function (
    id bigint NOT NULL,
    name text NOT NULL,
    domain_id bigint
);


--
-- Name: interval; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."interval" (
    id bigint NOT NULL,
    name text NOT NULL,
    domain_id bigint
);


--
-- Name: channel_detail; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.channel_detail AS
 SELECT c.id,
    c.db_series_id,
    c.spline_id,
    f.name AS f_name,
    i.name AS i_name,
    c.filter_critical_values,
    c.critical_min,
    c.critical_max
   FROM ((public.channel c
     LEFT JOIN public.function f ON ((f.id = c.aggr_function_id)))
     LEFT JOIN public."interval" i ON ((i.id = c.aggr_interval_id)));


--
-- Name: channel_function; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.channel_function (
    id integer NOT NULL,
    body text,
    name text,
    domain_id bigint
);


--
-- Name: channel_function_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.channel_function_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: channel_function_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.channel_function_id_seq OWNED BY public.channel_function.id;


--
-- Name: channel_function_parameter; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.channel_function_parameter (
    id integer NOT NULL,
    name text NOT NULL,
    description text,
    channel_function_id bigint NOT NULL
);


--
-- Name: channel_function_parameter_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.channel_function_parameter_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: channel_function_parameter_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.channel_function_parameter_id_seq OWNED BY public.channel_function_parameter.id;


--
-- Name: channel_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.channel_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: channel_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.channel_id_seq OWNED BY public.channel.id;


--
-- Name: channel_path; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.channel_path AS
 SELECT board.domain_id,
    board.id AS board_id,
    board.name AS board_name,
    channel.id AS channel_id,
    channel.name AS channel_name,
    (((COALESCE(board_group.name, 'NO-GROUP'::text) || '/'::text) || (COALESCE(board.name, board.origin) || '/'::text)) || COALESCE(channel.name, channel.nr)) AS path,
    board_group.name AS board_group
   FROM ((public.channel
     JOIN public.board ON ((board.id = channel.board_id)))
     LEFT JOIN public.board_group ON ((board.board_group_id = board_group.id)));


--
-- Name: channel_template; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.channel_template (
    id bigint NOT NULL,
    nr text NOT NULL,
    name text NOT NULL,
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
    check_delay integer,
    exclude_from_nagios boolean,
    filter_critical_values boolean
);


--
-- Name: channel_template_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.channel_template_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: channel_template_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.channel_template_id_seq OWNED BY public.channel_template.id;


--
-- Name: comment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.comment (
    id bigint NOT NULL,
    content text NOT NULL,
    insert_time timestamp without time zone NOT NULL,
    user_id bigint NOT NULL
);


--
-- Name: comment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.comment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: comment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.comment_id_seq OWNED BY public.comment.id;


--
-- Name: contact; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.contact (
    id integer NOT NULL,
    email text NOT NULL,
    domain_id bigint
);


--
-- Name: contact_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.contact_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: contact_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.contact_id_seq OWNED BY public.contact.id;


--
-- Name: domain; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.domain (
    id bigint NOT NULL,
    name text NOT NULL,
    db_folder_id integer
);


--
-- Name: domain_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.domain_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: domain_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.domain_id_seq OWNED BY public.domain.id;


--
-- Name: function_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.function_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: function_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.function_id_seq OWNED BY public.function.id;


--
-- Name: group_check; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.group_check AS
 SELECT board_group.id,
    max(board_check.status) AS status
   FROM (public.board_check
     RIGHT JOIN public.board_group ON ((board_group.id = board_check.board_group_id)))
  GROUP BY board_group.id;


--
-- Name: interval_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.interval_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: interval_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.interval_id_seq OWNED BY public."interval".id;


--
-- Name: knot_point; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.knot_point (
    id bigint NOT NULL,
    spline_id bigint NOT NULL,
    x real NOT NULL,
    y real NOT NULL
);


--
-- Name: knot_point_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.knot_point_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: knot_point_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.knot_point_id_seq OWNED BY public.knot_point.id;


--
-- Name: message; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.message (
    id bigint NOT NULL,
    content text NOT NULL,
    result_time timestamp with time zone NOT NULL,
    type character varying(255) NOT NULL,
    insert_time timestamp with time zone DEFAULT now(),
    board_id bigint NOT NULL
);


--
-- Name: message_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.message_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: message_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.message_id_seq OWNED BY public.message.id;


--
-- Name: nagios_status; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.nagios_status AS
 SELECT d.id AS domain_id,
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
   FROM ((((public.channel c
     JOIN public.channel_check chk ON ((c.id = chk.id)))
     JOIN public.board b ON ((b.id = c.board_id)))
     LEFT JOIN public.domain d ON ((d.id = b.domain_id)))
     LEFT JOIN public.board_group g ON ((g.id = b.board_group_id)))
  WHERE (((chk.status_complete > 0) OR (chk.status_valid > 0)) AND (NOT (b.exclude_from_nagios OR c.exclude_from_nagios)))
  ORDER BY d.name, g.name, b.origin, c.nr;


--
-- Name: notification; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notification (
    id bigint NOT NULL,
    contact_id bigint NOT NULL,
    board_group_id bigint,
    board_id bigint
);


--
-- Name: notification_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.notification_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: notification_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.notification_id_seq OWNED BY public.notification.id;


--
-- Name: observation_calc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.observation_calc (
    id integer NOT NULL,
    channel_id integer NOT NULL,
    result_time timestamp with time zone NOT NULL,
    result_value real NOT NULL
);


--
-- Name: observation_calc_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.observation_calc_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: observation_calc_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.observation_calc_id_seq OWNED BY public.observation_calc.id;


--
-- Name: observation_exp_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.observation_exp_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: observation_exp_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.observation_exp_id_seq OWNED BY public.observation_exp.id;


--
-- Name: observation_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.observation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: observation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.observation_id_seq OWNED BY public.observation.id;


--
-- Name: observation_out; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.observation_out (
    id bigint NOT NULL,
    channel_id integer NOT NULL,
    result_time timestamp with time zone NOT NULL,
    result_value real NOT NULL,
    insert_time timestamp with time zone DEFAULT now()
);


--
-- Name: observation_out_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.observation_out_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: observation_out_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.observation_out_id_seq OWNED BY public.observation_out.id;


--
-- Name: persistent_logins; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.persistent_logins (
    username text NOT NULL,
    series text NOT NULL,
    token text NOT NULL,
    last_used timestamp without time zone NOT NULL
);


--
-- Name: service_state_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.service_state_log (
    id bigint NOT NULL,
    last_state_change timestamp with time zone DEFAULT now() NOT NULL,
    last_check_time timestamp with time zone DEFAULT now() NOT NULL,
    domain_id bigint,
    board_group_id bigint,
    board_id bigint,
    soft_state_count smallint DEFAULT 0 NOT NULL,
    hard_state smallint DEFAULT 0 NOT NULL
);


--
-- Name: service_state_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.service_state_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: service_state_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.service_state_log_id_seq OWNED BY public.service_state_log.id;


--
-- Name: spline; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.spline (
    id bigint NOT NULL,
    name text NOT NULL,
    domain_id bigint
);


--
-- Name: spline_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.spline_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: spline_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.spline_id_seq OWNED BY public.spline.id;


--
-- Name: unit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.unit (
    id bigint NOT NULL,
    abbrevation character varying(255),
    name text NOT NULL,
    db_unit_id integer,
    domain_id bigint
);


--
-- Name: unit_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.unit_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: unit_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.unit_id_seq OWNED BY public.unit.id;


--
-- Name: upload; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.upload (
    domain_id bigint,
    id integer NOT NULL,
    uuid uuid NOT NULL,
    name text NOT NULL,
    size bigint NOT NULL,
    upload_time timestamp with time zone DEFAULT now() NOT NULL,
    import_time timestamp with time zone,
    import_message text,
    user_id bigint NOT NULL,
    import_status smallint
);


--
-- Name: upload_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.upload_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: upload_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.upload_id_seq OWNED BY public.upload.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    version bigint,
    account_locked boolean NOT NULL,
    password character varying(255),
    name character varying(255) NOT NULL,
    role text NOT NULL,
    domain_id bigint,
    first_name text,
    last_name text,
    contact_id bigint
);


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: virtual_channel; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.virtual_channel (
    id bigint NOT NULL,
    nr text NOT NULL,
    channel_function_id bigint NOT NULL,
    board_id bigint NOT NULL,
    event text DEFAULT 'insert'::text NOT NULL,
    CONSTRAINT virtual_table_chk_event CHECK ((event = ANY (ARRAY['insert'::text, 'calculate'::text])))
);


--
-- Name: virtual_channel_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.virtual_channel_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: virtual_channel_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.virtual_channel_id_seq OWNED BY public.virtual_channel.id;


--
-- Name: board id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board ALTER COLUMN id SET DEFAULT nextval('public.board_id_seq'::regclass);


--
-- Name: board_command id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_command ALTER COLUMN id SET DEFAULT nextval('public.board_command_id_seq'::regclass);


--
-- Name: board_group id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_group ALTER COLUMN id SET DEFAULT nextval('public.board_group_id_seq'::regclass);


--
-- Name: board_template id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_template ALTER COLUMN id SET DEFAULT nextval('public.board_template_id_seq'::regclass);


--
-- Name: channel id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel ALTER COLUMN id SET DEFAULT nextval('public.channel_id_seq'::regclass);


--
-- Name: channel_binding id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel_binding ALTER COLUMN id SET DEFAULT nextval('public.channel_binding_id_seq'::regclass);


--
-- Name: channel_function id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel_function ALTER COLUMN id SET DEFAULT nextval('public.channel_function_id_seq'::regclass);


--
-- Name: channel_function_parameter id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel_function_parameter ALTER COLUMN id SET DEFAULT nextval('public.channel_function_parameter_id_seq'::regclass);


--
-- Name: channel_template id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel_template ALTER COLUMN id SET DEFAULT nextval('public.channel_template_id_seq'::regclass);


--
-- Name: comment id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.comment ALTER COLUMN id SET DEFAULT nextval('public.comment_id_seq'::regclass);


--
-- Name: contact id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contact ALTER COLUMN id SET DEFAULT nextval('public.contact_id_seq'::regclass);


--
-- Name: domain id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.domain ALTER COLUMN id SET DEFAULT nextval('public.domain_id_seq'::regclass);


--
-- Name: function id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.function ALTER COLUMN id SET DEFAULT nextval('public.function_id_seq'::regclass);


--
-- Name: interval id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."interval" ALTER COLUMN id SET DEFAULT nextval('public.interval_id_seq'::regclass);


--
-- Name: knot_point id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.knot_point ALTER COLUMN id SET DEFAULT nextval('public.knot_point_id_seq'::regclass);


--
-- Name: message id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.message ALTER COLUMN id SET DEFAULT nextval('public.message_id_seq'::regclass);


--
-- Name: notification id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification ALTER COLUMN id SET DEFAULT nextval('public.notification_id_seq'::regclass);


--
-- Name: observation id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.observation ALTER COLUMN id SET DEFAULT nextval('public.observation_id_seq'::regclass);


--
-- Name: observation_calc id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.observation_calc ALTER COLUMN id SET DEFAULT nextval('public.observation_calc_id_seq'::regclass);


--
-- Name: observation_exp id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.observation_exp ALTER COLUMN id SET DEFAULT nextval('public.observation_exp_id_seq'::regclass);


--
-- Name: observation_out id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.observation_out ALTER COLUMN id SET DEFAULT nextval('public.observation_out_id_seq'::regclass);


--
-- Name: service_state_log id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_state_log ALTER COLUMN id SET DEFAULT nextval('public.service_state_log_id_seq'::regclass);


--
-- Name: spline id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.spline ALTER COLUMN id SET DEFAULT nextval('public.spline_id_seq'::regclass);


--
-- Name: unit id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.unit ALTER COLUMN id SET DEFAULT nextval('public.unit_id_seq'::regclass);


--
-- Name: upload id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.upload ALTER COLUMN id SET DEFAULT nextval('public.upload_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: virtual_channel id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.virtual_channel ALTER COLUMN id SET DEFAULT nextval('public.virtual_channel_id_seq'::regclass);


--
-- Data for Name: board; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.board (id, exclude_from_nagios, filter_critical_values, last_result_time, last_rssi, origin, name, sampling_interval, status_valid, db_folder_id, db_auto_export, board_group_id, check_delay, deny_new_channels, domain_id, domain_id_created, lat, lon, alt, last_insert_time, date_created, force_sync) FROM stdin;
\.


--
-- Data for Name: board_command; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.board_command (id, kind, payload, description, response, insert_time, response_time, user_id, board_id, status) FROM stdin;
\.


--
-- Data for Name: board_comment; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.board_comment (board_comments_id, comment_id) FROM stdin;
\.


--
-- Data for Name: board_group; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.board_group (id, name, db_folder_id, domain_id, date_created) FROM stdin;
\.


--
-- Data for Name: board_template; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.board_template (id, name, description, data_sheet, revision, last_updated, date_created, sampling_interval, check_delay, exclude_from_nagios, filter_critical_values, domain_id, deny_new_channels, last_applied) FROM stdin;
\.


--
-- Data for Name: channel; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.channel (id, aggr_function_id, aggr_interval_id, board_id, critical_max, critical_min, db_series_id, exclude_from_nagios, filter_critical_values, name, last_count, last_result_time, nr, phenomena, sampling_interval, spline_id, status_valid, status_valid_msg, unit_id, warning_max, warning_min, db_exclude_auto_export, last_result_value, check_delay, force_sync) FROM stdin;
\.


--
-- Data for Name: channel_binding; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.channel_binding (id, nr, channel_function_param_id, virtual_channel_id, value) FROM stdin;
\.


--
-- Data for Name: channel_function; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.channel_function (id, body, name, domain_id) FROM stdin;
1	x*x;	square	\N
2	var K2 = 17.625; \r\nvar K3 = 243.04;\r\nvar h = Math.log(rH);\r\nK3 * (K2*ta/(K3+ta) + h)/(K2*K3/(K3+ta) - h);	dewpoint	\N
\.


--
-- Data for Name: channel_function_parameter; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.channel_function_parameter (id, name, description, channel_function_id) FROM stdin;
1	x	\N	1
2	rH	relative humidity in percent	2
3	ta	air temperature in degC	2
\.


--
-- Data for Name: channel_template; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.channel_template (id, nr, name, phenomena, unit_id, spline_id, aggr_interval_id, aggr_function_id, board_template_id, channel_templates_idx, sampling_interval, critical_max, critical_min, warning_max, warning_min, check_delay, exclude_from_nagios, filter_critical_values) FROM stdin;
\.


--
-- Data for Name: comment; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.comment (id, content, insert_time, user_id) FROM stdin;
\.


--
-- Data for Name: contact; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.contact (id, email, domain_id) FROM stdin;
\.


--
-- Data for Name: domain; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.domain (id, name, db_folder_id) FROM stdin;
\.



--
-- Data for Name: function; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.function (id, name, domain_id) FROM stdin;
1	avg	\N
2	count	\N
3	median	\N
4	sum	\N
5	min	\N
6	max	\N
7	stddev	\N
8	sum	\N
9	variance	\N
\.


--
-- Data for Name: interval; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public."interval" (id, name, domain_id) FROM stdin;
1	1 min	\N
2	10 min	\N
3	30 min	\N
4	1 hour	\N
5	15 min	\N
6	45 min	\N
7	2 hour	\N
8	3 hour	\N
9	4 hour	\N
10	1 day	\N
11	1 week	\N
12	1 month	\N
\.


--
-- Data for Name: knot_point; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.knot_point (id, spline_id, x, y) FROM stdin;
1	1	106.91	48
2	1	114.55	46
3	1	122.71	44
4	1	131.5	42
5	1	140.95	40
6	1	151.1	38
7	1	161.89	36
8	1	173.47	34
9	1	185.92	32
10	1	199.13	30
11	1	213.22	28
12	1	228.25	26
13	1	236.08	25
14	1	244.16	24
15	1	260.99	22
16	1	278.84	20
17	1	297.62	18
18	1	317.43	16
19	1	338.17	14
20	1	359.83	12
21	1	382.42	10
22	1	405.81	8
23	1	429.96	6
24	1	454.79	4
25	1	480.24	2
26	1	506.2	0
27	1	532.45	-2
28	1	558.95	-4
29	1	585.52	-6
30	1	612.06	-8
31	1	638.4	-10
32	1	664.27	-12
33	1	689.63	-14
34	1	714.38	-16
35	1	738.37	-18
36	1	761.5	-20
37	1	783.57	-22
38	1	804.59	-24
39	1	824.51	-26
40	1	843.29	-28
41	1	860.88	-30
42	1	877.17	-32
43	1	892.29	-34
44	1	906.25	-36
45	1	919.07	-38
\.


--
-- Data for Name: message; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.message (id, content, result_time, type, insert_time, board_id) FROM stdin;
\.


--
-- Data for Name: notification; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.notification (id, contact_id, board_group_id, board_id) FROM stdin;
\.


--
-- Data for Name: observation; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.observation (id, channel_id, result_time, result_value, status_valid, insert_time) FROM stdin;
\.


--
-- Data for Name: observation_calc; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.observation_calc (id, channel_id, result_time, result_value) FROM stdin;
\.


--
-- Data for Name: observation_exp; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.observation_exp (id, channel_id, result_time, result_value, status_valid, insert_time) FROM stdin;
\.


--
-- Data for Name: observation_out; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.observation_out (id, channel_id, result_time, result_value, insert_time) FROM stdin;
\.


--
-- Data for Name: persistent_logins; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.persistent_logins (username, series, token, last_used) FROM stdin;
\.


--
-- Data for Name: service_state_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.service_state_log (id, last_state_change, last_check_time, domain_id, board_group_id, board_id, soft_state_count, hard_state) FROM stdin;
\.


--
-- Data for Name: spline; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.spline (id, name, domain_id) FROM stdin;
1	Siemens 836	\N
\.


--
-- Data for Name: unit; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.unit (id, abbrevation, name, db_unit_id, domain_id) FROM stdin;
1	\N	Celsius	\N	\N
2	\N	Volts	\N	\N
\.


--
-- Data for Name: upload; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.upload (domain_id, id, uuid, name, size, upload_time, import_time, import_message, user_id, import_status) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.users (id, version, account_locked, password, name, role, domain_id, first_name, last_name, contact_id) FROM stdin;
2	0	t	zZZSEKOCeaHyuf4TDN8/vubx+8g=	nagios	CHECK	\N	\N	\N	\N
3	0	f	Yv371V0ZsqRnEQKte8oX2HX4IHo=	import	IMPORT	\N	\N	\N	\N
1	0	f	8340uWG/1+Ca8V7rqyBRdQE8QTE=	root	USER	\N	\N	\N	\N
\.


--
-- Data for Name: virtual_channel; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.virtual_channel (id, nr, channel_function_id, board_id, event) FROM stdin;
\.


--
-- Name: board_command_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.board_command_id_seq', 1, false);


--
-- Name: board_group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.board_group_id_seq', 1, false);


--
-- Name: board_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.board_id_seq', 1, false);


--
-- Name: board_template_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.board_template_id_seq', 1, false);


--
-- Name: channel_binding_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.channel_binding_id_seq', 1, false);


--
-- Name: channel_function_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.channel_function_id_seq', 2, true);


--
-- Name: channel_function_parameter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.channel_function_parameter_id_seq', 3, true);


--
-- Name: channel_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.channel_id_seq', 1, false);


--
-- Name: channel_template_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.channel_template_id_seq', 1, false);


--
-- Name: comment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.comment_id_seq', 1, false);


--
-- Name: contact_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.contact_id_seq', 1, false);


--
-- Name: domain_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.domain_id_seq', 1, false);


--
-- Name: function_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.function_id_seq', 9, true);


--
-- Name: interval_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.interval_id_seq', 12, true);


--
-- Name: knot_point_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.knot_point_id_seq', 45, true);


--
-- Name: message_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.message_id_seq', 1, false);


--
-- Name: notification_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.notification_id_seq', 1, false);


--
-- Name: observation_calc_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.observation_calc_id_seq', 1, false);


--
-- Name: observation_exp_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.observation_exp_id_seq', 1, false);


--
-- Name: observation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.observation_id_seq', 1, false);


--
-- Name: observation_out_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.observation_out_id_seq', 1, false);


--
-- Name: service_state_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.service_state_log_id_seq', 1, false);


--
-- Name: spline_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.spline_id_seq', 1, true);


--
-- Name: unit_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.unit_id_seq', 2, true);


--
-- Name: upload_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.upload_id_seq', 1, false);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.users_id_seq', 3, true);


--
-- Name: virtual_channel_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.virtual_channel_id_seq', 1, false);


--
-- Name: board_command board_command_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_command
    ADD CONSTRAINT board_command_pkey PRIMARY KEY (id);


--
-- Name: board_comment board_comment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_comment
    ADD CONSTRAINT board_comment_pkey PRIMARY KEY (comment_id);


--
-- Name: board_group board_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_group
    ADD CONSTRAINT board_group_pkey PRIMARY KEY (id);


--
-- Name: board_group board_group_ukey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_group
    ADD CONSTRAINT board_group_ukey UNIQUE (domain_id, name);


--
-- Name: board board_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board
    ADD CONSTRAINT board_pkey PRIMARY KEY (id);


--
-- Name: board_template board_template_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_template
    ADD CONSTRAINT board_template_pkey PRIMARY KEY (id);


--
-- Name: board board_ukey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board
    ADD CONSTRAINT board_ukey UNIQUE (domain_id, origin);


--
-- Name: channel_binding channel_binding_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel_binding
    ADD CONSTRAINT channel_binding_pkey PRIMARY KEY (id);


--
-- Name: channel channel_board_nr_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel
    ADD CONSTRAINT channel_board_nr_unique UNIQUE (board_id, nr);


--
-- Name: channel_function_parameter channel_function_parameter_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel_function_parameter
    ADD CONSTRAINT channel_function_parameter_pkey PRIMARY KEY (id);


--
-- Name: channel_function_parameter channel_function_parameter_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel_function_parameter
    ADD CONSTRAINT channel_function_parameter_unique UNIQUE (name, channel_function_id);


--
-- Name: channel_function channel_function_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel_function
    ADD CONSTRAINT channel_function_pkey PRIMARY KEY (id);


--
-- Name: channel_function channel_function_ukey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel_function
    ADD CONSTRAINT channel_function_ukey UNIQUE (domain_id, name);


--
-- Name: channel channel_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel
    ADD CONSTRAINT channel_pkey PRIMARY KEY (id);


--
-- Name: channel_template channel_template_board_nr_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel_template
    ADD CONSTRAINT channel_template_board_nr_unique UNIQUE (board_template_id, nr);


--
-- Name: channel_template channel_template_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel_template
    ADD CONSTRAINT channel_template_pkey PRIMARY KEY (id);


--
-- Name: comment comment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.comment
    ADD CONSTRAINT comment_pkey PRIMARY KEY (id);


--
-- Name: contact contact_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contact
    ADD CONSTRAINT contact_pkey PRIMARY KEY (id);


--
-- Name: domain domain_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.domain
    ADD CONSTRAINT domain_name_key UNIQUE (name);


--
-- Name: domain domain_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.domain
    ADD CONSTRAINT domain_pkey PRIMARY KEY (id);


--
-- Name: channel_template fk_ch_template_nr; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel_template
    ADD CONSTRAINT fk_ch_template_nr UNIQUE (board_template_id, nr);


--
-- Name: function function_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.function
    ADD CONSTRAINT function_pkey PRIMARY KEY (id);


--
-- Name: function function_ukey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.function
    ADD CONSTRAINT function_ukey UNIQUE (domain_id, name);


--
-- Name: interval interval_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."interval"
    ADD CONSTRAINT interval_pkey PRIMARY KEY (id);


--
-- Name: interval interval_ukey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."interval"
    ADD CONSTRAINT interval_ukey UNIQUE (domain_id, name);


--
-- Name: knot_point knot_point_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.knot_point
    ADD CONSTRAINT knot_point_pkey PRIMARY KEY (id);


--
-- Name: message message_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.message
    ADD CONSTRAINT message_pkey PRIMARY KEY (id);


--
-- Name: notification notification_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT notification_pkey PRIMARY KEY (id);


--
-- Name: observation_exp observation_exp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.observation_exp
    ADD CONSTRAINT observation_exp_pkey PRIMARY KEY (id);


--
-- Name: observation_out observation_out_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.observation_out
    ADD CONSTRAINT observation_out_pkey PRIMARY KEY (id);


--
-- Name: observation observation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.observation
    ADD CONSTRAINT observation_pkey PRIMARY KEY (id);


--
-- Name: persistent_logins persistent_logins_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.persistent_logins
    ADD CONSTRAINT persistent_logins_pkey PRIMARY KEY (series);


--
-- Name: observation_calc pk_obs_calculated; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.observation_calc
    ADD CONSTRAINT pk_obs_calculated PRIMARY KEY (id);


--
-- Name: service_state_log pk_service_state_log; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_state_log
    ADD CONSTRAINT pk_service_state_log PRIMARY KEY (id);


--
-- Name: spline spline_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.spline
    ADD CONSTRAINT spline_pkey PRIMARY KEY (id);


--
-- Name: spline spline_ukey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.spline
    ADD CONSTRAINT spline_ukey UNIQUE (domain_id, name);


--
-- Name: board_command uk_board_command_board_kind; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_command
    ADD CONSTRAINT uk_board_command_board_kind UNIQUE (board_id, kind);


--
-- Name: unit unit_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.unit
    ADD CONSTRAINT unit_pkey PRIMARY KEY (id);


--
-- Name: unit unit_ukey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.unit
    ADD CONSTRAINT unit_ukey UNIQUE (domain_id, name);


--
-- Name: upload upload_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.upload
    ADD CONSTRAINT upload_pkey PRIMARY KEY (id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users users_ukey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_ukey UNIQUE (domain_id, name);


--
-- Name: virtual_channel virtual_channel_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.virtual_channel
    ADD CONSTRAINT virtual_channel_pkey PRIMARY KEY (id);


--
-- Name: virtual_channel virtual_channel_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.virtual_channel
    ADD CONSTRAINT virtual_channel_unique UNIQUE (nr, board_id);


--
-- Name: channel_board_id_nr; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX channel_board_id_nr ON public.channel USING btree (board_id, nr);


--
-- Name: knot_point_spline_id_x; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX knot_point_spline_id_x ON public.knot_point USING btree (spline_id, x);


--
-- Name: knote_point_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX knote_point_idx ON public.knot_point USING btree (id, x);


--
-- Name: obs_exp_ch_id_result_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX obs_exp_ch_id_result_time ON public.observation_exp USING btree (channel_id, result_time);


--
-- Name: observation_ch_id_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX observation_ch_id_id ON public.observation USING btree (channel_id, id);


--
-- Name: observation_ch_id_result_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX observation_ch_id_result_time ON public.observation USING btree (channel_id, result_time);


--
-- Name: observation_exp_id_cha_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX observation_exp_id_cha_idx ON public.observation_exp USING btree (id, channel_id);


--
-- Name: observation_exp_insert_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX observation_exp_insert_time_idx ON public.observation_exp USING btree (insert_time);


--
-- Name: observation_id_cha_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX observation_id_cha_idx ON public.observation USING btree (id, channel_id);


--
-- Name: observation trg_obs_inserts; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_obs_inserts BEFORE INSERT ON public.observation FOR EACH ROW EXECUTE FUNCTION public.obs_insert();


--
-- Name: virtual_channel trg_vc_upsert; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_vc_upsert BEFORE INSERT OR UPDATE ON public.virtual_channel FOR EACH ROW EXECUTE FUNCTION public.create_vc_channel();


--
-- Name: channel fk2c0b7d037d458b24; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel
    ADD CONSTRAINT fk2c0b7d037d458b24 FOREIGN KEY (spline_id) REFERENCES public.spline(id);


--
-- Name: channel fk2c0b7d03a1cb9076; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel
    ADD CONSTRAINT fk2c0b7d03a1cb9076 FOREIGN KEY (aggr_interval_id) REFERENCES public."interval"(id);


--
-- Name: channel fk2c0b7d03b4ecbfb0; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel
    ADD CONSTRAINT fk2c0b7d03b4ecbfb0 FOREIGN KEY (board_id) REFERENCES public.board(id) ON DELETE CASCADE;


--
-- Name: channel fk2c0b7d03c86784c4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel
    ADD CONSTRAINT fk2c0b7d03c86784c4 FOREIGN KEY (unit_id) REFERENCES public.unit(id);


--
-- Name: channel fk2c0b7d03cc75d396; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel
    ADD CONSTRAINT fk2c0b7d03cc75d396 FOREIGN KEY (aggr_function_id) REFERENCES public.function(id);


--
-- Name: knot_point fk7b8a01197d458b24; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.knot_point
    ADD CONSTRAINT fk7b8a01197d458b24 FOREIGN KEY (spline_id) REFERENCES public.spline(id) ON DELETE CASCADE;


--
-- Name: board fk_board_board_group; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board
    ADD CONSTRAINT fk_board_board_group FOREIGN KEY (board_group_id) REFERENCES public.board_group(id) ON DELETE SET NULL;


--
-- Name: board_command fk_board_command_board; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_command
    ADD CONSTRAINT fk_board_command_board FOREIGN KEY (board_id) REFERENCES public.board(id) ON DELETE CASCADE;


--
-- Name: board_command fk_board_command_users; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_command
    ADD CONSTRAINT fk_board_command_users FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: board_comment fk_board_comment_board; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_comment
    ADD CONSTRAINT fk_board_comment_board FOREIGN KEY (board_comments_id) REFERENCES public.board(id) ON DELETE CASCADE;


--
-- Name: board_comment fk_board_comment_comment; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_comment
    ADD CONSTRAINT fk_board_comment_comment FOREIGN KEY (comment_id) REFERENCES public.comment(id);


--
-- Name: board fk_board_domain; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board
    ADD CONSTRAINT fk_board_domain FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON DELETE CASCADE;


--
-- Name: board_group fk_board_group_domain; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_group
    ADD CONSTRAINT fk_board_group_domain FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON DELETE CASCADE;


--
-- Name: board_template fk_board_template_domain; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_template
    ADD CONSTRAINT fk_board_template_domain FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON DELETE CASCADE;


--
-- Name: channel_template fk_ch_template_brd_template; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel_template
    ADD CONSTRAINT fk_ch_template_brd_template FOREIGN KEY (board_template_id) REFERENCES public.board_template(id) ON DELETE CASCADE;


--
-- Name: channel_template fk_ch_template_function; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel_template
    ADD CONSTRAINT fk_ch_template_function FOREIGN KEY (aggr_function_id) REFERENCES public.function(id);


--
-- Name: channel_template fk_ch_template_interval; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel_template
    ADD CONSTRAINT fk_ch_template_interval FOREIGN KEY (aggr_interval_id) REFERENCES public."interval"(id);


--
-- Name: channel_template fk_ch_template_spline; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel_template
    ADD CONSTRAINT fk_ch_template_spline FOREIGN KEY (spline_id) REFERENCES public.spline(id);


--
-- Name: channel_template fk_ch_template_unit; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel_template
    ADD CONSTRAINT fk_ch_template_unit FOREIGN KEY (unit_id) REFERENCES public.unit(id);


--
-- Name: channel_binding fk_channel_binding_channel_function_param_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel_binding
    ADD CONSTRAINT fk_channel_binding_channel_function_param_id FOREIGN KEY (channel_function_param_id) REFERENCES public.channel_function_parameter(id) ON DELETE CASCADE;


--
-- Name: channel_binding fk_channel_binding_virtual_channel_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel_binding
    ADD CONSTRAINT fk_channel_binding_virtual_channel_id FOREIGN KEY (virtual_channel_id) REFERENCES public.virtual_channel(id) ON DELETE CASCADE;


--
-- Name: channel_function fk_channel_function_domain; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel_function
    ADD CONSTRAINT fk_channel_function_domain FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON DELETE CASCADE;


--
-- Name: channel_function_parameter fk_channel_function_parameter_channel_function_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.channel_function_parameter
    ADD CONSTRAINT fk_channel_function_parameter_channel_function_id FOREIGN KEY (channel_function_id) REFERENCES public.channel_function(id) ON DELETE CASCADE;


--
-- Name: comment fk_comment_users; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.comment
    ADD CONSTRAINT fk_comment_users FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: contact fk_contact_domain; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contact
    ADD CONSTRAINT fk_contact_domain FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON DELETE CASCADE;


--
-- Name: function fk_function_domain; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.function
    ADD CONSTRAINT fk_function_domain FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON DELETE CASCADE;


--
-- Name: interval fk_interval_domain; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."interval"
    ADD CONSTRAINT fk_interval_domain FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON DELETE CASCADE;


--
-- Name: message fk_message_board; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.message
    ADD CONSTRAINT fk_message_board FOREIGN KEY (board_id) REFERENCES public.board(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: notification fk_notification_board; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT fk_notification_board FOREIGN KEY (board_id) REFERENCES public.board(id) ON DELETE CASCADE;


--
-- Name: notification fk_notification_board_group; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT fk_notification_board_group FOREIGN KEY (board_group_id) REFERENCES public.board_group(id) ON DELETE CASCADE;


--
-- Name: notification fk_notification_contact; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT fk_notification_contact FOREIGN KEY (contact_id) REFERENCES public.contact(id) ON DELETE CASCADE;


--
-- Name: observation fk_obs_channel; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.observation
    ADD CONSTRAINT fk_obs_channel FOREIGN KEY (channel_id) REFERENCES public.channel(id) ON DELETE CASCADE;


--
-- Name: observation_calc fk_observation_calc_cha; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.observation_calc
    ADD CONSTRAINT fk_observation_calc_cha FOREIGN KEY (channel_id) REFERENCES public.channel(id) ON DELETE CASCADE;


--
-- Name: observation_exp fk_observation_exp_cha; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.observation_exp
    ADD CONSTRAINT fk_observation_exp_cha FOREIGN KEY (channel_id) REFERENCES public.channel(id) ON DELETE CASCADE;


--
-- Name: service_state_log fk_service_state_log_board; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_state_log
    ADD CONSTRAINT fk_service_state_log_board FOREIGN KEY (board_id) REFERENCES public.board(id) ON DELETE CASCADE;


--
-- Name: service_state_log fk_service_state_log_board_group; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_state_log
    ADD CONSTRAINT fk_service_state_log_board_group FOREIGN KEY (board_group_id) REFERENCES public.board_group(id) ON DELETE CASCADE;


--
-- Name: service_state_log fk_service_state_log_domain; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_state_log
    ADD CONSTRAINT fk_service_state_log_domain FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON DELETE CASCADE;


--
-- Name: spline fk_spline_domain; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.spline
    ADD CONSTRAINT fk_spline_domain FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON DELETE CASCADE;


--
-- Name: unit fk_unit_domain; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.unit
    ADD CONSTRAINT fk_unit_domain FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON DELETE CASCADE;


--
-- Name: upload fk_upload_domain; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.upload
    ADD CONSTRAINT fk_upload_domain FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON DELETE CASCADE;


--
-- Name: upload fk_upload_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.upload
    ADD CONSTRAINT fk_upload_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: users fk_users_contact; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk_users_contact FOREIGN KEY (contact_id) REFERENCES public.contact(id) ON DELETE SET NULL;


--
-- Name: users fk_users_domain; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk_users_domain FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON DELETE CASCADE;


--
-- Name: virtual_channel fk_virtual_channel_board_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.virtual_channel
    ADD CONSTRAINT fk_virtual_channel_board_id FOREIGN KEY (board_id) REFERENCES public.board(id) ON DELETE CASCADE;


--
-- Name: virtual_channel fk_virtual_channel_channelfunction_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.virtual_channel
    ADD CONSTRAINT fk_virtual_channel_channelfunction_id FOREIGN KEY (channel_function_id) REFERENCES public.channel_function(id) ON DELETE CASCADE;


--
-- Name: observation_out observation_out_channel_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.observation_out
    ADD CONSTRAINT observation_out_channel_id_fkey FOREIGN KEY (channel_id) REFERENCES public.channel(id) ON DELETE CASCADE;


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: -
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO PUBLIC;




--
-- PostgreSQL database dump complete
--

