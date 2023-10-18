package gomoku.services.game

import gomoku.domain.game.Game
import gomoku.utils.Either

sealed class GameCreationError {
    object UserAlreadyInLobby : GameCreationError()

    object GameVariantNotFound : GameCreationError()
    object UserAlreadyInGame : GameCreationError()
    object GameNotFound : GameCreationError()
}

typealias GameCreationResult = Either<GameCreationError, String>

sealed class GettingGameError {
    object GameNotFound : GettingGameError()
}

typealias GettingGameResult = Either<GettingGameError, Game>

sealed class GamePutError {
    object UserIsNotTheHost : GamePutError()
    object GameNotFound : GamePutError()
}

typealias GamePutResult = Either<GamePutError, Boolean>

sealed class GameDeleteError {
    object UserDoesntBelongToThisGame : GameDeleteError()
    object GameNotFound : GameDeleteError()
}

typealias GameDeleteResult = Either<GameDeleteError, Boolean>


sealed class GameMakeMoveError {
    object MoveNotValid : GameMakeMoveError()
    object UserDoesNotBelongToThisGame : GameMakeMoveError()
    object GameNotFound : GameMakeMoveError()
}

typealias GameMakeMoveResult = Either<GameMakeMoveError, Boolean>