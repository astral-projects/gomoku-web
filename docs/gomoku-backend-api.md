# Gomoku - API Documentation

> This document contains the HTTP API documentation required for a frontend client application to use this API.

> **Note**: This documentation does not contain any information about the internal backend implementation. To learn more
> about the backend implementation, please refer to the [backend documentation](../code/jvm/docs/README.md).

## Table of Contents

- [Introduction](#introduction)
- [Functionality](#functionality)
- [Open-API Specification](#open-api-specification)
- [Media Types](#media-types)
- [User Creation](#user-creation)
- [User Login](#user-login)
- [User Logout](#user-logout)
- [Requests](#requests)
  - [User](#user)
  - [Game](#game)

---

## Introduction

This API is a RESTful API that is designed to be consumed by a frontend client application.

## Functionality

The API provides the following functionality:

- Creating games, leaving games and matchmaking;
- In-game actions, such as placing pieces on a board.
- User authentication;
- Consult user rankings;

## Open-API Specification

The Open-API specification for this API can be found [here](../docs/gomoku-api-spec.yaml).

In our specification, we highlight the following aspects:

- The requests are split into the following groups:
    - `User` - requests related to the `User` entity;
    - `Game` - requests related to the `Game` entity;

---

## Media Types

The API uses the following media types:

- `application/json` - JSON for the API request bodies.
- `application/problem+json` - [RFC7807](https://tools.ietf.org/html/rfc7807) problem details for the API responses in
  case of errors;

### User Creation

- The client application makes a `POST` request to the `register` request, with the user's credentials in the request
  body. The request body should be a JSON object with the following properties:
    - `username` - the user's username (must be between `5 and 30 characters` long);
    - `email` - the user's email (must follow the following regex: `^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$`);
    - `password` - the user's password (must be between `8 and 40 characters` long);
- The API creates a new user with the provided credentials and returns a `201 Created` response with the user token
  in the response body. The response body should be a JSON object with the following properties:
    - `token` - the user's access token should be used in the `Authorization` header with
      the `Bearer` scheme for all the requests that require authentication;
- The client application should store the user's tokens in a secure place, such as the user's browser's local storage
  or cookies.

### User Login

- The client application makes a `POST` request to the `login` request, with the user's credentials in the request
  body. The request body should be a JSON object with the following properties:
    - `username` - the user's username (must be between `5 and 30 characters` long);
    - `password` - the user's password (must be between `8 and 40 characters` long);
- The API authenticates the user and returns a `200 OK` response with a message in the response body.

### User Logout

- The client application makes a `POST` request to the `logout` request, with the user token in the `Authorization`
  header with the `Bearer` scheme;
- The API invalidates the user's token and returns a `200 OK` response with a message in the response body.

### Requests

Information about the requests:

- For endpoints marked with 🔒 (indicating authentication is required):
  - Include an `Authorization` header using the `Bearer` scheme, with the user's `token`.
- For endpoints marked with 📦 (indicating a request body is required):
  - Include a request body with the required information.
  - Ensure the `Content-Type` header is set to `application/json`.
- All endpoints should be prefixed with `/api`.

#### User

The API provides the following operations/resources related to the `User` entity:

- `POST /users 📦` - creates a new user; See [User Creation](#user-creation) for more information;
- `POST /users/token 📦` - authenticates a user; See [User Login](#user-login) for more information;
- `POST /users/logout 🔒` - invalidates a user's token; See [User Logout](#user-logout) for more information;
- `GET /users/home 🔒` - returns logged-in user's information;
- `GET /users/{id}` - returns the user with the given id;
- `GET /users/ranking` - returns the users ranking. This route is paginated and accepts the following optional query
  parameters:
    - `offset` - the page offset (defaults to `0`);
    - `limit` - the page limit (defaults to `10`);

#### Game

The API provides the following operations/resources related to the `Game` entity:

- `POST /games 🔒📦` - joins a lobby or creates a new game with the given variant id;
- `GET /games/{id}` - returns the game with the given id;
- `DELETE /games/{id}` 🔒 - deletes the game with the given id;
- `GET /system` - returns the system information;
- `POST /games/{id}/move 🔒📦` - makes a move in the game with the given id;
- `POST /games/{id}/exit 🔒` - exits the game with the given id;
