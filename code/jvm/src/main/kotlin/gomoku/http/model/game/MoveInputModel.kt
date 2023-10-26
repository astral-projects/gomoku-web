package gomoku.http.model.game

import org.hibernate.validator.constraints.Range

class MoveInputModel(
    val col: Char,
    @field:Range(min = 1)
    val row: Int
)
