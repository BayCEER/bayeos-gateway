CREATE OR REPLACE FUNCTION public.delete_obs(
	ctime timestamp with time zone,
	maxid bigint)
    RETURNS void
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE 
AS $BODY$
begin 
 create temporary table channel_detail_temp on commit drop as select * from channel_detail where db_series_id is not null;
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

return;
end;
$BODY$;