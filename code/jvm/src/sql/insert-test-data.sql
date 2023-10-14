insert into dbo.GameVariant (variant)
values ('FREESTYLE'),
       ('RENJU'),
       ('CARO'),
       ('OMOK'),
       ('NINUKI_RENJU'),
       ('PENTE');

insert into dbo.OpeningRule (rule)
values ('PRO'),
       ('LONG_PRO'),
       ('SWAP'),
       ('SWAP2');

insert into dbo.BoardSize (size)
values (15),
       (19);

insert into dbo.User (username, email, password_validation)
values ('user1', 'user1@example.com', 'password_hash_1'),
       ('user2', 'user2@example.com', 'password_hash_2'),
       ('user3', 'user3@example.com', 'password_hash_3'),
       ('user4', 'user4@example.com', 'password_hash_4'),
       ('user5', 'user5@example.com', 'password_hash_5');

insert into dbo.Token (token_validation, created_at, last_used_at, user_id)
values ('token_1', 1633824000, 1633904000, 1),
       ('token_2', 1633910400, 1634004000, 2),
       ('token_3', 1634090400, 1634184000, 3),
       ('token_4', 1634180400, 1634284000, 4),
       ('token_5', 1634280400, 1634384000, 5);

insert into dbo.Statistic (user_id, points, games_played, games_won)
values (1, 1000, 10, 5),
       (2, 4350, 8, 3),
       (3, 1303, 6, 2),
       (4, 1531, 10, 5),
       (5, 6122, 10, 5);

insert into dbo.Lobby (host_id, game_variant, opening_rule, board_size)
values (1, 'FREESTYLE', 'PRO', 15),
       (2, 'RENJU', 'LONG_PRO', 19),
       (3, 'OMOK', 'SWAP', 15);

insert into dbo.Game (state, game_variant, opening_rule, board_size, board, created, updated, host, guest)
values ('FINISHED', 'FREESTYLE', 'PRO', 15, '{"grid": ["b-7f", "w-9c", "b-8d", "w-6a"], "turn": {"color": "black", "timerLeftInSeconds": 28}}', 1635835200, 1635835300, 1, 3),
       ('IN_PROGRESS', 'RENJU', 'LONG_PRO', 15, '{"grid": ["b-7f", "w-9c", "b-8d", "w-6a"], "turn": {"color": "white", "timerLeftInSeconds": 13}}', 1635835200, 1635835300, 4, 5);

