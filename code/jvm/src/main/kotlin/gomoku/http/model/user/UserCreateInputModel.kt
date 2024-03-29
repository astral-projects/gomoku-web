package gomoku.http.model.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class UserCreateInputModel(
    val username: String,
    val email: String,
    @field:NotBlank
    @field:NotEmpty
    @field:Size(min = 8, max = 40)
    val password: String
)
