# Gomoku Royale

## Project Description

This project consists of the development of a [Gomoku](https://en.wikipedia.org/wiki/Gomoku) game, which will be played
by two players.

This project is divided into two phases:

- The first phase consists of the development of the **backend service**.
- The second phase consists of the development of a **browser-based frontend application**, which will use the backend
  service developed in the first phase.

## Functionality

The HTTP API should provide the functionality required for a front-end application to:

- Obtain information about the system, such as the system authors and the system version, by an unauthenticated user.

  ```json
  {
    "status": "success",
    "id": "A1B2C3D4E5F6G7H8I9J0K",
    "reason": "Obtain system information",
    "data": {
      "name": "Gomoku Royale",
      "description": "Gomoku Royale is an online multiplayer strategy game where players compete to connect five of their pieces in a row, column or diagonally.",
      "developers": [
        "Diogo Rodrigues",
        "Tiago Frazão",
        "Francisco Engenheiro"
      ],
      "releaseDate": "2024-01-01",
      "version": "1.0.9"
    }
  }
  ```

- Obtain statistical and ranking information, such as number of played games and users ranking, by an unauthenticated
  user.
  ```json
  {
    "status": "success",
    "id": "J0K1A2B3C4D5E6F7G8H9I0J",
    "reason": "Obtain statistical and ranking information",
    "data": {
      "items": [
        {
          "username": "Geralt of Rivia",
          "rank": 1,
          "points": 5421
        },  
        {
          "username": "Arthur Morgan",
          "rank": 2,
          "points": 4956
        },
        {
          "username": "Nathan Drake",
          "rank": 3,
          "points": 4874
        }
      ],
      "totalItems": 256,
      "currentPage" : 1,
      "itemsPerPage": 3,
      "totalPages": 86
    }
  }
  ```
- Register a new user.
    - Request:
      ```json
      {
        "username": "Arthur Morgan",
        "email": "outlawOfTheWest@gmail.com",
        "password": "redemption",
        "passwordConfirmation": "redemption"
      }
      ```
    - Response:
      ```json
      {
        "status": "success",
        "id": "B2C3D4E5F6G7H8I9J0K1A2B3",
        "reason": "Register new user",
        "data": {
          "message": "User registered successfully."
        }
      }
      ```

      ```json
      {
        "status": "error",
        "id": "B2C3D4E5F6G7H8I9J0K1A2B3",
        "type": "https://example.com/probs/invalid-data",
        "title": "Invalid password confirmation",
        "instance": "/register",
        "reason": "Register new user",
        "data": {
          "message": "Please make sure that your password confirmation matches your password." 
        }
      }
      ```

- Allow a user to express their desire to start a new game - users will enter a waiting lobby, where a matchmaking
  algorithm will select pairs of users and start games with them.
  ```json
  {
    "status": "success",
    "id": "C3D4E5F6G7H8I9J0K1A2B3C4",
    "reason": "Start new game",
    "data": {
      "message": "You have entered the waiting lobby. The matchmaking algorithm will pair you with an opponent. Please wait for the game to start.",
      "lobby": {
        "id": 4,
        "variant": "omok",  
        "playersInQueue": 7
      }
    }
  }
  ```

- Allow a user to observe the game state.
  ```json
    {
      "status": "success",
      "id": "D4E5F6G7H8I9J0K1A2B3C4D5",
      "reason": "Observe game state",
      "data": {
        "gameId": 73,
        "openingRule": "swap",
        "variant": "renju",
        "state": "inProgress",
        "turn": {
          "current": "black",
          "timerLeftInSeconds": 28         
        },
        "players": [
          { 
            "userId": 19,
            "color": "black"
          },
          {
            "userId": 15,
            "color": "white"
          }
        ],
        "board": {
          "size": 15,
          "grid": ["b-7f", "w-9c", "b-8d", "w-6a"]
        }  
      }
    }
    ```
- Allow a user to play a round.
    - Request:
      ```json
      {
        "position": "7f",
        "gameId": 73
      }
      ```
    - Response:
      ```json
      {
        "status": "success",
        "id": "E5F6G7H8I9J0K1A2B3C4D5E6",
        "reason": "Play a round",
        "data": {
          "gameId": 194,
          "openingRule": "pro",
          "variant": "omok",
          "state": "inProgress",
          "move": {
            "player": {
              "userId": 15,
              "color": "black"
            },
            "position": "7f"
          } 
        }
      }
      ```

      ```json
      {
        "status": "error",
        "id": "E5F6G7H8I9J0K1A2B3C4D5E6",
        "type": "https://example.com/probs/invalid-data",
        "title": "Invalid position",
        "instance": "/play",
        "reason": "Play a round",
        "data": {
          "message": "The position you have chosen is already occupied. Please choose another position.",
          "timerLeftInSeconds": 15
        }
      }
      ```

### Authors

- Diogo Rodrigues - 49513
- Tiago Frazão - 48666
- Francisco Engenheiro - 49428

---
Instituto Superior de Engenharia de Lisboa
Web Applications Development
Winter Semester of 2023/2024
