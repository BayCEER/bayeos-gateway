-- Eleminate hard coded db_ids in export table
-- db_series_ids must be joined at runtime 
DELETE FROM observation_calc WHERE db_series_id NOT IN (SELECT db_series_id FROM channel);
ALTER TABLE observation_calc RENAME db_series_id  TO channel_id;
UPDATE observation_calc SET channel_id = channel.id FROM channel WHERE channel_id = channel.db_series_id;
ALTER TABLE public.observation_calc
  ADD CONSTRAINT fk_observation_calc_cha FOREIGN KEY (channel_id)
      REFERENCES public.channel (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;

DELETE FROM public.observation_exp WHERE channel_id NOT IN (SELECT id FROM channel);
ALTER TABLE public.observation_exp
  ADD CONSTRAINT fk_observation_exp_cha FOREIGN KEY (channel_id)
      REFERENCES public.channel (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;

     
  
-- Drop config table for job, all configuration should be done by environment variables 
DROP TABLE export_job_config;

-- Filter critical values
DROP FUNCTION get_bayeos_obs(timestamp with time zone);
DROP TYPE bayeos_observation;

DROP VIEW public.channel_detail;

-- Filter of critical values is controlled by channel attribute only
CREATE OR REPLACE VIEW public.channel_detail AS 
 SELECT c.id,
    c.db_series_id,
    c.spline_id,
    f.name AS f_name,
    i.name AS i_name,
    c.filter_critical_values,
    COALESCE(c.critical_min, b.critical_min) AS critical_min,
	COALESCE(c.critical_max, b.critical_max) AS critical_max
   FROM board b
     JOIN channel c ON c.board_id = b.id
     LEFT JOIN function f ON f.id = c.aggr_function_id
     LEFT JOIN "interval" i ON i.id = c.aggr_interval_id;
         
-- Interpolation und Filterung 
create or replace function get_result_value(_result_value real,_spline_id bigint,_filter_critical_values boolean, _critical_min real, _critical_max real) returns real as
$$
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
$$ language plpgsql; 
    
DROP FUNCTION public.get_channel_time_value_flag(timestamp with time zone, bigint, bigint, text, text);

CREATE OR REPLACE FUNCTION public.get_channel_time_value_flag(
    _ctime timestamp with time zone,
    _channel_id bigint,
    _spline_id bigint,
    _afunction text,
    _ainterval text,
    _filter_critical_values boolean,
    _critical_min real,
    _critical_max real)
  RETURNS SETOF time_value_flag AS
$BODY$
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
end;$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;

-- Replaceed db_series_id with channel_id and return implicit rowtype of observation_calc
CREATE OR REPLACE FUNCTION public.get_bayeos_obs(_ctime timestamp with time zone)
  RETURNS SETOF observation_calc AS
$BODY$
declare
b observation_calc%rowtype;
cha record;
begin
for cha in select * from channel_detail where db_series_id is not null 
loop 
 for b in select nextval('observation_calc_id_seq'::regclass), cha.id, c.result_time, c.result_value from 
	get_channel_time_value_flag(_ctime,cha.id,cha.spline_id,cha.f_name,cha.i_name,cha.filter_critical_values,cha.critical_min, cha.critical_max) as c where c.flag < 1
 loop
  return next b;
 end loop;
end loop;
return;
end;$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
 
  
 
 