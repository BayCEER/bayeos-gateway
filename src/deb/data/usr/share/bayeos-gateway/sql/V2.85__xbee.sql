CREATE OR REPLACE FUNCTION public.get_grafana_obs(_domain_id bigint, _path text, _interval text, _from timestamp with time zone, _to timestamp with time zone)
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
