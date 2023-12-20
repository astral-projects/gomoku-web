package gomoku.services.game

import gomoku.domain.components.Id
import gomoku.domain.game.Game
import gomoku.domain.game.board.errors.MakeMoveError
import gomoku.utils.Either

sealed class GameCreationError {
    data object VariantNotFound : GameCreationError()
    class UserAlreadyLeftTheLobby(val lobbyId: Id) : GameCreationError()
    data object GameInsertionError : GameCreationError()
    data object LobbyNotFound : GameCreationError()
}

typealias GameCreationResult = Either<GameCreationError, FindGameSuccess>

sealed class GettingGameError {
    data object GameNotFound : GettingGameError()
}

typealias GettingGameResult = Either<GettingGameError, Game>

sealed class GameUpdateError {
    data object UserNotInGame : GameUpdateError()
    data object GameNotFound : GameUpdateError()
    data object GameAlreadyFinished : GameUpdateError()
    data object VariantNotFound : GameUpdateError()
}

typealias GameUpdateResult = Either<GameUpdateError, Boolean>

sealed class GameMakeMoveError {
    class MoveNotValid(val error: MakeMoveError) : GameMakeMoveError()
    data object UserNotInGame : GameMakeMoveError()
    data object UserDoesNotBelongToThisGame : GameMakeMoveError()
    data object GameNotFound : GameMakeMoveError()
    data object VariantNotFound : GameMakeMoveError()
}

typealias MakeMoveResult = Either<MakeMoveError, Game>
typealias GameMakeMoveResult = Either<GameMakeMoveError, Boolean>

sealed class GameWaitError {
    data object UserNotInAnyGameOrLobby : GameWaitError()
}

typealias GameWaitResult = Either<GameWaitError, WaitForGameSuccess>

sealed class LobbyDeleteError {
    data object LobbyNotFound : LobbyDeleteError()
}

typealias LobbyDeleteResult = Either<LobbyDeleteError, Boolean>
