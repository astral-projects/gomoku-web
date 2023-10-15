insert into dbo.GameVariants (variant)
values ('FREESTYLE'),
       ('RENJU'),
       ('CARO'),
       ('OMOK'),
       ('NINUKI_RENJU'),
       ('PENTE');

insert into dbo.OpeningRules (rule)
values ('PRO'),
       ('LONG_PRO'),
       ('SWAP'),
       ('SWAP2');

insert into dbo.BoardSizes (size)
values (15),
       (19);

insert into dbo.Users (username, email, password_validation)
values ('user1', 'user1@example.com', 'password_hash_1'),
       ('user2', 'user2@example.com', 'password_hash_2'),
       ('user3', 'user3@example.com', 'password_hash_3'),
       ('user4', 'user4@example.com', 'password_hash_4'),
       ('user5', 'user5@example.com', 'password_hash_5');

insert into dbo.Tokens (token_validation, user_id)
values ('token_1', 1),
       ('token_2', 2),
       ('token_3', 3),
       ('token_4', 4),
       ('token_5', 5);

insert into dbo.Statistics (user_id, points, games_played, games_won)
values (1, 1000, 10, 5),
       (2, 4350, 8, 3),
       (3, 1303, 6, 2),
       (4, 1531, 10, 5),
       (5, 6122, 10, 5);

insert into dbo.Lobbies (host_id, game_variant, opening_rule, board_size)
values (1, 'FREESTYLE', 'PRO', 15),
       (2, 'RENJU', 'LONG_PRO', 19),
       (3, 'OMOK', 'SWAP', 15);

insert into dbo.Games (state, game_variant, opening_rule, board_size, board, host_id, guest_id)
values ('FINISHED', 'FREESTYLE', 'PRO', 15, '{"grid":["c9-w","d8-b","a6-w","b7-b","b11-b"],"turn":{"player":"b","timeLeftInSec":28}}', 1, 3),
       ('IN_PROGRESS', 'RENJU', 'LONG_PRO', 15, '{"grid":["j6-w","k7-b","l8-w","m9-b","a10-w"],"turn":{"player":"w","timeLeftInSec":3}}', 4, 5),
       ('FINISHED', 'OMOK', 'SWAP', 19, '{"grid":["e1-b","f2-w","g3-b","h4-w","i5-b"],"turn":{"player":"w","timeLeftInSec":57}}', 3, 5);
