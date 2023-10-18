package gomoku

import gomoku.domain.user.Email
import gomoku.domain.user.Username
import kotlin.math.abs
import kotlin.random.Random

object TestDataGenerator {

    fun newTestUserName() = Username("user-${abs(Random.nextLong())}")

    fun newTestEmail() = Email("email@-${abs(Random.nextLong())}.com")

    fun newTokenValidationData() = "token-${abs(Random.nextLong())}"

}