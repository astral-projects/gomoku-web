package gomoku.services.game

import gomoku.domain.game.Game
import gomoku.utils.Either

sealed class GameCreationError {
    object UserAlreadyInLobby : GameCreationError()
    object GameNotFound : GameCreationError()
}

typealias GameCreationResult = Either<GameCreationError, Boolean>

sealed class GettingGameError {
    object GameNotFound : GettingGameError()
}

typealias GettingGameResult = Either<GettingGameError, Game>

sealed class GamePutError {
    object GameNotFound : GamePutError()
}

typealias GamePutResult = Either<GamePutError, Boolean>