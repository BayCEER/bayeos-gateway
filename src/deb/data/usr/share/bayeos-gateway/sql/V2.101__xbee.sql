/* prevent 0 values  */ 
ALTER TABLE board DROP CONSTRAINT IF EXISTS board_sampling_interval_gt0;
ALTER TABLE board ADD CONSTRAINT board_sampling_interval_gt0 CHECK (sampling_interval > 0);

ALTER TABLE channel DROP CONSTRAINT IF EXISTS channel_sampling_interval_gt0; 
ALTER TABLE channel ADD CONSTRAINT channel_sampling_interval_gt0 CHECK (sampling_interval > 0);


ALTER TABLE board DROP CONSTRAINT IF EXISTS board_check_delay_gt0;
ALTER TABLE board ADD CONSTRAINT board_check_delay_gt0 CHECK (check_delay > 0);

ALTER TABLE channel DROP CONSTRAINT IF EXISTS channel_check_delay_gt0;
ALTER TABLE channel ADD CONSTRAINT channel_check_delay_gt0 CHECK (check_delay > 0);

/* completeness on aggregation definition */
ALTER TABLE channel DROP CONSTRAINT IF EXISTS channel_valid_aggregation;
ALTER TABLE channel
ADD CONSTRAINT channel_valid_aggregation
CHECK (
    (aggr_function_id IS NULL AND aggr_interval_id IS NULL) 
    OR 
    (aggr_function_id IS not NULL AND aggr_interval_id IS not NULL)
);