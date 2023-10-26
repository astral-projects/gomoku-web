package gomoku.services.game

import gomoku.domain.Id
import gomoku.domain.errors.MakeMoveError
import gomoku.domain.game.Game
import gomoku.domain.game.variant.GameVariant
import gomoku.domain.game.variant.Variant
import gomoku.utils.Either

sealed class GameCreationError {
    class UserAlreadyInLobby(val lobbyId: Id) : GameCreationError()
    class UserAlreadyInGame(val gameId: Id) : GameCreationError()
    object VariantNotFound : GameCreationError()
    object ErrorCreatingGame : GameCreationError()
    object UserAlreadyNotInLobby : GameCreationError()
}

typealias GameCreationResult = Either<GameCreationError, FindGameSuccess>

sealed class GettingGameError {
    object GameNotFound : GettingGameError()
}

typealias GettingGameResult = Either<GettingGameError, Game>

sealed class GamePutError {
    object UserIsNotTheHost : GamePutError()
    object GameIsInprogress : GamePutError()
    object GameNotFound : GamePutError()
}

typealias GamePutResult = Either<GamePutError, Boolean>

sealed class GameDeleteError {
    object UserDoesntBelongToThisGame : GameDeleteError()
    object GameNotFound : GameDeleteError()
    object GameAlreadyFinished : GameDeleteError()
    object VariantNotFound : GameDeleteError()
}

typealias GameDeleteResult = Either<GameDeleteError, Boolean>

sealed class GameMakeMoveError {
    class MoveNotValid(val error: MakeMoveError) : GameMakeMoveError()
    object UserDoesNotBelongToThisGame : GameMakeMoveError()
    object GameNotFound : GameMakeMoveError()
    object VariantNotFound : GameMakeMoveError()
}

typealias GameMakeMoveResult = Either<GameMakeMoveError, Boolean>

sealed class GameWaitError {
    object UserIsInLobby : GameWaitError()

    object UserDoesNotBelongToThisLobby : GameWaitError()
}

// TODO: change to GameWaitResult
typealias GameWaitResult = Either<GameWaitError, String>

sealed class LobbyDeleteError {

    object LobbyNotFound : LobbyDeleteError()
}
typealias LobbyDeleteResult = Either<LobbyDeleteError, Boolean>


sealed class GetVariantsError {
    object VariantsEmpty : GetVariantsError()
}
typealias GetVariantsResult= Either<GetVariantsError, List<GameVariant>>