# Rotas

### 1. Criar Utilizador (Sign Up)
- **URI:** `/api/users`
- **Método:** `POST`
- **Body:**
  ```json
  { "username": "user1", "password": "pass1" }
- **Respota**
  ```json
  { "userId": "123", "message": "User created successfully" }

### 2. Login (Token)
- **URI:** `/api/users/login`
- **Método:** `POST`
- **Body:**
  ```json
  { "username": "user1", "password": "pass1" }
- **Respota**
  ```json
  { "token": "abc123", "userId": "123" }

### 3. Pedido para jogar(Encontrar um jogo)
- **URI:** `/api/games`
- **Método:** `POST`
- **Headers:** `{ "Authorization": "Bearer abc123" }`
- **Body:**
  ```json
  { "userId": "123" }
- **Respota**
  ```json
  { "gameId": "456", "message": "Game created" }

### 4. Obter informação do jogo criado (Game status)
- **URI:** `/api/games/:gameId/status`
- **Método:** `GET`
- **Headers:** `{ "Authorization": "Bearer abc123" }`
- **Respota**
  ```json
  { "gameId": "456", "status": "in progress", "currentPlayer": "user1", "boardState": [...] }

### 5. Fazer uma jogada
- **URI:** `/api/games/:gameId/moves`
- **Método:** `PUT`
- **Headers:** `{ "Authorization": "Bearer abc123" }`
- **Respota**
  ```json
    { "move": "X to position 0,0", "userId": "123" }

### 6. Obter o estado do jogo
- **URI:** `/api/games/:gameId`
- **Método:** `GET`
- **Headers:** `{ "Authorization": "Bearer abc123" }`
- **Respota**
  ```json
  { "gameId": "456", "status": "in progress", "winner": NULL, "boardState": [...] }