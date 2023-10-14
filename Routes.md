# Rotas

### 1. Create User (Sign Up)
- **URI:** `/api/users`
- **Method:** `POST`
- **Body:**
  ```json
  { 
   "username": "user1",
   "email": "user123@gmail.com" (Será que é preciso confirmar o email?)
   "password": "pass1"
  }
- **Response**
  ```json
  { 
    "userId": "123",
    "message": "User created successfully"
   }

### 2. Login (Token)
- **URI:** `/api/users/login`
- **Method:** `POST`
- **Body:**
  ```json
  { 
   "username": "user1",
   "password": "pass1"
  }
- **Response**
  ```json
  { 
   "token": "abc123",
  }

### 3. Get Game Status 
- **URI:** `/api/games/:gameId/status`
- **Method:** `GET`
- **Headers:** `{ "Authorization": "Bearer abc123" }`
- **Response**
  ```json
  { 
    "gameId": "456", 
    "status": "in progress",
    "currentPlayer": "user1",
    "boardState": [...]
   }

### 4. Make a move on the board
- **URI:** `/api/games/:gameId/moves`
- **Method:** `PUT`
- **Headers:** `{ "Authorization": "Bearer abc123" }`
- **Response**
  ```json
  { 
    "move": "X to position 0,0",
    "userId": "123" 
  }

### 5. Get User Status (User Status)
- **URI:** `/api/users/:userId/status`
- **Method:** `GET`
- **Headers:** `{ "Authorization": "Bearer abc123" }`
- **Response:**
  ```json
  { 
     "userId": "123", 
     "status": "online",
     "Number of wins:2", 
     "Number games played":10,
     "Number of loses":8,
     "currentGame": "456" 
  }
  
### 6. Get Users Ranking (Users Ranking)
-We need to define the ranking system
- **URI:** `/api/users/ranking`
- **Method:** `GET`
- **Headers:** `{"Authorization": "Bearer abc123" }`
- **Response:**
  ```json
  { 
    "ranking": [
      {
        "userId": "123",
        "username": "user1",
        "wins": 2,
        "loses": 8,
        "gamesPlayed": 10
      },
      {
        "userId": "456",
        "username": "user2",
        "wins": 5,
        "loses": 5,
        "gamesPlayed": 10
      }
    ]
  }

### 7. Search for a Game with a specific variant
- On this route in cause of a game not being found, the user will create a game and will wait for another player to join the game.
- **URI:** `/api/games?variant=15`
- **Method:** `GET`
- **Headers:** `{ "Authorization": "Bearer abc123" }`
- **Response**
  ```json
  { 
    "response" : "Joined with sucess to the game 456"
  }

### 8. Create a Game 
- **URI:** `/api/games`
- **Method:** `POST`
- **Header:** `{ "Authorization": "Bearer abc123" }`
- **Body:**
  ```json
  {
   "variant" : "15" (example) 
   }

### 9. Create a Game and invite a friend
- **URI:** `/api/games/private`
- **Method:** `POST`
- **Headers:** `{ "Authoriztion" : "Bearer abc123"}`
- **Body:**
  ```json
  {
   "variant" : "15" (example),
   "friendId" : "123"
   }
  
### 10.Logout
- **URI:** `/api/users/logout`
- **Method:** `POST`
- **Headers:** `{ "Authoriztion" : "Bearer abc123"}`
- **Response:**
  ```json
  {
   "message" : "User logged out with sucess"
   }

### 11. Exit a Game
- **URI:** `/api/games/:gameId/exit`
- **Method:** `POST`
- **Headers:** `{ "Authoriztion" : "Bearer abc123"}`
- **Response:**
  ```json
  {
   "message" : "User exited the game with sucess"
   }
  
### 12.Edit User Profile
- **URI:** `/api/users/:userId`
- **Method:** `PUT`
- **Headers:** `{ "Authoriztion" : "Bearer abc123"}`
- **Body:**
  ```json
  {
   "username" : "user1",
   "old-password" : "1234",
   "new-password" : "12345"
  }
  

### 13.Get the infomation of the apllication
- **URI:** `/api/info`
- **Method:** `GET`
