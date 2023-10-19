insert into dbo.GameVariants (name, opening_rule, board_size)
values ('FREESTYLE', 'PRO', 15),
       ('RENJU', 'LONG_PRO', 19),
       ('CARO', 'SWAP', 15),
       ('OMOK', 'SWAP2', 15),
       ('NINUKI_RENJU', 'SWAP2', 19),
       ('PENTE', 'PRO', 19);

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

insert into dbo.Lobbies (host_id, variant_id)
values (1, 1),
       (2, 2),
       (3, 3);

insert into dbo.Games (state, variant_id, board, host_id, guest_id, lobby_id)
values ('FINISHED', 1, '{
  "type": "run",
  "size": 15,
  "grid": [
    "c9-w",
    "d8-b",
    "a6-w",
    "b7-b",
    "b11-b"
  ],
  "turn": {
    "player": "b",
    "timeLeftInSec": 28
  }
}', 1, 3, 1),
       ('IN_PROGRESS', 2, '{
         "type": "run",
         "size": 15,
         "grid": [
           "j6-w",
           "k7-b",
           "l8-w",
           "m9-b",
           "a10-w"
         ],
         "turn": {
           "player": "w",
           "timeLeftInSec": 3
         }
       }', 4, 5, 2),
       ('FINISHED', 3, '{
         "type": "run",
         "size": 15,
         "grid": [
           "e1-b",
           "f2-w",
           "g3-b",
           "h4-w",
           "i5-b"
         ],
         "turn": {
           "player": "w",
           "timeLeftInSec": 57
         }
       }', 3, 5, 3);
