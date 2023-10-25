package gomoku.services

import gomoku.domain.Id
import gomoku.domain.PositiveValue
import gomoku.domain.game.GameState
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row
import gomoku.domain.game.variant.FreestyleVariant
import gomoku.domain.game.variant.Variant
import gomoku.domain.game.variant.VariantConfig
import gomoku.domain.token.Sha256TokenEncoder
import gomoku.domain.user.User
import gomoku.domain.user.UsersDomain
import gomoku.domain.user.UsersDomainConfig
import gomoku.repository.jdbi.JdbiTestConfiguration
import gomoku.repository.jdbi.transaction.JdbiTransactionManager
import gomoku.repository.transaction.TransactionManager
import gomoku.services.game.GameCreationError
import gomoku.services.game.GameMakeMoveError
import gomoku.services.game.GamesService
import gomoku.services.user.UsersService
import gomoku.utils.Failure
import gomoku.utils.Success
import gomoku.utils.TestClock
import gomoku.utils.TestDataGenerator
import gomoku.utils.get
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.test.fail
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class GameServicesTests {
    init {
        gameVariantMap
    }

    @Test
    fun `create a game`() {
        // given: a user service
        val user = createRandomUser()
        val user2 = createRandomUser()

        // given: a game service
        val testClock = TestClock()
        val gameService = createGamesService(testClock)

        // when: joining a game
        val gameCreationResult = gameService.findGame(Id(1).get(), user.id)

        // then: the  join is successful
        when (gameCreationResult) {
            is Failure -> fail("Unexpected $gameCreationResult")
            is Success -> assertEquals("Waiting in lobby", gameCreationResult.value.message)
        }

        // then: other player wants to play the same variant so its a match between the two
        val gameCreationResult2 = gameService.findGame(Id(1).get(), user2.id)

        // then: the match is successful
        when (gameCreationResult2) {
            is Failure -> fail("Unexpected $gameCreationResult2")
            is Success -> assertEquals("Joining game", gameCreationResult2.value.message)
        }

        assertNotEquals(gameCreationResult.value.message, gameCreationResult2.value.message)

        // then: the game is created
        val gameResult = gameService.findGame(Id(1).get(), user.id)
        assertTrue(gameResult is Failure)
        when (gameResult) {
            is Success -> fail("User needs to be already in a Game $gameResult")
            is Failure -> {
                assertTrue(gameResult.value is GameCreationError.UserAlreadyInGame)
            }
        }
    }

    @Test
    fun `get game by id`() {
        // given: a user service
        val user = createRandomUser()
        val user2 = createRandomUser()

        // given: a game service
        val testClock = TestClock()
        val gameService = createGamesService(testClock)

        // when: creating a game with two users
        val gameId = createRandomGame(gameService, user, user2) ?: fail("Unexpected null game")

        // then: after the correct creation of the game, we can get the game by id
        when (val gameResult = gameService.getGameById(gameId)) {
            is Failure -> fail("Unexpected $gameResult")
            is Success -> assertEquals(gameId, gameResult.value.id)
        }
    }

    @Test
    fun `exit a game`() {
        // given: a user service
        val user = createRandomUser()
        val user2 = createRandomUser()

        // given: a game service
        val gameService = createGamesService(TestClock())
        val gameId = createRandomGame(gameService, user, user2) ?: fail("Unexpected null game")

        // then: after the correct creation of the game, we can get the game by id
        val game = gameService.getGameById(gameId)
        assertTrue(game is Success)
        when (game) {
            is Failure -> fail("Unexpected $game")
            is Success -> {
                assertEquals(gameId, game.value.id)
                assertEquals(GameState.IN_PROGRESS, game.value.state)
            }
        }

        // then: try to exit the game
        val res = gameService.exitGame(gameId, user2.id)
        assertTrue(res is Success)
        when (res) {
            is Failure -> fail("Unexpected $res")
            is Success -> assertTrue(res.value)
        }

        // then: check if the game is finished
        val checkExit = gameService.getGameById(gameId)
        assertTrue(checkExit is Success)
        when (checkExit) {
            is Failure -> fail("Unexpected $checkExit")
            is Success -> {
                assertEquals(gameId, checkExit.value.id)
                assertEquals(GameState.FINISHED, checkExit.value.state)
            }
        }
    }

    @Test
    fun `delete a game`() {
        // given: a user service
        val user = createRandomUser()
        val user2 = createRandomUser()

        // given: a game service
        val gameService = createGamesService(TestClock())

        // then: create a game WITH user2
        val gameId = createRandomGame(gameService, user, user2) ?: fail("Unexpected null game")
        val game = gameService.getGameById(gameId)

        // then: forcing the game to be FINISHED to be able to delete it
        val exitGame = gameService.exitGame(gameId, user2.id)
        assertTrue(exitGame is Success)

        // then: Delete game can only be deleted by the host and if the game is not in progress
        if (game is Success) {
            assertEquals(gameId, game.value.id)
            val res = gameService.deleteGame(gameId, user.id)
            when (res) {
                is Failure -> fail("Deleted Game was not possible ${res.value}")
                is Success -> assertTrue(res.value)
            }
        } else {
            fail("The game wasn't create with success $game")
        }
    }

    @Test
    fun `make a move`() {
        // given: a user service
        val user = createRandomUser()
        val user2 = createRandomUser()

        // given: a game service and create a game
        val gameService = createGamesService(TestClock())
        val gameId = createRandomGame(gameService, user, user2) ?: fail("Unexpected null game")

        // then: make a move
        val g = gameService.makeMove(gameId, user.id, Square(Column('a'), Row(1)))
        assertTrue(g is Success)
        when (g) {
            is Failure -> fail("Unexpected $g")
            is Success -> {
                assertTrue(g.value)
            }
        }

        // then: make a move with the same user
        val g2 = gameService.makeMove(gameId, user.id, Square(Column('b'), Row(1)))
        assertTrue(g2 is Failure)
        when (g2) {
            is Failure -> {
                assertTrue(g2.value is GameMakeMoveError.MoveNotValid)
            }

            is Success -> {
                fail("It isn't correct the same user play 2 times in a row $g2")
            }
        }
    }

    private fun createRandomGame(gameService: GamesService, user: User, user2: User): Id? {
        val variantId = Id(1)
        val gameCreationResult = gameService.findGame(variantId.get(), user.id)
        val gameCreationResult2 = gameService.findGame(variantId.get(), user2.id)
        if (gameCreationResult is Success && gameCreationResult2 is Success) return gameCreationResult2.value.id
        return null
    }

    private fun createRandomUser(): User {
        val userService = createUsersService(TestClock())
        val username = TestDataGenerator.newTestUserName()
        val email = TestDataGenerator.newTestEmail()
        val password = TestDataGenerator.newTestPassword()
        userService.createUser(username, email, password)
        val token = when (val createTokenResult = userService.createToken(username, password)) {
            is Failure -> fail(createTokenResult.toString())
            is Success -> createTokenResult.value.tokenValue
        }
        return userService.getUserByToken(token) ?: fail("User not found")
    }

    companion object {
        private val transactionManager: TransactionManager = JdbiTransactionManager(JdbiTestConfiguration.jdbi)
        private val variants: List<Variant> = listOf(FreestyleVariant())
        private val gameVariantMap: Map<Id, Variant> by lazy {
            transactionManager.run { transaction ->
                val variantsConfig: List<VariantConfig> = variants.map { it.config }
                transaction.gamesRepository.insertVariants(variantsConfig)
                val gameVariants = transaction.gamesRepository.getVariants()
                require(gameVariants.isNotEmpty()) { "No variants found in the database" }
                gameVariants.associateBy({ it.id }, { variants.first { v -> v.config.name === it.name } })
            }
        }

        private fun createUsersService(
            testClock: TestClock,
            tokenTtl: Duration = 30.days,
            tokenRollingTtl: Duration = 30.minutes,
            maxTokensPerUser: Int = 3
        ) = UsersService(
            JdbiTransactionManager(JdbiTestConfiguration.jdbi),
            UsersDomain(
                BCryptPasswordEncoder(),
                Sha256TokenEncoder(),
                UsersDomainConfig(
                    tokenSizeInBytes = PositiveValue(256 / 8).get(),
                    tokenTtl = tokenTtl,
                    tokenRollingTtl = tokenRollingTtl,
                    maxTokensPerUser = PositiveValue(maxTokensPerUser).get()
                )
            ),
            testClock
        )

        fun createGamesService(
            testClock: TestClock
        ) = GamesService(
            JdbiTransactionManager(JdbiTestConfiguration.jdbi),
            testClock,
            variants
        )
    }
}
