# Modelo Relacional

## User

- User (`user_id`, username, email, password_validation)

| Attribute           | Type         | Description                              |
|---------------------|--------------|------------------------------------------|
| `user_id`             | serial       | Primary key                              |
| username            | varchar(255) |                                          |
| email               | varchar(255) | Secondary key                            |
| password_validation | varchar(255) |                                          |

## Token

- Token (`token_validation`, created_at, last_used_at, user_id)

| Attribute        | Type         | Description                            |
|------------------|--------------|----------------------------------------|
| `token_validation` | varchar(255) | Primary key                            |
| created_at       | bigint       |                                        |
| last_used_at     | bigint       |                                        |
| user_id          | serial       | Foreign key references `User(user_id)` |

## Statistic

- Statistic (`user_id`, games_played, rank, points, games_won)

| Attribute    | Type    | Description                             |
|--------------|---------|-----------------------------------------|
| `user_id`      | integer | Foreign Key references `User(user_id)`  |
| rank         | integer | Rank of a player.                       |                                      
| points       | integer | Total points of a player.               |                                        
| games_won    | integer | All The games that a player has won.    |
| games_played | integer | All The games that a player has played. |

## Lobby

- Lobby (`lobby_id`, `user`, joined_at, variant, opening_rule)

| Attribute | Type      | Description                         |
|-----------|-----------|-------------------------------------|
| `lobby_id`  | serial    | Primary key                         |
| joined_at | timestamp | Time that the user joined the lobby |
| variant      | varchar(255) | Can take values: "freestyle", "renju", "caro", "omok", "Ninuki-renju", "Pente". |
| opening_rule | varchar(255) | Can take values: "pro", "Long pro", "swap", "swap2".                            |
| `user`      | integer   | Foreign key references `User(user_id)` primary key |           

## Game

- Game (`game_id`, `lobby`, `user`, state, size, board)

| Attribute    | Type         | Description                                                                     |
|--------------|--------------|---------------------------------------------------------------------------------|
| `game_id`      | serial       | Primary key                                                                     |
| state        | varchar(255) | Can take values: "inProgress", "Finished" |
| size         | integer      | The size of the table that the player want play                                 |
| board        | jsonb        |                                                                                 |
| `lobby`        | integer      | Foreign key references `Lobby(lobby_id)` primary key                            |
| `user`         | integer      | Foreign key references `User(user_id)` primary key                              |




