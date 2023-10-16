package gomoku.services.game

import gomoku.utils.Either

sealed class GameErrors {
    object GameAlreadyExists : GameErrors()
    object GameNotFound : GameErrors()
}

typealias GameCreationResult = Either<GameErrors, Int>