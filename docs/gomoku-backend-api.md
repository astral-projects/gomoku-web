# Gomoku - API Documentation 🉐

> This document contains the HTTP API documentation required for a frontend client application to use this API.

> **Note**: This documentation does not contain any information about the internal backend implementation. To learn more
> about the backend implementation, please refer to the [backend documentation](../code/jvm/docs/README.md).

## Table of Contents

- [Introduction](#introduction)
- [Functionality](#functionality)
- [Media Types](#media-types)
  - [Siren](#siren)
- [Link Relations](#link-relations)
- [Navigation Graph](#navigation-graph)
- [Requests](#requests)
  - [Home](#home)
  - [User](#user)
  - [Game](#game)
  - [Lobby](#lobby)
  - [System](#system)
- [Responses](#responses)
  - [Headers](#headers)
  - [Problem](#problem)
- [Resources](#resources)
  - [Get home](#get-home)
  - [Register a new user](#register-a-new-user)
  - [Login a user](#login-a-user)
  - [Logout a user](#logout-a-user)
  - [Get home authenticated](#get-home-authenticated)
  - [Get a user](#get-a-user)
  - [Get user stats](#get-user-stats)
  - [Get users stats](#get-users-stats)
  - [Get users stats by search term](#get-users-stats-by-search-term)
  - [Find a game](#find-a-game)
  - [Get a game](#get-a-game)
  - [Exit a game](#exit-a-game)
  - [Make a game move](#make-a-game-move)
  - [Get game variants](#get-game-variants)
  - [Check lobby status](#check-lobby-status)
  - [Exit a lobby](#exit-a-lobby)
  - [Get system information](#get-system-information)
- [Representations](#representations)
  - [Game Representation](#game-representation)
  - [Paginated Result Representation](#paginated-result-representation)
---

## Introduction

This API is a RESTful API designed to be consumed by a frontend client application.

## Functionality

The API provides the following functionality:

- Creating games, leaving games and lobbies and matchmaking;
- In-game actions, such as placing pieces on a board;
- User authentication;
- Consult user(s) statistical information.

## Media Types

The API uses the following media types:

- `application/json` - for the API request bodies;
- `application/vnd.siren+json` - [Siren](https://github.com/kevinswiber/siren) for the API response bodies;
- `application/problem+json` - [Problem](https://tools.ietf.org/html/rfc7807) for the API responses in
  case of errors;

### Siren

The following properties were added to the siren representation of the API:

> **Recipe Links**:
> To indicate the available resources of the API.

> **Require Auth**:
> To indicate if the resource requires authentication or not.

## Link Relations

The API uses the following [link relations](https://www.iana.org/assignments/link-relations/link-relations.xhtml)
represented in the file [Rels.kt](../code/jvm/src/main/kotlin/gomoku/http/Rels.kt):

- Every link relation that is undocumented in the previous link is a custom link relation created by the API. Every link
  is represented as an uri with the following format:
  `https://github.com/isel-leic-daw/2023-daw-leic51d-14/tree/main/code/jvm/docs/rels/{resource}`.
  - `self` - the link to the current resource;
  - `system-info` - the link to the system information resource template;
  - `login` - the link to the login resource template;
  - `logout` - the link to the logout resource template;
  - `register` - the link to the register resource template;
  - `me` - the link to the logged-in user resource template;
  - `users/user` - the link to the user resource template;
  - `find-game` - the link to the games resource template;
  - `games/game` - the link to the game resource template;
  - `games/game/move` - the link to the game move resource template;
  - `games/variants` - the link to the game variants resource template;
  - `games/game/exit-game` - the link to the game exit resource template;
  - `lobbies/lobby` - the link to the lobby resource template;
  - `lobbies/lobby/exit-lobby` - the link to the lobby exit resource template;
  - `users/user/search` - the link to the user statistic information resource template;
  - `users/stats` - the link to the users statistic information resource template;
  - `users/search` - the link to the users statistic information by search term resource template;

## URI Templates

The API uses the following URI templates
represented in the file [UriTemplates.kt](../code/jvm/src/main/kotlin/gomoku/http/UriTemplates.kt):

If the URI is not documented in the following link, it is the actual URI of the resource.
- Every URI template that is undocumented in the previous link is a custom URI template created by the API. Every URI
  template is represented as an uri with a base following format:
  `/api`.
- `/users/{user_id}` - the user resource template;
- `/games/{game_id}` - the game resource template;
- `/games/{game_id}/move` - the game move resource template;
- `/games/{game_id}/exit` - the game exit resource template;
- `/lobby/{lobby_id}` - the lobby resource template;
- `/lobby/{lobby_id}/exit` - the lobby exit resource template;
- `/users/{user_id}/stats` - the user statistic information resource template;
- `/users/stats?q={query}{&page,itemsPerPage}` - the users statistic information resource template;
- `/users/stats/search?q={query}{&page,itemsPerPage}` - the users statistic information by search term resource
  template;

## Navigation Graph

| ![Navigation Graph](../docs/diagrams/navigation-graph.png) |
|:----------------------------------------------------------:|
|                    **Navigation Graph**                    |

## Requests

Information about the requests:

- For endpoints marked with 🔒 (indicating authentication is required):
    - Include an `Authorization` header using the `Bearer` scheme, with the user's `token`.
  - If the request is done by a browser-based client, the browser will send cookies automatically with each request.
    Supported browsers: `Chrome, Firefox, Safari`.
- For endpoints marked with 📦 (indicating a request body is required):
    - Include a request body with the required information.
    - Ensure the `Content-Type` header is set to `application/json`.
- For endpoints marked with 📖 (indicating the response is paginated):
    - Include the following optional query parameters:
        - `page` - the page number (defaults to `1`);
        - `itemsPerPage` - the number of items per page (defaults to `10`);
- All endpoints should be prefixed with `/api`.

### Home

- `GET /` - returns the home page information and the available resources of the API; See [Get home](#get-home) for more
  information.

### User

- `POST /users 📦` - creates a new user. See [Register a new user](#register-a-new-user) for more information;
- `POST /users/token 📦` - authenticates a user. See [Login a user](#login-a-user) for more information;
- `POST /users/logout 🔒` - invalidates a user's token. See [Logout a user](#logout-a-user) for more information;
- `GET /users/home 🔒` - returns logged-in user's information. See [Get home authenticated](#get-home-authenticated) for
  more information;
- `GET /users/{id}` - returns the user with the given id. See [Get a user](#get-a-user) for more information;
- `GET /users/{id}/stats` - returns the user statistic information with the given id.
  See [Get user stats](#get-user-stats) for more information;
- `GET /users/stats 📖` - returns the users statistic information by ranking. See [Get users stats](#get-users-stats) for
  more information;
- `GET /users/stats/search 📖` - returns the users statistic information by search term.
  See [Get users stats by search term](#get-users-stats-by-search-term) for more information.

### Game

- `POST /games 🔒📦` - joins a lobby or creates a new game with the given variant id. See [Find a game](#find-a-game) for
  more information;
- `GET /games/{id}` - returns the game with the given id. See [Get a game](#get-a-game) for more information;
- `POST /games/{id}/exit` 🔒 - exits the game with the given id. See [Exit a game](#exit-a-game) for more information;
- `POST /games/{id}/move 🔒📦` - makes a move in the game with the given id. See [Make a game move](#make-a-game-move) for
  more
- `POST /games/variants` - returns the game variants. See [Get game variants](#get-game-variants) for more information;

### Lobby

- `GET /lobby/{id} 🔒` - checks the status of the lobby with the given id. See [Check lobby status](#check-lobby-status)
  for more information;
- `DELETE /lobby/{id}/exit 🔒` - deletes the lobby with the given id. See [Exit a lobby](#exit-a-lobby) for more

### System

- `GET /system` - returns the system information. See [Get system information](#get-system-information) for more

## Responses

### Headers

All responses have the following headers:

- `Request-Id` with a unique `UUID` for the request, mainly used for debugging purposes.
- `Content-Type` with the media type of the response body.
- `WWW-Authenticate` with the authentication scheme used by the API, when the authentication failed.

### Problem

> [!NOTE]
> The API uses the [Problem Details for HTTP APIs](https://tools.ietf.org/html/rfc7807) specification to represent
> errors.

#### Example

> [!IMPORTANT]
> An optional data field was added to the problem representation of the API,
> to provide additional information about the request problem.
>
> Status: `400 BAD REQUEST`

```json
{
  "type": "https://github.com/2023-daw-leic51d-14/code/jvm/docs/problems/invalid-move",
  "title": "Invalid move",
  "status": 400,
  "detail": "The move is invalid because the square is already occupied with a piece.",
  "instance": "/api/games/{id}/move",
  "data": null
}
```

## Resources

### Get home

> [!NOTE]
> Returns the home page information and the available resources of the API.

#### HTTP Response Status codes

| Status Code | Description |
|:-----------:|:-----------:|
|     200     |     OK      |

#### Request Example

```curl
curl http://localhost/api/
```

#### Response Example

> [!IMPORTANT]
> Some recipe links were omitted for readability purposes.
>
> Status: `200 OK`

<details>
  <summary>Click to expand example</summary>

```json
{
  "class": [
    "home"
  ],
  "properties": {
    "message": "Welcome to Gomoku API."
  },
  "links": [
    {
      "rel": [
        "self"
      ],
      "href": "/api/"
    }
  ],
  "recipeLinks": [
    {
      "rel": [
        "https://github.com/isel-leic-daw/2023-daw-leic51d-14/tree/main/code/jvm/docs/rels/system"
      ],
      "href": "/api/system"
    },
    {
      "rel": [
        "https://github.com/isel-leic-daw/2023-daw-leic51d-14/tree/main/code/jvm/docs/rels/users/home"
      ],
      "href": "/api/users/home"
    }
  ],
  "actions": [],
  "entities": [],
  "requireAuth": [
    false
  ]
}
```

</details>

### Register a new user

> [!NOTE]
> Register a new user in the system.

#### Body Parameters

- **Username** - the user's username.
  - Required: `true`
  - Type: `string`
  - Length Range: `5-30`

- **Email** - the user's email.
  - Required: `true`
  - Type: `string`
  - Regex: `^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$`

- **Password** - the password of the user.
  - Required: `true`
  - Type: `string`
  - Length Range: `8-40`

#### HTTP Response Status codes

| Status Code | Description |
|:-----------:|:-----------:|
|     201     |   Created   |
|     400     | Bad Request |

#### Request Example

```curl
curl \
  -X POST \
  -H "Accept: application/json" \
  http://localhost/api/users \
  -d '{"username":"postman-user","email":"email@valid.com","password":"postman-password"}'
```

#### Response Example

> [!IMPORTANT]
> Status: `201 CREATED`

<details>
  <summary>Click to expand example</summary>

```json
{
  "class": [
    "user",
    "login"
  ],
  "properties": {
    "id": 895
  },
  "links": [
    {
      "rel": [
        "https://github.com/isel-leic-daw/2023-daw-leic51d-14/tree/main/code/jvm/docs/rels/user"
      ],
      "href": "/api/users/895"
    },
    {
      "rel": [
        "self"
      ],
      "href": "/api/users/token"
    }
  ],
  "recipeLinks": [],
  "actions": [
    {
      "name": "login",
      "href": "/api/users/token",
      "method": "POST",
      "type": "application/json",
      "fields": [
        {
          "name": "username",
          "type": "text",
          "value": null
        },
        {
          "name": "password",
          "type": "text",
          "value": null
        }
      ],
      "requireAuth": [
        false
      ]
    }
  ],
  "entities": [],
  "requireAuth": [
    false
  ]
}
```

</details>

### Login a user

> [!NOTE]
> Login a user in the system.

#### Body Parameters

- **Username** - the user's username.
  - Required: `true`
  - Type: `string`
  - Length Range: `5-30`
- **Password** - the password of the user.
  - Required: `true`
  - Type: `string`
  - Length Range: `8-40`

#### HTTP Response Status codes

| Status Code | Description |
|:-----------:|:-----------:|
|     200     |     OK      |
|     400     | Bad Request |

#### Request Example

```curl
curl \
  -X POST \
  -H "Accept: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  http://localhost/api/users/token \
  -d '{"username":"postman-user","password":"postman-password"}'
```

#### Response Example

> [!IMPORTANT]
> Status: `200 OK`

<details>
  <summary>Click to expand example</summary>

```json
{
  "class": [
    "token",
    "user"
  ],
  "properties": {
    "token": "A_1bgxfmE7zZJN7xwh67-ruKI8AcrKJP4MprsXxyfFA="
  },
  "links": [
    {
      "rel": [
        "self"
      ],
      "href": "/api/users/token"
    }
  ],
  "recipeLinks": [],
  "actions": [],
  "entities": [
    {
      "properties": {
        "id": {
          "value": 895
        },
        "username": {
          "value": "postman-username"
        },
        "email": {
          "value": "example@postman.com"
        },
        "passwordValidation": {
          "validationInfo": "$2a$10$j/t4mb2yyUHJu5pZxpSGfepbh/suWqqG82CqFle0etSsnin4p3dJO"
        }
      },
      "links": [
        {
          "rel": [
            "self"
          ],
          "href": "/api/users/895"
        }
      ],
      "rel": [
        "https://github.com/isel-leic-daw/2023-daw-leic51d-14/tree/main/code/jvm/docs/rels/user"
      ]
    }
  ],
  "requireAuth": [
    false
  ]
}
```

</details>

### Logout a user

> [!NOTE]
> Logout a user in the system.

> [!WARNING]
> Requires authentication. See [Requests](#requests) for more information.

#### HTTP Response Status codes

| Status Code | Description  |
|:-----------:|:------------:|
|     200     |      OK      |
|     401     | Unauthorized |

#### Request Example

```curl
curl  \
-X POST \
-H "Authorization: Bearer <TOKEN>" \
http://localhost/api/users/logout
```

#### Response Example

> [!IMPORTANT]
> Status: `200 OK`

<details>
  <summary>Click to expand example</summary>

```json
{
  "class": [
    "logout"
  ],
  "properties": {
    "message": "User logged out successfully, token was revoked."
  },
  "links": [
    {
      "rel": [
        "self"
      ],
      "href": "/api/users/logout"
    }
  ],
  "recipeLinks": [],
  "actions": [],
  "entities": [],
  "requireAuth": [
    true
  ]
}
```

</details>

### Get home authenticated

> [!NOTE]
> Returns the logged-in user's information.

> [!WARNING]
> Requires authentication. See [Requests](#requests) for more information.

#### HTTP Response Status codes

| Status Code | Description  |
|:-----------:|:------------:|
|     200     |      OK      |
|     401     | Unauthorized |

#### Request Example

```curl
curl \ 
-H "Authorization: Bearer <TOKEN>" \
http://localhost/api/users/home
```

#### Response Example

> [!IMPORTANT]
> Status: `200 OK`

<details>
  <summary>Click to expand example</summary>

```json
{
  "class": [
    "home",
    "users-stats",
    "user-stats",
    "system-info",
    "find-game",
    "logout"
  ],
  "properties": {
    "id": {
      "value": 895
    },
    "username": {
      "value": "postman-username"
    },
    "email": {
      "value": "example@postman.com"
    },
    "passwordValidation": {
      "validationInfo": "$2a$10$j/t4mb2yyUHJu5pZxpSGfepbh/suWqqG82CqFle0etSsnin4p3dJO"
    }
  },
  "links": [
    {
      "rel": [
        "self"
      ],
      "href": "/api/"
    }
  ],
  "recipeLinks": [],
  "actions": [
    {
      "name": "find-game",
      "href": "/api/games",
      "method": "POST",
      "type": "application/json",
      "fields": [],
      "requireAuth": [
        true
      ]
    },
    {
      "name": "logout",
      "href": "/api/users/logout",
      "method": "POST",
      "type": "application/json",
      "fields": [],
      "requireAuth": [
        true
      ]
    }
  ],
  "entities": [
    {
      "properties": {
        "id": {
          "value": 895
        },
        "username": {
          "value": "postman-username"
        },
        "email": {
          "value": "example@postman.com"
        },
        "points": {
          "value": 0
        },
        "rank": {
          "value": 1
        },
        "gamesPlayed": {
          "value": 0
        },
        "wins": {
          "value": 0
        },
        "draws": {
          "value": 0
        },
        "losses": {
          "value": 0
        }
      },
      "links": [
        {
          "rel": [
            "self"
          ],
          "href": "/api/users/895/stats"
        }
      ],
      "rel": [
        "https://github.com/isel-leic-daw/2023-daw-leic51d-14/tree/main/code/jvm/docs/rels/user-stats"
      ]
    },
    {
      "properties": {
        "currentPage": 1,
        "itemsPerPage": 1,
        "totalPages": 611,
        "items": [
          {
            "id": {
              "value": 2
            },
            "username": {
              "value": "user2"
            },
            "email": {
              "value": "user2@example.com"
            },
            "points": {
              "value": 4350
            },
            "rank": {
              "value": 2
            },
            "gamesPlayed": {
              "value": 8
            },
            "wins": {
              "value": 3
            },
            "draws": {
              "value": 0
            },
            "losses": {
              "value": 5
            }
          }
        ]
      },
      "links": [
        {
          "rel": [
            "self"
          ],
          "href": "/api/users/stats?page=1&itemsPerPage=1"
        },
        {
          "rel": [
            "next"
          ],
          "href": "/api/users/stats?page=2&itemsPerPage=1"
        },
        {
          "rel": [
            "last"
          ],
          "href": "/api/users/stats?page=611&itemsPerPage=1"
        }
      ],
      "rel": [
        "https://github.com/isel-leic-daw/2023-daw-leic51d-14/tree/main/code/jvm/docs/rels/users-stats"
      ]
    },
    {
      "properties": {
        "gameName": "Gomoku Royale API",
        "version": "0.0.1",
        "description": "Gomoku Royale is an online multiplayer strategy game where players compete to connect five of their pieces in a row, column or diagonally.",
        "releaseDate": "18/12/2023",
        "authors": [
          {
            "firstName": "Diogo",
            "lastName": "Rodrigues",
            "gitHubUrl": "https://github.com/Diogofmr"
          },
          {
            "firstName": "Tiago",
            "lastName": "Frazão",
            "gitHubUrl": "https://github.com/TiagoFrazao01"
          },
          {
            "firstName": "Francisco",
            "lastName": "Engenheiro",
            "gitHubUrl": "https://github.com/FranciscoEngenheiro"
          }
        ]
      },
      "links": [
        {
          "rel": [
            "self"
          ],
          "href": "/api/system"
        }
      ],
      "rel": [
        "https://github.com/isel-leic-daw/2023-daw-leic51d-14/tree/main/code/jvm/docs/rels/system-info"
      ]
    }
  ],
  "requireAuth": [
    true
  ]
}
```

</details>

### Get a user

> [!NOTE]
> Returns the user with the given id.

#### Path Parameters

- **id** - the user's id.
  - Required: `true`
  - Type: `number`
  - Range: `1-`

#### HTTP Response Status codes

| Status Code | Description |
|:-----------:|:-----------:|
|     200     |     OK      |
|     400     | Bad Request |
|     404     |  Not Found  |

#### Request Example

```curl
curl http://localhost/api/users/1
```

#### Response Example

> [!IMPORTANT]
> Status: `200 OK`

<details>
  <summary>Click to expand example</summary>

```json
{
  "class": [
    "user"
  ],
  "properties": {
    "id": {
      "value": 1
    },
    "username": {
      "value": "user1"
    },
    "email": {
      "value": "user1@example.com"
    },
    "passwordValidation": {
      "validationInfo": "password_hash_1"
    }
  },
  "links": [
    {
      "rel": [
        "self"
      ],
      "href": "/api/users/1"
    }
  ],
  "recipeLinks": [],
  "actions": [],
  "entities": [],
  "requireAuth": [
    false
  ]
}
```

</details>

### Get user stats

> [!NOTE]
> Returns the user statistic information with the given id.

#### Path Parameters

- **id** - the user's id.
  - Required: `true`
  - Type: `number`
  - Range: `1-`

#### HTTP Response Status codes

| Status Code | Description |
|:-----------:|:-----------:|
|     200     |     OK      |
|     404     |  Not Found  |

#### Request Example

```curl
curl http://localhost/api/users/1/stats
```

#### Response Example

> [!IMPORTANT]
> Status: `200 OK`

<details>
  <summary>Click to expand example</summary>

```json
{
  "class": [
    "user-stats"
  ],
  "properties": {
    "id": {
      "value": 6
    },
    "username": {
      "value": "postman-username5"
    },
    "email": {
      "value": "example@postman.com5"
    },
    "points": {
      "value": 0
    },
    "rank": {
      "value": 1
    },
    "gamesPlayed": {
      "value": 0
    },
    "wins": {
      "value": 0
    },
    "draws": {
      "value": 0
    },
    "losses": {
      "value": 0
    }
  },
  "links": [
    {
      "rel": [
        "self"
      ],
      "href": "/api/users/6/stats"
    }
  ],
  "recipeLinks": [],
  "actions": [],
  "entities": [],
  "requireAuth": [
    true
  ]
}
```

</details>

### Get users stats

> [!NOTE]
> Returns the users statistic information by ranking.
>
> The result is paginated.

#### Query Parameters

- **page** - the page number.
  - Required: `false`
  - Type: `number`
  - Default: `1`
  - Range: `1-`

- **itemsPerPage** - the number of items to return per page.
  - Required: `false`
  - Type: `number`
  - Default: `10`
  - Range: `1-`

#### HTTP Response Status codes

| Status Code | Description |
|:-----------:|:-----------:|
|     200     |     OK      |
|     400     | Bad Request |

#### Request Example

```curl
curl http://localhost/api/users/stats?page=3&itemsPerPage=2
```

#### Response Example

> [!IMPORTANT]
> Links can be used to navigate through the pages.
>
> Status: `200 OK`

<details>
  <summary>Click to expand example</summary>

```json
{
  "class": [
    "users-stats"
  ],
  "properties": {
    "currentPage": 3,
    "itemsPerPage": 2,
    "totalPages": 306,
    "items": [
      {
        "id": {
          "value": 302
        },
        "username": {
          "value": "user-6649887803439842203"
        },
        "email": {
          "value": "email@5567147137422571053.com"
        },
        "points": {
          "value": 500
        },
        "rank": {
          "value": 6
        },
        "gamesPlayed": {
          "value": 1
        },
        "wins": {
          "value": 1
        },
        "draws": {
          "value": 0
        },
        "losses": {
          "value": 0
        }
      },
      {
        "id": {
          "value": 304
        },
        "username": {
          "value": "user-8097313227960329506"
        },
        "email": {
          "value": "email@798740636865958576.com"
        },
        "points": {
          "value": 500
        },
        "rank": {
          "value": 6
        },
        "gamesPlayed": {
          "value": 1
        },
        "wins": {
          "value": 1
        },
        "draws": {
          "value": 0
        },
        "losses": {
          "value": 0
        }
      }
    ]
  },
  "links": [
    {
      "rel": [
        "self"
      ],
      "href": "/api/users/stats?page=3&itemsPerPage=2"
    },
    {
      "rel": [
        "next"
      ],
      "href": "/api/users/stats?page=4&itemsPerPage=2"
    },
    {
      "rel": [
        "last"
      ],
      "href": "/api/users/stats?page=305&itemsPerPage=2"
    },
    {
      "rel": [
        "prev"
      ],
      "href": "/api/users/stats?page=2&itemsPerPage=2"
    },
    {
      "rel": [
        "first"
      ],
      "href": "/api/users/stats?page=1&itemsPerPage=2"
    }
  ],
  "recipeLinks": [],
  "actions": [],
  "entities": [],
  "requireAuth": [
    false
  ]
}
```

</details>

### Get users stats by search term

> [!NOTE]
> Returns the users statistic information by search term.
>
> The result is paginated.

#### Query Parameters

- **term** - the search term.
  - Required: `true`
  - Type: `string`

- **page** - the page number.
  - Required: `false`
  - Type: `number`
  - Default: `1`
  - Range: `1-`

- **itemsPerPage** - the number of items to return per page.
  - Required: `false`
  - Type: `number`
  - Default: `10`
  - Range: `1-`

#### HTTP Response Status codes

| Status Code | Description |
|:-----------:|:-----------:|
|     200     |     OK      |
|     400     | Bad Request |

#### Request Example

```curl
curl http://localhost/api/users/stats/search?term=user&page=3&itemsPerPage=2
```

### Response Example

> [!IMPORTANT]
> Links can be used to navigate through the pages.
>
> Status: `200 OK`

<details>
  <summary>Click to expand example</summary>

```json
{
  "class": [
    "users-stats"
  ],
  "properties": {
    "currentPage": 3,
    "itemsPerPage": 2,
    "totalPages": 280,
    "items": [
      {
        "id": {
          "value": 758
        },
        "username": {
          "value": "user-4464902552321055112"
        },
        "email": {
          "value": "email@4941312777461605655.com"
        },
        "points": {
          "value": 500
        },
        "rank": {
          "value": 6
        },
        "gamesPlayed": {
          "value": 1
        },
        "wins": {
          "value": 1
        },
        "draws": {
          "value": 0
        },
        "losses": {
          "value": 0
        }
      },
      {
        "id": {
          "value": 304
        },
        "username": {
          "value": "user-8097313227960329506"
        },
        "email": {
          "value": "email@798740636865958576.com"
        },
        "points": {
          "value": 500
        },
        "rank": {
          "value": 6
        },
        "gamesPlayed": {
          "value": 1
        },
        "wins": {
          "value": 1
        },
        "draws": {
          "value": 0
        },
        "losses": {
          "value": 0
        }
      }
    ]
  },
  "links": [
    {
      "rel": [
        "self"
      ],
      "href": "/api/users/stats/search?term=user&page=3&itemsPerPage=2"
    },
    {
      "rel": [
        "next"
      ],
      "href": "/api/users/stats/search?term=user&page=4&itemsPerPage=2"
    },
    {
      "rel": [
        "last"
      ],
      "href": "/api/users/stats/search?term=user&page=279&itemsPerPage=2"
    },
    {
      "rel": [
        "prev"
      ],
      "href": "/api/users/stats/search?term=user&page=2&itemsPerPage=2"
    },
    {
      "rel": [
        "first"
      ],
      "href": "/api/users/stats/search?term=user&page=1&itemsPerPage=2"
    }
  ],
  "recipeLinks": [],
  "actions": [],
  "entities": [],
  "requireAuth": [
    true
  ]
}
```

</details>

### Find a game

> [!NOTE]
> Joins a lobby or creates a new game with the given variant id.

> [!WARNING]
> Requires authentication. See [Requests](#requests) for more information.

#### Body Parameters

- **variantId** - the game variant id.
  - Required: `true`
  - Type: `number`
  - Range: `1-`

#### HTTP Response Status codes

| Status Code | Description  |
|:-----------:|:------------:|
|     201     |   Created    |
|     400     | Bad Request  |
|     401     | Unauthorized | 

#### Request Example

```curl
curl \
  -X POST \
  -H "Accept: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  http://localhost/api/games \
  -d '{"variantId":1}'
```

#### Response Example

> [!IMPORTANT]
> Lobby created example.
>
> Status: `201 CREATED`

<details>
  <summary>Click to expand example</summary>

```json
{
  "class": [
    "game",
    "make-move",
    "exit-game"
  ],
  "properties": {
    "id": 1,
    "state": {
      "name": "finished"
    },
    "variant": {
      "id": 1,
      "name": "freestyle",
      "openingRule": "none",
      "boardSize": 15
    },
    "board": {
      "grid": [],
      "winner": "B"
    },
    "createdAt": "2023-12-09T15:41:13Z",
    "updatedAt": "2023-12-09T15:41:14Z",
    "hostId": 7,
    "guestId": 8
  },
  "links": [
    {
      "rel": [
        "self"
      ],
      "href": "/api/games/1"
    }
  ],
  "recipeLinks": [],
  "actions": [
    {
      "name": "Make Move",
      "href": "/api/games/1/move",
      "method": "POST",
      "type": "application/json",
      "fields": [
        {
          "name": "col",
          "type": "text",
          "value": null
        },
        {
          "name": "row",
          "type": "number",
          "value": null
        }
      ],
      "requireAuth": [
        true
      ]
    },
    {
      "name": "Exit Game",
      "href": "/api/games/1/exit",
      "method": "POST",
      "type": "application/json",
      "fields": [],
      "requireAuth": [
        true
      ]
    }
  ],
  "entities": [
    {
      "properties": {
        "id": 7,
        "username": "user-1132257373105219752",
        "email": "email@1489387210923067713.com"
      },
      "links": [
        {
          "rel": [
            "self"
          ],
          "href": "/api/users/7"
        }
      ],
      "rel": [
        "https://github.com/isel-leic-daw/2023-daw-leic51d-14/tree/main/code/jvm/docs/rels/user"
      ]
    },
    {
      "properties": {
        "id": 8,
        "username": "user-8038795331941020073",
        "email": "email@1019835312111379755.com"
      },
      "links": [
        {
          "rel": [
            "self"
          ],
          "href": "/api/users/8"
        }
      ],
      "rel": [
        "https://github.com/isel-leic-daw/2023-daw-leic51d-14/tree/main/code/jvm/docs/rels/user"
      ]
    }
  ],
  "requireAuth": [
    false
  ]
}
```

</details>

> [!IMPORTANT]
> Game created example.
>
> Status: `201 CREATED`

<details>
  <summary>Click to expand example</summary>

```json
{
  "class": [
    "game",
    "make-move",
    "exit-game"
  ],
  "properties": {
    "message": "Joined the game successfully with id=87",
    "id": 87
  },
  "links": [
    {
      "rel": [
        "self"
      ],
      "href": "/api/games/87"
    }
  ],
  "recipeLinks": [],
  "actions": [
    {
      "name": "Make Move",
      "href": "/api/games/87/move",
      "method": "POST",
      "type": "application/json",
      "fields": [
        {
          "name": "col",
          "type": "text",
          "value": null
        },
        {
          "name": "row",
          "type": "number",
          "value": null
        }
      ],
      "requireAuth": [
        true
      ]
    },
    {
      "name": "Exit Game",
      "href": "/api/games/87/exit",
      "method": "POST",
      "type": "application/json",
      "fields": [],
      "requireAuth": [
        true
      ]
    }
  ],
  "entities": [],
  "requireAuth": [
    true
  ]
}
```

</details>

### Get a game

> [!NOTE]
> Returns the game with the given id.

#### Path Parameters

- **id** - the game's id.
  - Required: `true`
  - Type: `number`
  - Range: `1-`

#### HTTP Response Status codes

| Status Code | Description |
|:-----------:|:-----------:|
|     200     |     OK      |
|     404     |  Not Found  |

#### Request Example

```curl
curl http://localhost/api/games/1
```

#### Response Example

> [!IMPORTANT]
> Status: `200 OK`

<details>
  <summary>Click to expand example</summary>

```json
{
  "class": [
    "game",
    "make-move",
    "exit-game"
  ],
  "properties": {
    "id": 1,
    "state": {
      "name": "finished"
    },
    "variant": {
      "id": 1,
      "name": "freestyle",
      "openingRule": "none",
      "boardSize": 15
    },
    "board": {
      "grid": [],
      "winner": "B"
    },
    "createdAt": "2023-12-09T15:41:13Z",
    "updatedAt": "2023-12-09T15:41:14Z",
    "hostId": 7,
    "guestId": 8
  },
  "links": [
    {
      "rel": [
        "self"
      ],
      "href": "/api/games/1"
    }
  ],
  "recipeLinks": [],
  "actions": [
    {
      "name": "Make Move",
      "href": "/api/games/1/move",
      "method": "POST",
      "type": "application/json",
      "fields": [
        {
          "name": "col",
          "type": "text",
          "value": null
        },
        {
          "name": "row",
          "type": "number",
          "value": null
        }
      ],
      "requireAuth": [
        true
      ]
    },
    {
      "name": "Exit Game",
      "href": "/api/games/1/exit",
      "method": "POST",
      "type": "application/json",
      "fields": [],
      "requireAuth": [
        true
      ]
    }
  ],
  "entities": [
    {
      "properties": {
        "id": 7,
        "username": "user-1132257373105219752",
        "email": "email@1489387210923067713.com"
      },
      "links": [
        {
          "rel": [
            "self"
          ],
          "href": "/api/users/7"
        }
      ],
      "rel": [
        "https://github.com/isel-leic-daw/2023-daw-leic51d-14/tree/main/code/jvm/docs/rels/user"
      ]
    },
    {
      "properties": {
        "id": 8,
        "username": "user-8038795331941020073",
        "email": "email@1019835312111379755.com"
      },
      "links": [
        {
          "rel": [
            "self"
          ],
          "href": "/api/users/8"
        }
      ],
      "rel": [
        "https://github.com/isel-leic-daw/2023-daw-leic51d-14/tree/main/code/jvm/docs/rels/user"
      ]
    }
  ],
  "requireAuth": [
    false
  ]
}
```

</details>

### Make a game move

> [!NOTE]
> Makes a move in the game with the given id.

> [!WARNING]
> Requires authentication. See [Requests](#requests) for more information.

#### Path Parameters

- **id** - the game's id.
  - Required: `true`
  - Type: `number`
  - Range: `1-`

#### Body Parameters

- **col** - the column of the move.
  - Required: `true`
  - Type: `number`
  - Range: `a-z`

- **row** - the row of the move.
  - Required: `true`
  - Type: `number`
  - Range: `1-`

#### HTTP Response Status codes

| Status Code | Description  |
|:-----------:|:------------:|
|     200     |      OK      |
|     400     | Bad Request  |
|     401     | Unauthorized |
|     404     |  Not Found   |

#### Request Example

```curl
curl \
  -X POST \
  -H "Accept: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  http://localhost/api/games/1/move \
  -d '{"col":"a","row":1}'
```

#### Response Example

> [!IMPORTANT]
> Status: `200 OK`

<details>
  <summary>Click to expand example</summary>

```json
{
  "class": [
    "game"
  ],
  "properties": {
    "id": 87,
    "state": {
      "name": "in_progress"
    },
    "variant": {
      "id": 1,
      "name": "freestyle",
      "openingRule": "none",
      "boardSize": 15
    },
    "board": {
      "grid": [
        "a7-w"
      ],
      "turn": {
        "player": "B",
        "timeLeftInSec": {
          "value": 60
        }
      }
    },
    "createdAt": "2023-12-18T13:50:34Z",
    "updatedAt": "2023-12-18T14:00:09Z",
    "hostId": 895,
    "guestId": 896
  },
  "links": [
    {
      "rel": [
        "self"
      ],
      "href": "/api/games/87/move"
    }
  ],
  "recipeLinks": [],
  "actions": [],
  "entities": [
    {
      "properties": {
        "id": 87,
        "state": {
          "name": "in_progress"
        },
        "variant": {
          "id": 1,
          "name": "freestyle",
          "openingRule": "none",
          "boardSize": 15
        },
        "board": {
          "grid": [
            "a7-w"
          ],
          "turn": {
            "player": "B",
            "timeLeftInSec": {
              "value": 60
            }
          }
        },
        "createdAt": "2023-12-18T13:50:34Z",
        "updatedAt": "2023-12-18T14:00:09Z",
        "hostId": 895,
        "guestId": 896
      },
      "links": [
        {
          "rel": [
            "self"
          ],
          "href": "/api/games/87"
        }
      ],
      "rel": [
        "https://github.com/isel-leic-daw/2023-daw-leic51d-14/tree/main/code/jvm/docs/rels/game"
      ]
    }
  ],
  "requireAuth": [
    true
  ]
}
```

</details>

### Exit a game

> [!NOTE]
> Exits the game with the given id.

> [!WARNING]
> Requires authentication. See [Requests](#requests) for more information.

#### Path Parameters

- **id** - the game's id.
  - Required: `true`
  - Type: `number`
  - Range: `1-`

#### HTTP Response Status codes

| Status Code | Description  |
|:-----------:|:------------:|
|     200     |      OK      |
|     401     | Unauthorized |
|     404     |  Not Found   |

#### Request Example

```curl
curl \
  -X POST \
  -H "Authorization: Bearer <TOKEN>" \
  http://localhost/api/games/87/exit
```

#### Response Example

> [!IMPORTANT]
> Status: `200 OK`

<details>
  <summary>Click to expand example</summary>

```json
{
  "class": [
    "game"
  ],
  "properties": {
    "userId": 895,
    "gameId": 87,
    "message": "User with id <895> left the Game with id <87>."
  },
  "links": [
    {
      "rel": [
        "self"
      ],
      "href": "/api/games/87/exit"
    }
  ],
  "recipeLinks": [],
  "actions": [],
  "entities": [],
  "requireAuth": [
    true
  ]
}
```

</details>

### Get game variants

> [!NOTE]
> Returns the available game variants.

#### HTTP Response Status codes

| Status Code | Description |
|:-----------:|:-----------:|
|     200     |     OK      |

#### Request Example

```curl
curl http://localhost/api/games/variants
```

#### Response Example

> [!IMPORTANT]
> Status: `200 OK`

<details>
  <summary>Click to expand example</summary>

```json
{
  "class": [
    "variants"
  ],
  "properties": [
    {
      "id": {
        "value": 1
      },
      "name": "FREESTYLE",
      "openingRule": "NONE",
      "boardSize": "FIFTEEN"
    },
    {
      "id": {
        "value": 2
      },
      "name": "OMOK",
      "openingRule": "PRO",
      "boardSize": "NINETEEN"
    }
  ],
  "links": [
    {
      "rel": [
        "self"
      ],
      "href": "/api/games/variants"
    }
  ],
  "recipeLinks": [],
  "actions": [],
  "entities": [],
  "requireAuth": [
    false
  ]
}
```

</details>

### Check lobby status

> [!NOTE]
> Checks lobby current status.
>
> Serves as a polling endpoint for the client application to check if the user is still waiting in the lobby,
> or if a second player has joined the lobby.

> [!WARNING]
> Requires authentication. See [Requests](#requests) for more information.

#### Path Parameters

- **id** - the lobby's id.
  - Required: `true`
  - Type: `number`
  - Range: `1-`

#### HTTP Response Status codes

| Status Code | Description  |
|:-----------:|:------------:|
|     200     |      OK      |
|     401     | Unauthorized |
|     404     |  Not Found   |
|     403     |  Forbidden   |

#### Request Example

```curl
curl \
  -X POST \
  -H "Authorization: Bearer <TOKEN>" \
  http://localhost/api/lobby/89
```

#### Response Example

> [!IMPORTANT]
> Lobby is still waiting for a second player to join.
>
> Status: `200 OK`

<details>
 <summary>Click to expand example</summary>

```json
{
  "class": [
    "lobby",
    "exit-lobby"
  ],
  "properties": {
    "message": "Waiting in lobby with id <91>",
    "id": 91
  },
  "links": [
    {
      "rel": [
        "self"
      ],
      "href": "/api/lobby/91"
    }
  ],
  "recipeLinks": [],
  "actions": [
    {
      "name": "Exit Lobby",
      "href": "/api/lobby/91/exit",
      "method": "DELETE",
      "type": "application/json",
      "fields": [],
      "requireAuth": [
        true
      ]
    }
  ],
  "entities": [],
  "requireAuth": [
    true
  ]
}
```

</details>

> [!IMPORTANT]
> A second player has joined the lobby.
>
> Status: `200 OK`

<details>
  <summary>Click to expand example</summary>

```json
{
  "class": [
    "game",
    "exit-game",
    "make-move"
  ],
  "properties": {
    "message": "Already in game with id <88>",
    "id": 88
  },
  "links": [
    {
      "rel": [
        "self"
      ],
      "href": "/api/games/88"
    }
  ],
  "recipeLinks": [],
  "actions": [
    {
      "name": "Exit Game",
      "href": "/api/games/88",
      "method": "POST",
      "type": "application/json",
      "fields": [],
      "requireAuth": [
        true
      ]
    },
    {
      "name": "Make Move",
      "href": "/api/games/88",
      "method": "POST",
      "type": "application/json",
      "fields": [
        {
          "name": "col",
          "type": "text",
          "value": null
        },
        {
          "name": "row",
          "type": "number",
          "value": null
        }
      ],
      "requireAuth": [
        true
      ]
    }
  ],
  "entities": [],
  "requireAuth": [
    true
  ]
}
```

</details>

### Exit a lobby

> [!NOTE]
> Exits the lobby with the given id.

> [!WARNING]
> Requires authentication. See [Requests](#requests) for more information.

#### Path Parameters

- **id** - the lobby's id.
  - Required: `true`
  - Type: `number`
  - Range: `1-`

#### HTTP Response Status codes

| Status Code | Description  |
|:-----------:|:------------:|
|     200     |      OK      |
|     401     | Unauthorized |
|     404     |  Not Found   |

#### Request Example

```curl
curl http://localhost/api/lobby/92/exit
```

#### Response Example

> [!IMPORTANT]
> Status: `200 OK`

<details>
  <summary>Click to expand example</summary>

```json
{
  "class": [
    "lobby-exit"
  ],
  "properties": {
    "lobbyId": 92,
    "message": "Lobby was exited successfully."
  },
  "links": [
    {
      "rel": [
        "self"
      ],
      "href": "/api/lobby/92/exit"
    }
  ],
  "recipeLinks": [],
  "actions": [],
  "entities": [],
  "requireAuth": [
    true
  ]
}
```

</details>

### Get system information

> [!NOTE]
> Returns the system information.

#### HTTP Response Status codes

| Status Code | Description |
|:-----------:|:-----------:|
|     200     |     OK      |

#### Request Example

```curl
curl http://localhost/api/system
```

#### Response Example

> [!IMPORTANT]
> Status: `200 OK`

<details>
  <summary>Click to expand example</summary>

```json
{
  "class": [
    "system-info"
  ],
  "properties": {
    "gameName": "Gomoku Royale API",
    "version": "0.0.1",
    "description": "Gomoku Royale is an online multiplayer strategy game where players compete to connect five of their pieces in a row, column or diagonally.",
    "releaseDate": "18/12/2023",
    "authors": [
      {
        "firstName": "Diogo",
        "lastName": "Rodrigues",
        "gitHubUrl": "https://github.com/Diogofmr"
      },
      {
        "firstName": "Tiago",
        "lastName": "Frazão",
        "gitHubUrl": "https://github.com/TiagoFrazao01"
      },
      {
        "firstName": "Francisco",
        "lastName": "Engenheiro",
        "gitHubUrl": "https://github.com/FranciscoEngenheiro"
      }
    ]
  },
  "links": [
    {
      "rel": [
        "self"
      ],
      "href": "/api/system"
    }
  ],
  "recipeLinks": [],
  "actions": [],
  "entities": [],
  "requireAuth": [
    false
  ]
}
```

</details>

## Representations

### Game Representation

> [!IMPORTANT]
> The `state` property can only be `in_progress` or `finished`.
>
> The `winner` property can only be `B` or `W`.
>
> The `turn` property can only be `B` or `W`.
>
> Both `winner` and `turn` properties might not be present in the representation, depending on the game state:
> - If the state is `in_progress` the `turn` property is present.
> - If the state is `finished`, the `turn` property is not present, and:
    >
- a draw is represented by the absence of the `winner` property.
>   - a win is represented by the presence of the `winner` property.
>
> The `createdAt` and `updatedAt` properties are in [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601)
> format `YYYY-MM-DDTHH:MM:SSZ`.
>
> Moves in the `grid` property are represented as `colrow-player` format where `col` is the column,
> `row` is the row and `player` is the player that made the move (`w`-White, `b`-Black).

```json
{
  "id": 87,
  "state": {
    "name": "finished"
  },
  "variant": {
    "id": 1,
    "name": "freestyle",
    "openingRule": "none",
    "boardSize": 15
  },
  "board": {
    "grid": [
      "a7-w"
    ],
    "winner": "B"
  },
  "createdAt": "2023-12-18T13:50:34Z",
  "updatedAt": "2023-12-18T14:00:56Z",
  "hostId": 895,
  "guestId": 896
}
```

### Paginated Result Representation

> [!IMPORTANT]
>
> The `currentPage` property is the current page number.
>
> The `itemsPerPage` property is the number of items per page, that could be less or equal to `itemsPerPage` query
> parameter.
>
> The `totalPages` property is the total number of pages that can be transversed with the received `itemsPerPage` query
> parameter.
>
> The `items` property is an array of items of the retrieved page.

```json
{
  "currentPage": 1,
  "itemsPerPage": 10,
  "totalPages": 57,
  "items": []
}
```