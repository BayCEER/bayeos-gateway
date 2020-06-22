CREATE TYPE public.publish_obs AS
   (channel_id integer,
    result_time timestamp with time zone,
    result_value real);

DROP FUNCTION public.get_bayeos_obs(timestamp with time zone);

-- Filter must be adapted to include only channels with export = True
CREATE OR REPLACE FUNCTION public.get_bayeos_obs(_ctime timestamp with time zone)
  RETURNS SETOF publish_obs AS
$BODY$
declare
b publish_obs;
cha record;
begin
for cha in select * from channel_detail 
loop 
 for b in select cha.id, c.result_time, c.result_value from 
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
ALTER FUNCTION public.get_bayeos_obs(timestamp with time zone)
  OWNER TO xbee;

-- Filter must be adapted to include only channels with export = True
CREATE OR REPLACE FUNCTION public.delete_obs(
    ctime timestamp with time zone,
    maxid bigint)
  RETURNS bigint AS
$BODY$
declare
 counts bigint;
begin 
 create temporary table channel_detail_temp on commit drop as select * from channel_detail;
 insert into observation_exp select o.* from observation o, channel_detail_temp c 
 where o.id<=maxid and c.id=o.channel_id and (
   (c.i_name is null and o.result_time<ctime) or 
   (c.i_name is not null and o.result_time< date_truncate(ctime, c.i_name::interval) )
 );

 delete from observation as o
 using channel_detail_temp c 
 where o.id<=maxid and c.id=o.channel_id and (
   (c.i_name is null and o.result_time<ctime) or 
   (c.i_name is not null and o.result_time< date_truncate(ctime, c.i_name::interval) )
 ); 
 GET DIAGNOSTICS counts = ROW_COUNT; 
return counts;
end;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.delete_obs(timestamp with time zone, bigint)
  OWNER TO xbee;

-- Wait until table is empty ?
drop table observation_calc;
