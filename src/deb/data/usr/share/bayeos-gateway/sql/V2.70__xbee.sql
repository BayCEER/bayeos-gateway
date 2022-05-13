CREATE TABLE board_command (
	id bigserial NOT NULL,	
    value text not null,
    response text,
	insert_time timestamp NOT NULL,
    response_time timestamp,
	user_id int8 NOT NULL,
    board_id int8 NOT NULL,
	CONSTRAINT command PRIMARY KEY (id),
	CONSTRAINT fk_board_command_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_board_command_board FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE
);
