-- Board Commands
DROP TABLE IF EXISTS board_command;
CREATE TABLE board_command (
	id bigserial PRIMARY KEY,
    kind integer not null,
    value text not null,
    description text,
    response text,
	insert_time timestamp NOT NULL,
    response_time timestamp,
	user_id int8 NOT NULL,
    board_id int8 NOT NULL,	
	CONSTRAINT fk_board_command_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_board_command_board FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE,
    CONSTRAINT uk_board_command_board_kind UNIQUE (board_id,kind),
    CONSTRAINT chk_board_command_kind CHECK (kind >= 0)    
);

-- Persistent Logins
DROP TABLE IF EXISTS persistent_logins;
create table persistent_logins (username text not null, series text primary key, token text not null, last_used timestamp not null);
