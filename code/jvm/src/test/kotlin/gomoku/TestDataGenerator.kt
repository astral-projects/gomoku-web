package gomoku

import gomoku.domain.user.Email
import gomoku.domain.user.Password
import gomoku.domain.user.Username
import kotlin.math.abs
import kotlin.random.Random

object TestDataGenerator {

    /**
     * Generates a random [Username] prefixed with "user-".
     */
    fun newTestUserName() = Username("user-${abs(Random.nextLong())}")

    /**
     * Generates a random [Email] prefixed with "email@" and suffixed with ".com".
     */
    fun newTestEmail() = Email("email@${abs(Random.nextLong())}.com")

    /**
     * Generates a random [Password] prefixed with "password-".
     */
    fun newTestPassword() = Password("password-${abs(Random.nextLong())}")

    /**
     * Generates a random token validation data prefixed with "token-".
     */
    fun newTokenValidationData() = "token-${abs(Random.nextLong())}"

    /**
     * Generates a random number between this [Int] and [end] (inclusive).
     */
    infix fun Int.randomTo(end: Int) = (this..end).random()
}