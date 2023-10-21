# Gomoku Royale

## Project Description

This project consists of the development of a [Gomoku](https://en.wikipedia.org/wiki/Gomoku) game, which will be played
by two players.

This project is divided into two phases:

- The first phase consists of the development of the **backend service**.
  - [Backend Documentation](code/jvm/docs/README.md)
  - [Backend API Documentation](docs/gomoku-backend-api.md)
- The second phase consists of the development of a **browser-based frontend application**, which will use the backend
  service developed in the first phase.

## Functionality

The HTTP API should provide the functionality required for a front-end application to:


## Get the information of the application
- Obtain information about the system, such as the system authors and the system version, by an unauthenticated user.
- **URI:** `/api/system`
- **Method:** `GET`
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


### Get Users Ranking (Users Ranking)
- Obtain statistical and ranking information, such as number of played games and users ranking, by an unauthenticated
  user.
- We need to define the ranking system
- **URI:** `/api/users/ranking`
- **Method:** `GET`
  - **Response:**
    ```json 
    { 
      "status": "success",
      "id": "J0K1A2B3C4D5E6F7G8H9I0J",
      "reason": "Obtain statistical and ranking information",
      "data": {
        "items": [
          {
              "username": "user1",
              "rank": 1,
              "wins": 2,
              "loses": 8,
              "points": 1000,
              "gamesPlayed": 10
          },  
          {
            "username": "user2",
            "rank": 4,
            "wins": 2,
            "loses": 18,
            "points": 4874,
            "gamesPlayed": 20
          },
          {
            "username": "Nathan Drake",
            "rank": 6,
            "wins": 20,
            "loses": 6,
            "points": 10000,
            "gamesPlayed": 26
          }
        ],
        "totalItems": 256,
        "currentPage" : 1,
        "itemsPerPage": 3,
        "totalPages": 86
      }
    }
    ```

### Register a new User (Sign Up)
- Allow a user to register a new account. The user will provide a username, an email and a password.This will be done by a form in the frontend application.
- **URI:** `/api/users`
- **Method:** `POST`
    - Request(Body information json):
      ```json
      {
        "username": "Arthur Morgan",
        "email": "outlawOfTheWest@gmail.com",
        "password": "redemption",
        "passwordConfirmation": "redemption"
      }
      ```
    - Response with sucess:
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
    - Response (Error):    
      ```json
      {
            "status": "error",
            "id": "B2C3D4E5F6G7H8I9J0K1A2B3",
            "type": "https://example.com/probs/invalid-data",
            "title": "Invalid password confirmation",
            "instance": "/register",
            "detail": "Register new user",
            "data": {
              "message": "Please make sure that your password confirmation matches your password." 
            }
       }
      ```

### Get User Status 
- Allow a user to obtain their own statistics.
- **URI:** `/api/users/:userId`
- **Method:** `GET`
- **Headers:** `{ "Authorization": "Bearer abc123" }`
  - Allow a user to obtain a user statistics.
      - Request:
          The user id comes in the path. Ex.: http://localhost:8080/users/1
      - Response:
    ```json
      {
        "status": "success",
        "id": "C3D4E5F6G7H8I9J0K1A2B3C4",
        "reason": "Obtain user details",
        "data": {
          "status": "online",
          "username": "Geralt of Rivia",
          "email": "geralt@gmail.com",
          "points": 5421,
          "rank": 1,
          "gamesPlayed": 256,
          "gamesWon": 128,
          "gamesLost": 128  
        }
      }
      ```
      - Response(error)
          -This functionally can produce a error for example de userID not being valid
          ```json
            {
            "status": "error",
            "id": "C3D4E5F6G7H8I9J0K1A2B3C4",
            "type": "https://example.com/probs/invalid-data",
            "title": "Invalid variant",
            "instance": "/users",
            "detail": "Obtain user details",
            "data": {
              "message": "The userId you have chosen is not valid. Please choose another userId."
              }
            }
          ```
        
  
### Search for a new Game with the desired options by the user
- - Allow a user to express their desire to start a new game - users will enter a waiting lobby, where a matchmaking
    algorithm will select pairs of users and start games with them.
- **URI:** `/api/games?variant=15&&openingRule=swap&&size=15`
- **Method:** `POST`
- **Headers:** `{ "Authorization": "Bearer abc123" }`
 - Request:
    ```json
    {
      "variant": "omok",
      "openingRule": "swap",
      "size": 15
    }
    ```
  - Response:
  ```json
  {
    "status": "success",
    "id": "C3D4E5F6G7H8I9J0K1A2B3C4",
    "reason": "Start new game",
    "data": {
      "message": "You have entered the waiting lobby. The matchmaking algorithm will pair you with an opponent. Please wait for the game to start.",
      "lobby": {
        "variant": "omok", 
        "openingRule": "swap",
        "size": 15
      }
    }
  }
  ```

 - Response(error):
    - This route can have multiple error.For example if the user put some invalid options or if the user is already in a game.
    ```json
    {
      "status": "error",
      "id": "C3D4E5F6G7H8I9J0K1A2B3C4",
      "type": "https://example.com/probs/invalid-data",
      "title": "Invalid variant",
      "instance": "/games",
      "detail": "Start new game",
      "data": {
        "message": "The variant you have chosen is not valid. Please choose another variant."
      }
    }
    ``` 

### 3. Get Game Status
- Allow a user to observe the game state.
- **URI:** `/api/games/:id/status`
- **Method:** `GET`
- **Headers:** `{ "Authorization": "Bearer abc123" }`
    - **Request**
      The game id comes in the path. Ex.: http://localhost:8080/games/1
    - **Response**
      ```json
        {
          "status": "success",
          "id": "D4E5F6G7H8I9J0K1A2B3C4D5",
          "reason": "Observe game state",
          "data": {
            "id": 73,
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
### Make a move on the board
- **URI:** `/api/games/:id/moves`
- **Method:** `PUT`
- **Headers:** `{ "Authorization": "Bearer abc123" }`
- **Response**

- Allow a user to play a round.
    - **Request**:
      ```json
      {
        "position": "7f",
        "id": 73
      }
      ```
    - **Response (Success)**
      ```json
      {
         "status": "success",
         "id": "E5F6G7H8I9J0K1A2B3C4D5E6",
         "reason": "Play a round",
          "data": {
            "id": 194,
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
      - **Response (Error)**
      ```json
      {
        "status": "error",
        "id": "E5F6G7H8I9J0K1A2B3C4D5E6",
        "type": "https://example.com/probs/invalid-data",
        "title": "Invalid position",
        "instance": "/play",
        "detail": "Play a round",
        "data": {
          "message": "The position you have chosen is already occupied. Please choose another position.",
        "timerLeftInSeconds": 15
        }
      }
      ```

### Logout
-This route will be used to logout the user
- **URI:** `/api/users/logout`
- **Method:** `POST`
- **Headers:** `{ "Authoriztion" : "Bearer abc123"}`
- **Response:**
  ```json
  {
         "status": "success",
         "id": "E5F6G7H8I9J0K1A2B3C4D5E6",
         "reason": "Logout",
          "data": {
               "message" : "User logged out with sucess"
          } 
  } 
  ```

### Exit a Game
- This route will be used to exit a game, but you can only exit a game if you are in a game.
- **URI:** `/api/games/:id/exit`
- **Method:** `POST`
- **Headers:** `{ "Authoriztion" : "Bearer abc123"}`
- **Response:**
  ```json
  {
    "status": "success",
         "id": "E5F6G7H8I9J0K1A2B3C4D5E6",
         "reason": "Logout",
          "data": {
               "message" : "User exited the game with sucess"
          }
   }
  ```

### Edit User Profile
- This route will be used to edit the user profile, but you can only edit the user profile if you are logged in.
- **URI:** `/api/users/:userId`
- **Method:** `PUT`
- **Headers:** `{ "Authoriztion" : "Bearer abc123"}`
- **Request**:
   ```json
   {
     "username" : "user1",
     "old-password" : "1234",
     "new-password" : "12345"
   }
   ```
- **Response**:
    ```json
    {
      "status": "success",
      "id": "E5F6G7H8I9J0K1A2B3C4D5E6",
      "reason": "Edit user profile",
      "data": {
         "message" : "User profile edited with sucess"
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
