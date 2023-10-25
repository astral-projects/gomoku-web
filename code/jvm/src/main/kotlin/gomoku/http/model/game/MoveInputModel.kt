package gomoku.http.model.game

import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.Range

class MoveInputModel(
    @field:Size(min = 1, max = 1)
    val col: String,
    @field:Range(min = 1, max = 15)
    val row: Int
)
