CREATE OR REPLACE FUNCTION public.get_channel_time_value_flag(
    _ctime timestamp with time zone,
    _channel_id bigint,
    _spline_id bigint,
    _afunction text,
    _ainterval text)
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
interpolate boolean;
begin
  aggregate = _afunction is not null and _ainterval is not null;
  interpolate = _spline_id is not null;
  if (_ctime is null or _channel_id is null) then 
	return;
  end if;
  
-- check if there are new values in the observation table
  select into rec channel_id from observation where channel_id=_channel_id and result_time<_ctime limit 1;
  if not found then
    return;
  end if;

  if (aggregate) then
    -- union form observation and observeration_exp
    if (interpolate) then
        -- interpolate and aggregate
	return query execute 'select date_truncate(result_time,interval ''' || _ainterval || ''')  as result_time, ' || _afunction || '(real_value(result_value,$1))::real as result_value,avg(flag)::numeric from 
	(select result_time,result_value,0::numeric as flag from observation
	where result_time < date_truncate($2, interval ''' || _ainterval || ''') and channel_id = $3
	union
	select result_time,result_value,1 as flag from observation_exp
	where result_time < date_truncate($2, interval ''' || _ainterval || ''') 
	and result_time>= (select min(date_truncate(result_time, interval ''' || _ainterval || ''')) from observation where result_time < date_truncate($2, interval ''' || _ainterval || ''') and channel_id = $3) and channel_id = $3) a
	group by date_truncate(result_time, interval ''' || _ainterval || ''') order by 1' using _spline_id, _ctime, _channel_id;	
      
    else
	-- aggregate only      	
	return query execute 'select date_truncate(result_time,interval ''' || _ainterval || ''')  as result_time, ' || _afunction || '(result_value)::real as result_value,avg(flag)::numeric as flag from 
	(select result_time,result_value,0::numeric as flag from observation
	where result_time < date_truncate($1, interval ''' || _ainterval || ''') and channel_id = $2
	union
	select result_time,result_value,1::numeric as flag from observation_exp
	where channel_id = $2 and result_time < date_truncate($1, interval ''' || _ainterval || ''') 
	and result_time>= (select min(date_truncate(result_time, interval ''' || _ainterval || ''')) from observation where result_time < date_truncate($1, interval ''' || _ainterval || ''') and channel_id = $2) ) a
	group by date_truncate(result_time, interval ''' || _ainterval || ''') order by 1' using _ctime, _channel_id;
    end if;
  else 
    if (interpolate) then
	-- interpolate
	return query execute 'select result_time,real_value(result_value,$1) as result_value, 0::numeric as flag from observation where result_time < $2 and channel_id = $3 order by 1' using _spline_id, _ctime, _channel_id;		
    else
	-- original 
	return query execute 'select result_time,result_value,0::numeric as flag from observation where result_time < $1 and channel_id = $2 order by 1'
	using _ctime,_channel_id;		
    end if;
  end if;
return;
end;$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;