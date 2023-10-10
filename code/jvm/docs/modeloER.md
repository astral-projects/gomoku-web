# Modelo Relacional

## User

- User (`user_id`, username, email, password_validation, lobby, game)

| Attribute           | Type         | Description                              |
|---------------------|--------------|------------------------------------------|
| user_id             | serial       | Primary key                              |
| username            | varchar(255) |                                          |
| email               | varchar(255) | Secondary key                            |
| password_validation | varchar(255) |                                          |
| lobby               | integer      | Foreign Key references `Lobby(lobby_id)` |
| game                | integer      | Foreign Key references `Game(game_id)`   |

## Token

- Token (`token_validation`, created_at, last_used_at, user_id)

| Attribute        | Type         | Description                            |
|------------------|--------------|----------------------------------------|
| token_validation | varchar(255) | Primary key                            |
| created_at       | bigint       |                                        |
| last_used_at     | bigint       |                                        |
| user_id          | serial       | Foreign key references `User(user_id)` |

## Lobby

- Lobby (`lobby_id`, joined_at)

| Attribute | Type      | Description                         |
|-----------|-----------|-------------------------------------|
| lobby_id  | serial    | Primary key                         |
| joined_at | timestamp | Time that the user joined the lobby |           

## Game

- Game (`game_id`, state, size, variant, board, opening_rule)

| Attribute    | Type         | Description                                                                     |
|--------------|--------------|---------------------------------------------------------------------------------|
| game_id      | serial       | Primary key                                                                     |
| state        | varchar(255) | Can take values: "waiting", "playing", "finished"                               |
| size         | integer      | The size of the table that the player want play                                 |
| variant      | varchar(255) | Can take values: "freestyle", "renju", "caro", "omok", "Ninuki-renju", "Pente". |
| board        | jsonb        |                                                                                 |                                                                                                                                   
| opening_rule | varchar(255) | Can take values: "pro", "Long pro", "swap", "swap2".                            |

## Statistic

- Statistic (`user_id`, games_played, rank, points, games_won)

| Attribute    | Type    | Description                             |
|--------------|---------|-----------------------------------------|
| user_id      | integer | Foreign Key references `User(user_id)`  |
| rank         | integer | Rank of a player.                       |                                      
| points       | integer | Total points of a player.               |                                        
| games_won    | integer | All The games that a player has won.    |
| games_played | integer | All The games that a player has played. |



