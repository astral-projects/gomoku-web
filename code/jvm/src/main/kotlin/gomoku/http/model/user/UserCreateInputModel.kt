package gomoku.http.model.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.Range

data class UserCreateInputModel(
    @field:NotBlank
    @field:NotEmpty
    @field:Size(min = 5, max = 30)
    val username: String,
    @field:Email
    val email: String,
    @field:NotBlank
    @field:NotEmpty
    @field:Size(min = 8, max = 40)
    val password: String
)
