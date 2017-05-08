-- Rename column channel.label to channel.name 
alter table channel rename label to name;
alter table channel_template rename label to name;

drop view channel_path;
create view channel_path as 
 select board.id as board_id,
    board.name as board_name,
    channel.id as channel_id,
    channel.name as channel_name,
    (coalesce(board.name, board.origin) || '/'::text) || coalesce(channel.name, channel.nr) as path
   from channel,
    board
  where board.id = channel.board_id;
drop view nagios_status;
create view nagios_status as 
 select board_group.id as group_id,
    board_group.name as group_name,
    b.id as board_id,
    b.origin as board_origin,
    c.id as channel_id,
    c.nr as channel_nr,
    c.name as channel_name,
    chk.status_complete,
    chk.status_complete_msg,
    chk.status_valid,
    chk.status_valid_msg
   from board b
     left join board_group on board_group.id = b.board_group_id,
    channel c,
    channel_check chk
  where b.id = c.board_id and chk.id = c.id and (chk.status_complete > 0 or chk.status_valid > 0) and not (b.exclude_from_nagios or c.exclude_from_nagios)
  order by board_group.name, b.origin, c.nr;

-- New virtual channel functionality 
create table channel_binding (id serial not null, nr text not null, channel_function_param_id bigint not null, virtual_channel_id bigint not null, primary key (id));
create table channel_function (id serial not null, body text, name text, primary key (id));

create table virtual_channel (id serial not null, nr text not null, channel_function_id bigint not null, board_id bigint not null, primary key (id));
create table channel_function_parameter (id  serial not null, name text not null, description text, channel_function_id bigint not null, primary key (id));

alter table channel_binding add constraint fk_channel_binding_virtual_channel_id foreign key (virtual_channel_id) references virtual_channel (id) ON DELETE CASCADE;
alter table channel_binding add constraint fk_channel_binding_channel_function_param_id foreign key (channel_function_param_id) references channel_function_parameter (id) ON DELETE CASCADE;

alter table virtual_channel add constraint fk_virtual_channel_channelfunction_id foreign key (channel_function_id) references channel_function (id) ON DELETE CASCADE;
alter table virtual_channel add constraint fk_virtual_channel_board_id foreign key (board_id) references board (id) ON DELETE CASCADE;

alter table channel_function_parameter add constraint fk_channel_function_parameter_channel_function_id foreign key (channel_function_id) references channel_function (id) ON DELETE CASCADE;

INSERT INTO channel_function VALUES (1, 'x*x;', 'square');
INSERT INTO channel_function VALUES (2, 'var K2 = 17.625; 
var K3 = 243.04;
var h = Math.log(rH);
K3 * (K2*ta/(K3+ta) + h)/(K2*K3/(K3+ta) - h);', 'dewpoint');
SELECT pg_catalog.setval('channel_function_id_seq', 2, true);

INSERT INTO channel_function_parameter VALUES (1, 'x', null, 1);
INSERT INTO channel_function_parameter VALUES (2, 'rH', 'relative humidity in percent', 2);
INSERT INTO channel_function_parameter VALUES (3, 'ta', 'air temperature in degC', 2);
SELECT pg_catalog.setval('channel_function_parameter_id_seq', 3, true);


CREATE TABLE IF NOT EXISTS observation_calc
(
  id serial NOT NULL,
  db_series_id integer NOT NULL,
  result_time timestamp with time zone NOT NULL,
  result_value real NOT NULL,
  CONSTRAINT pk_obs_calculated PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
