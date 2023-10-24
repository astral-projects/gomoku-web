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
