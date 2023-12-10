# Gomoku - API Documentation 🉐

> This document contains the HTTP API documentation required for a frontend client application to use this API.

> **Note**: This documentation does not contain any information about the internal backend implementation. To learn more
> about the backend implementation, please refer to the [backend documentation](../code/jvm/docs/README.md).

## Table of Contents

- [Introduction](#introduction)
- [Functionality](#functionality)
- [Media Types](#media-types)
- [Navigation Graph](#navigation-graph)
- [Requests](#requests)
    - [User](#user)
    - [Game](#game)
- [Responses](#responses)
- [Usage Examples](#usage-examples)
    - [User Creation](#user-creation)
    - [User Login](#user-login)
    - [User Logout](#user-logout)
    - [Game Creation](#game-creation)
    - [Game Move](#game-move)
    - [Pagination](#pagination)

---

## Introduction

This API is a RESTful API that is designed to be consumed by a frontend client application.

## Functionality

The API provides the following functionality:

- Creating games, leaving games and matchmaking;
- In-game actions, such as placing pieces on a board;
- User authentication;
- Consult user/s statistical information.

## Media Types

The API uses the following media types:

- `application/vnd.siren+json` - for the API response bodies;
- `application/problem+json` - [RFC7807](https://tools.ietf.org/html/rfc7807) problem details for the API responses in
  case of errors;

## Navigation Graph

| ![Navigation Graph](../docs/diagrams/navigation-graph.png) |
|:----------------------------------------------------------:|
|                    **Navigation Graph**                    |

## Requests

Information about the requests:

- For endpoints marked with 🔒 (indicating authentication is required):
    - Include an `Authorization` header using the `Bearer` scheme, with the user's `token`.
      If the request is done by a browser, a cookie will be sent automatically.
      Supported browsers are Chrome, Firefox and
      Safari.
- For endpoints marked with 📦 (indicating a request body is required):
    - Include a request body with the required information.
    - Ensure the `Content-Type` header is set to `application/json`.
- For endpoints marked with 📖 (indicating the response is paginated):
    - Include the following optional query parameters:
        - `page` - the page number (defaults to `1`);
        - `itemsPerPage` - the number of items per page (defaults to `10`);
- All endpoints should be prefixed with `/api`.

### User

The API provides the following operations/resources related to the `User` entity:

- `POST /users 📦` - creates a new user; See [User Creation](#user-creation) for more information;
- `POST /users/token 📦` - authenticates a user; See [User Login](#user-login) for more information;
- `POST /users/logout 🔒` - invalidates a user's token; See [User Logout](#user-logout) for more information;
- `GET /users/home 🔒` - returns logged-in user's information;
- `GET /users/{id}` - returns the user with the given id; See [Get User](#get-user-by-id) for more information;
- `GET /users/stats 📖` - returns the users statistic information by ranking; See [Pagination](#pagination) for more
  information.
- `GET /users/{id}/stats` - returns the user statistic information with the given id.
  See [Get User Stats](#get-user-stats) for more information.

### Game

The API provides the following operations/resources related to the `Game` entity:

- `POST /games 🔒📦` - joins a lobby or creates a new game with the given variant id; See [Game Creation](#game-creation)
  for more information;
- `GET /games/{id}` - returns the game with the given id;
- `DELETE /games/{id}` 🔒 - deletes the game with the given id;
- `GET /system` - returns the system information;
- `POST /games/{id}/move 🔒📦` - makes a move in the game with the given id; See [Game Move](#game-move) for more
  information;
- `POST /games/{id}/exit 🔒` - exits the game with the given id.
- `GET /games/lobby/{id} 🔒` - Checks the status of the lobby with the given ID, returning whether the user is still
  waiting in the lobby or has already entered a game.
- `DELETE /games/lobby/{id} 🔒` - deletes the lobby with the given id.

### Responses

Information about the responses:

- All responses have a `Request-Id` header with a unique `UUID` for the request, used for debugging purposes. Also if
  someone report a bug in the future, this header can be used to identify the request.

## Usage Examples

### User Creation

- The client application makes a `POST` request to the `register` resource, with the **user's credentials** in the
  request
  body. The request body should be a JSON object with the following properties:
    - `username` - the user's username (must be between `5 and 30 characters` long);
    - `email` - the user's email (must follow the following regex: `^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$`);
    - `password` - the user's password (must be between `8 and 40 characters` long);

  Example:
  ```json
  {
    "username": "postman-user",
    "email": "email@validemail.com",
    "password": "postman-password"
  }
  ```
- The API then:
    - **On Success** - creates a new user with the provided credentials and returns a `201 Created` response with the *
      *user id**
      in the response body.

      Example:
      ```json
      {
         "id": {
            "value": 1
         }
      }
      ```
    - **On Failure Example** - returns a `400 Bad Request` response with a message in the response body.

      Example:
      ```json
      {
         "type": "https://github.com/2023-daw-leic51d-14/code/jvm/docs/problems/insecure-password",
         "title": "Received password is considered insecure",
         "status": 400,
         "detail": "Password length must be between 8 and 40 characters",
         "instance": "/api/users"
      }
      ```

### User Login

- The client application makes a `POST` request to the `login` resource, with the user's credentials in the request
  body. The request body should be a JSON object with the following properties:
    - `username` - the user's username (must be between `5 and 30 characters` long);
    - `password` - the user's password (must be between `8 and 40 characters` long).

  Example:
    ```json
    {
        "username": "postman-user",
        "password": "postman-password"
    }
    ```

- The API then:
    - **On Success** - authenticates the user and returns a `200 OK` response with a message in the response body. The
      response body
      contains the following properties:
        - `token` - the user's access token should be used in the `Authorization` header with
          the `Bearer` scheme for all the requests that require authentication.

      Example:
      ```json 
      {
         "token": "G87BsVDnFbHezX7_gkXcqBjB-VPUUsTj33S3NFzUDq0="
      }
      ```
    - **On Failure Example** - returns a `400 Bad Request` response with a message in the response body.

      ```json
      {
         "type": "https://github.com/2023-daw-leic51d-14/code/jvm/docs/problems/invalid-username",
         "title": "Invalid username",
         "status": 400,
         "detail": "The username <postman-user> is invalid",
         "instance": "/api/users/token"
      }
      ```

- The client application should store the user token in a secure place, such as the browser's local storage or a
  cookie.

### User Logout

- The client application makes a `POST` request to the `logout` resource, with the user token in the `Authorization`
  header with the `Bearer` scheme;
- The API then:
    - **On Success** - invalidates the user's token and returns a `200 OK` response with a message in the response body.

      Example:
      ```json
      { 
         "message": "User was logged out successfully."
      }
      ```

    - **On Failure Example** - returns a `401 Unauthorized` response with a message in the response body.

      ```json
      {
        "type": "https://github.com/2023-daw-leic51d-14/code/jvm/docs/problems/token-is-invalid",
        "title": "Invalid token",
        "status": 401,
        "detail": "The token received is invalid",
        "instance": "/api/users/logout"
      }
      ```

- To log in again, the client application should request a new token to the API.

### Get User By Id

- The client application makes a `GET` request to the `users/{id}` resource, with the user id in the request path;
    - The Api then:
        - **On Success**
            - returns a `200 OK` response with the user information in the response body. The response body
              contains the following properties:
                - `id` - the user's id;
                - `username` - the user's username;
                - `email` - the user's email;

              Example:
              ```json
              {
                "id": 42,
                "username": "user example",
                "email": "user1@example.com"
              }
                ```
        - **On Failure**
            - returns a `400 Bad Request` if the id is not valid. Response with a message in the response body.

              Example:
                ```json
                  {
                    "type": "https://github.com/isel-leic-daw/2023-daw-leic51d-14/tree/main/code/jvm/docs/problems/invalid-id",
                    "title": "Invalid user id",
                    "status": 400,
                    "detail": "The user id must be a positive integer",
                    "instance": "/api/users/-1",
                    "data": null
                  }
                ```
            - returns a `404 Not Found` if the user with the given id does not exist. Response with a message in the
              response body.

              Example:
              ```json
                {
                    "type": "https://github.com/isel-leic-daw/2023-daw-leic51d-14/tree/main/code/jvm/docs/problems/user-not-found",
                    "title": "User not found",
                    "status": 404,
                    "detail": "The user with id <90> was not found",
                    "instance": "/api/users/90",
                    "data": null
                }
              ```

### Get User Stats

- The client application makes a `GET` request to the `users` resource, with the id in the path.
  The response should be in body.
    - The API then:
    - **On Success** - returns a `200 OK` response with the user statistic information in the response body. The
      response body
      contains the following properties:
        - `id` - the user's id;
        - `username` - the user's username;
        - `email` - the user's email;
        - `points` - the user's points;
        - `rank` - the user's rank;
        - `gamesPlayed` - the user's games played;
        - `wins` - the user's wins;
        - `draws` - the user's draws;
        - `losses` - the user's losses;

      Example:
      ```json
      {
          "id": 1,
          "username": "user1",
          "email": "user1@example.com",
          "points": 1000,
          "rank": 1,
          "gamesPlayed": 10,
          "wins": 5,
          "draws": 0,
          "losses": 5
      }
      ```
    - **On Failure** - returns a `404 Not Found` if the user with the given id does not exist. Response with a message
      in the response body.

      Example:
      ```json
      {
          "type": "https://github.com/isel-leic-daw/2023-daw-leic51d-14/tree/main/code/jvm/docs/problems/user-not-found",
          "title": "User not found",
          "status": 404,
          "detail": "The user with id <9> was not found",
          "instance": "/api/users/9/stats",
          "data": null
      }
      ```

### Game Creation

- The client application makes a `POST` request to the `games` resource, with the variant id in the request
  body. The request body should be a JSON object with the following properties:

  ```json 
  {
    "variantId": 1
  }
  ```

- The API then:
    - **On Success**
        - **Lobby created** - creates a new game only if another user with the same variant id is waiting at a lobby. If
          there is no
          user waiting for a game, the API creates a new lobby with the given variant id and returns a `201 Created`
          response
          with the lobby id and a message in the response body;

          Example:
          ```json
          {
            "id": {
              "value": 1
            },
            "message": "Waiting for another player to join the game."
          }
          ```
        - **Game created** - creates a new game with the provided variant id and returns a `201 Created` response with
          the
          game id and a message in the response body.

          Example:
          ```json
          {
             "id": {
                "value": 1
             },
             "message": "Game created successfully."
          }
          ```
    - **On Failure Example** - returns a `400 Bad Request` response with a message in the response body.

      ```json
      {
         "type": "https://github.com/2023-daw-leic51d-14/code/jvm/docs/problems/game-variant-not-found",
          "title": "Game variant not found",
          "status": 400,
          "detail": "The game variant with id <1> was not found",
          "instance": "/api/games"
      }
      ```

### Game Move

- The client application makes a `POST` request to the `games/{id}/move` resource, with the move information in the
  request body. The request body should be a JSON object with the following properties:
    - `col` - the column of the square where player will play (must be between `a` and `z`);
    - `row` - the row of the square where player will play (must be minimum `1`).
      Example:
      ```json
      {
        "col": "a",
        "row": 11
      }
      ```

- The API then:
    - **On Success** - makes the move and returns a `200 OK` response with a message in the response body.

      Example:
      ```json
       {
          "message": "Move was made successfully."
       }
      ```

    - **On Failure Example** - returns a `400 Bad Request` response with a message in the response body.

      ```json
      {
         "type": "https://github.com/2023-daw-leic51d-14/code/jvm/docs/problems/invalid-move",
         "title": "Invalid move",
         "status": 400,
         "detail": "The move is invalid because the square is already occupied with a piece.",
         "instance": "/api/games/{id}/move"
      }
      ``` 

### Pagination

- The client application makes a `GET` request a resource marked as paginated.

  Example:
  ```text
   GET /api/users/stats?limit=0&offset=10
  ```

- The API then returns a `200 OK` response with the requested page in the response body. The response body contains the
  following properties:
    - `totalItems` - the total number of items available in this resource;
    - `currentPage` - the current page number;
    - `itemsPerPage` - the number of items per page, that could be less or equal to the `limit` query parameter;
    - `totalPages` - the total number of pages that can be transversed with the received `limit` query parameter;
    - `items` - the items in the current page.

  Example:

  ```json
  {
    "totalItems": 151,
    "currentPage": 1,
    "itemsPerPage": 10,
    "totalPages": 16,
    "items": [
      {
        "id": {
          "value": 5
        },
        "username": {
          "value": "user5"
        },
        "email": {
          "value": "user5@example.com"
        },
        "points": {
          "value": 6122
        },
        "rank": {
          "value": 1
        },
        "gamesPlayed": {
          "value": 10
        },
        "wins": {
          "value": 5
        },
        "draws": {
          "value": 0
        },
        "losses": {
          "value": 5
        }
      }
    ] 
    // ...
  }
  ```