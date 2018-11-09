 -- Issue#48 Constant values in channel function
alter table channel_binding add column value real;
alter table channel_binding alter column nr drop not null;


