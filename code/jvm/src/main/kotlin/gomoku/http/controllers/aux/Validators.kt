package gomoku.http.controllers.aux

import gomoku.domain.Id
import gomoku.domain.errors.GettingEmailResult
import gomoku.domain.errors.GettingIdResult
import gomoku.domain.errors.InvalidEmailError
import gomoku.domain.errors.InvalidIdError
import gomoku.domain.user.Email
import gomoku.utils.Failure
import gomoku.utils.Success

/**
 * Validates id and returns the value if it is valid or a Problem if it is not.
 *
 * @param id The id to validate.
 */
fun validateId(id: GettingIdResult): Id? = when (id) {
    is Success -> id.value
    is Failure -> when (id.value) {
        InvalidIdError.InvalidId -> null
    }
}

fun validateEmail(email: GettingEmailResult): Email? = when (email) {
    is Success -> email.value
    is Failure -> when (email.value) {
        InvalidEmailError.InvalidEmail -> null
    }
}


