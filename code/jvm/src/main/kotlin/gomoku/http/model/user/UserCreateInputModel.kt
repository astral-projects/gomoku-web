package gomoku.http.model.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class UserCreateInputModel(
    @field:NotBlank
    @field:NotEmpty
    @field:Size(min = 5, max = 30)
    val username: String,
    @field:Email(regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$")
    val email: String,
    @field:NotBlank
    @field:NotEmpty
    @field:Size(min = 8, max = 40)
    val password: String
)
