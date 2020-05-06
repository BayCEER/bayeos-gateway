ALTER TABLE upload ADD COLUMN import_status smallint;
UPDATE upload set import_status = 0 where import_time is not null;
UPDATE upload set import_status = 1 where import_time is null;
