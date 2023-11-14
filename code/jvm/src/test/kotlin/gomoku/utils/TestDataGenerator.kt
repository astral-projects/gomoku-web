package gomoku.utils

import gomoku.domain.components.Id
import gomoku.domain.user.components.Email
import gomoku.domain.user.components.Password
import gomoku.domain.user.components.Username
import java.util.*
import kotlin.math.abs
import kotlin.random.Random

/*
 * Centralizes the generation of test data.
 */
object TestDataGenerator {

    /**
     * Generates a random [String] using [UUID.randomUUID] coerced by [minLength] and [maxLength].
     * @param minLength minimum length of the generated string.
     * @param maxLength maximum length of the generated string.
     */
    fun newRandomString(
        minLength: Int = 0,
        maxLength: Int = Int.MAX_VALUE,
    ): String = UUID.randomUUID().toString().substring(minLength, maxLength)

    /**
     * Generates a random [Id].
     */
    fun newTestId() = Id(abs(Random.nextInt())).get()

    /**
     * Generates a random [Username] prefixed with "user-".
     */
    fun newTestUserName() = Username("user-${abs(Random.nextLong())}").get()

    /**
     * Generates a random [Email] prefixed with "email@" and suffixed with ".com".
     */
    fun newTestEmail() = Email("email@${abs(Random.nextLong())}.com").get()

    /**
     * Generates a random [Password] prefixed with "password-".
     */
    fun newTestPassword() = Password("password-${abs(Random.nextLong())}").get()

    /**
     * Generates a random token validation data prefixed with "token-".
     */
    fun newTokenValidationData() = "token-${abs(Random.nextLong())}"

    /**
     * Generates a random number between this [Int] and [end] (inclusive).
     */
    infix fun Int.randomTo(end: Int) = (this..end).random()

}
