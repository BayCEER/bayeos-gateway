/* Spatial coordinates for board */ 
ALTER TABLE board ADD lat float4 NULL;
ALTER TABLE board ADD lon float4 NULL;
ALTER TABLE board ADD alt float4 NULL;

/* Retention policy for board template */ 
alter table board_template add last_applied timestamptz;
update board_template set last_applied = now();

/* Retention policy for board */
alter table board add last_insert_time timestamptz;
alter table board add date_created timestamptz;
with cha as (select board_id, min(last_result_time) as min, max(last_result_time) as max from channel group by board_id)
update board set date_created = cha.min, last_insert_time = cha.max from cha where cha.board_id = board.id;  
update board set date_created = now() where date_created is null;
alter table board alter column date_created set default now();
alter table board alter column date_created set not null; 

/* Retention policy for board group */ 
alter table board_group add date_created timestamptz;
update board_group set date_created = now() where date_created is null;
alter table board_group alter column date_created set default now();
alter table board_group alter column date_created set not null;

/* Force sync with flag on boards and channels to trigger update of server properties by user */
alter table board add column force_sync boolean default false not null;
alter table channel add column force_sync boolean default false not null;

/* Anyelement does not exist on postgresql >= 14 */
DROP AGGREGATE IF EXISTS median(anyelement);

/* Anyarray does not exist on postgresql >= 14 */
DROP FUNCTION IF EXISTS _float_median(anyarray);

CREATE OR REPLACE FUNCTION _real_median(_value real[])
 RETURNS real
 LANGUAGE plpgsql
AS $function$
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
end;$function$
;

CREATE OR REPLACE AGGREGATE median(real) (
	SFUNC = array_append,
	STYPE = real[],
	FINALFUNC = _real_median,
	FINALFUNC_MODIFY = READ_ONLY,
	INITCOND = '{}'
);
 
 







