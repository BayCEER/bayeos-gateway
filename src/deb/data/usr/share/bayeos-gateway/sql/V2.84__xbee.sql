DROP TABLE board_command;

CREATE TABLE board_command (
	id bigserial NOT NULL,
	kind int4 NOT NULL,
	payload bytea NOT NULL,
	description text NULL,
	response bytea NULL,
	insert_time timestamp with time zone NOT NULL,
	response_time timestamp with time zone NULL,
	user_id int8 NOT NULL,
	board_id int8 NOT NULL,
	status int4 NULL,
	CONSTRAINT board_command_pkey PRIMARY KEY (id),
	CONSTRAINT chk_board_command_kind CHECK ((kind >= 0)),
	CONSTRAINT uk_board_command_board_kind UNIQUE (board_id, kind),
	CONSTRAINT fk_board_command_board FOREIGN KEY (board_id) REFERENCES public.board(id) ON DELETE CASCADE,
	CONSTRAINT fk_board_command_users FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE
);



 