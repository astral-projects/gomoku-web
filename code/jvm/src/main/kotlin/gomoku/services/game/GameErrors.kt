package gomoku.services.game

import gomoku.utils.Either

sealed class GameErrors {

}

typealias GameCreationResult = Either<GameErrors, Int>