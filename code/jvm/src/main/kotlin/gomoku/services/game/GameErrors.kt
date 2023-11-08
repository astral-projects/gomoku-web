package gomoku.services.game

import gomoku.domain.components.Id
import gomoku.domain.game.Game
import gomoku.domain.game.errors.MakeMoveError
import gomoku.domain.game.variant.GameVariant
import gomoku.domain.idempotencyKey.IdempotencyKey
import gomoku.utils.Either

sealed class GameCreationError {
    class UserAlreadyInGame(val gameId: Id) : GameCreationError()
    object VariantNotFound : GameCreationError()
    object GameInsertFailure : GameCreationError()
    class UserAlreadyLeaveTheLobby(val lobbyId: Id) : GameCreationError()
    object LobbyInsertFailure : GameCreationError()
}

typealias GameCreationResult = Either<GameCreationError, FindGameSuccess>

sealed class GettingGameError {
    object GameNotFound : GettingGameError()
}

typealias GettingGameResult = Either<GettingGameError, Game>

sealed class GameDeleteError {
    object UserIsNotTheHost : GameDeleteError()
    object GameIsInprogress : GameDeleteError()
    object GameNotFound : GameDeleteError()
    object GameDeleteFailure : GameDeleteError()
}

typealias GameDeleteResult = Either<GameDeleteError, Boolean>

sealed class GameUpdateError {
    object UserDoesntBelongToThisGame : GameUpdateError()
    object GameNotFound : GameUpdateError()
    object GameAlreadyFinished : GameUpdateError()
    object VariantNotFound : GameUpdateError()
}

typealias GameUpdateResult = Either<GameUpdateError, Boolean>

sealed class GameMakeMoveError {
    class MoveNotValid(val error: MakeMoveError) : GameMakeMoveError()
    object UserNotInGame : GameMakeMoveError()
    object UserDoesNotBelongToThisGame : GameMakeMoveError()
    object GameNotFound : GameMakeMoveError()
    object VariantNotFound : GameMakeMoveError()
    object GameUpdateFailure : GameMakeMoveError()
    object IdempotencyKeyExpired : GameMakeMoveError()
    object IdempotencyKeyAlreadyExists : GameMakeMoveError()
    object IdempotencyKeyNotFound : GameMakeMoveError()
}

typealias GameMakeMoveResult = Either<GameMakeMoveError, Boolean>

sealed class GameWaitError {
    object UserNotInAnyGameOrLobby : GameWaitError()
    object UserNotInLobby : GameWaitError()
}

typealias GameWaitResult = Either<GameWaitError, WaitForGameSuccess>

sealed class LobbyDeleteError {
    object LobbyNotFound : LobbyDeleteError()
    object LobbyDeleteFailure : LobbyDeleteError()
}
typealias LobbyDeleteResult = Either<LobbyDeleteError, Boolean>

sealed class GetVariantsError {
    object VariantsEmpty : GetVariantsError()
}
typealias GetVariantsResult = Either<GetVariantsError, List<GameVariant>>

sealed class IdempotencyKeyError {
    object IdempotencyKeyNotFound : IdempotencyKeyError()
}

typealias IdempotencyKeyResult = Either<IdempotencyKeyError, IdempotencyKey>
