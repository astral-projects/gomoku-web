package gomoku.domain.errors

sealed class MakeMoveError {
    object GameIsNotInProgress : MakeMoveError()
    object GameOver : MakeMoveError()
    object NotYourTurn : MakeMoveError()
    object InvalidMove : MakeMoveError()
}
