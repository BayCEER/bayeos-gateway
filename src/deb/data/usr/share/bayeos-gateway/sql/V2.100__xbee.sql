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




