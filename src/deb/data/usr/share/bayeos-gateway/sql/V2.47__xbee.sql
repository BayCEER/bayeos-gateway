--
-- Fixed issue # 51 
-- Message not shown after board has been moved to a new domain
--
alter table message add column board_id bigint;
update message set board_id = board.id from board where message.origin = board.origin and 
((message.domain_id is null and board.domain_id is null) or (message.domain_id = board.domain_id));
delete from message where board_id is null;
alter table message alter column board_id set not null;
alter table message drop column origin;
alter table message drop column domain_id;
alter table message
  add constraint fk_message_board foreign key (board_id) references board (id)
   on delete cascade on update cascade;



