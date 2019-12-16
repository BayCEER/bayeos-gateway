CREATE OR replace VIEW channel_path as
SELECT board.domain_id,
    board.id AS board_id,
    board.name AS board_name,
    channel.id AS channel_id,
    channel.name AS channel_name,
    (COALESCE(board_group.name, 'NO-GROUP') || '/'::text) || (COALESCE(board.name, board.origin) || '/'::text) || COALESCE(channel.name, channel.nr) AS path,
    board_group.name as board_group
   FROM channel join board on board.id = channel.board_id
   left join board_group on board.board_group_id = board_group.id;

