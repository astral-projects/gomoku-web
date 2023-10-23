package gomoku.http.model.game

import org.hibernate.validator.constraints.Range

data class VariantInputModel(
    @field:Range(min = 1)
    val id: Int
)
