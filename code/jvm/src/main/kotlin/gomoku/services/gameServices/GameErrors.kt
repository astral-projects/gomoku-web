package gomoku.services.gameServices

import gomoku.utils.Either

sealed class GameErrors {

}

typealias GameCreationResult = Either<GameErrors, Int>