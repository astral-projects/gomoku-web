/*-- Insert data into dbo.Lobby
INSERT INTO dbo.Lobby (joined_at)
VALUES ('2023-10-10 12:00:00'),
       ('2023-10-11 14:30:00'),
       ('2023-10-12 10:15:00');

-- Insert data into dbo.Game
INSERT INTO dbo.Game (state, board, size, variant, opening_rule)
VALUES ('WAITING FOR PLAYERS',
        '{"data": {"moves": [{"x": 1,"y": ''f'',"color": "BLACK"},{"x": 2,"y": ''e'',"color": "WHITE"}],"turn": "BLACK"}}',
        15, 'FREESTYLE', 'PRO'),
       ('PLAYING',
        '{"data": {"moves": [{"x": 1,"y": ''f'',"color": "WHITE"},{"x": 2,"y": ''e'',"color": "BLACK"}],"turn": "WHITE"}}',
        15, 'RENJU', 'LONG-PRO'),
       ('FINISHED',
        '{"data": {"moves": [{"x": 1,"y": ''f'',"color": "BLACK"},{"x": 2,"y": ''e'',"color": "WHITE"}],"turn": "BLACK"}}',
        19, 'CARO', 'SWAP2');

-- Insert data into dbo.User
INSERT INTO dbo.User (username, email, password_validation, lobby, game)
VALUES ('user1', 'user1@example.com', 'password_hash_1', 1, 1),
       ('user2', 'user2@example.com', 'password_hash_2', 1, 1),
       ('user3', 'user3@example.com', 'password_hash_3', 2, 3);

-- Insert data into dbo.Token
INSERT INTO dbo.Token (token_validation, created_at, last_used_at, user_id)
VALUES ('token_1', 1633824000, 1633904000, 1),
       ('token_2', 1633910400, 1634004000, 2),
       ('token_3', 1634090400, 1634184000, 3);

-- Insert data into dbo.Statistic
INSERT INTO dbo.Statistic (user_id, rank, points, games_played, games_won)
VALUES (1, 1, 100, 10, 5),
       (2, 2, 80, 8, 3),
       (3, 3, 60, 6, 2);
*/
