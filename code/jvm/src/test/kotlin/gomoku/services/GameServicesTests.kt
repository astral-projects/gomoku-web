package gomoku.services

import gomoku.domain.Id
import gomoku.domain.PositiveValue
import gomoku.domain.game.GameState
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.Move
import gomoku.domain.game.board.moves.move.Piece
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
import gomoku.services.game.GameMakeMoveError
import gomoku.services.game.GamesService
import gomoku.services.user.UsersService
import gomoku.utils.Failure
import gomoku.utils.Success
import gomoku.utils.TestClock
import gomoku.utils.TestDataGenerator
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
        val user = createRandomUser()
        val user2 = createRandomUser()

        // given: a game service
        val testClock = TestClock()
        val gameService = createGamesService(testClock)

        // when: creating a game
        val gameCreationResult = gameService.findGame(Id(1), user.id)

        // then: the creation is successful
        when (gameCreationResult) {
            is Failure -> fail("Unexpected $gameCreationResult")
            is Success -> assertEquals("Waiting in lobby", gameCreationResult.value.message)
        }

        val gameCreationResult2 = gameService.findGame(Id(1), user2.id)

        // then: the creation is successful
        when (gameCreationResult2) {
            is Failure -> fail("Unexpected $gameCreationResult2")
            is Success -> assertEquals("Joining game", gameCreationResult2.value.message)
        }

        assertNotEquals(gameCreationResult.value.message, gameCreationResult2.value.message)

        val gameResult = gameService.findGame(Id(1), user.id)
        if (gameResult is Success)
            assertEquals(gameCreationResult2.value.id, gameResult.value.id)
        else
            fail("Unexpected $gameResult")

    }


    @Test
    fun `get game by id`() {
        val user = createRandomUser()
        val user2 = createRandomUser()


        // given: a game service
        val testClock = TestClock()
        val gameService = createGamesService(testClock)
        val gameId = createRandomGame(gameService, user, user2) ?: fail("Unexpected null game")

        // after the correct creation of the game, we can get the game by id
        when (val gameResult = gameService.getGameById(gameId)) {
            is Failure -> fail("Unexpected $gameResult")
            is Success -> assertEquals(gameId, gameResult.value.id)
        }

    }

    @Test
    fun `exit a game`() {
        val user = createRandomUser()
        val user2 = createRandomUser()
        val gameService = createGamesService(TestClock())
        val gameId = createRandomGame(gameService, user, user2) ?: fail("Unexpected null game")
        val game = gameService.getGameById(gameId)
        val res = gameService.exitGame(gameId, user2.id)
        assertTrue(res is Success)
        when (res) {
            is Failure -> fail("Unexpected $res")
            is Success -> assertTrue(res.value)
        }
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
        val user = createRandomUser()
        val user2 = createRandomUser()
        val gameService = createGamesService(TestClock())
        val gameId = createRandomGame(gameService, user, user2) ?: fail("Unexpected null game")
        val game = gameService.getGameById(gameId)
        gameService.exitGame(gameId, user2.id)
        if (game is Success) {
            assertEquals(gameId, game.value.id)
            val res = gameService.deleteGame(gameId, user.id)
            when (res) {
                is Failure -> fail("Unexpected $res")
                is Success -> assertTrue(res.value)
            }
            // assertTrue(gameService.getGameById(gameId) is Failure)
        } else {
            fail("Unexpected $game")
        }
    }

    @Test
    fun `make a move`() {
        val user = createRandomUser()
        val user2 = createRandomUser()
        val gameService = createGamesService(TestClock())
        val gameId = createRandomGame(gameService, user, user2) ?: fail("Unexpected null game")
        val g = gameService.makeMove(gameId, user.id, Move(Square(Column('a'), Row(1)), Piece(Player.w)))
        assertTrue(g is Success)
        when (g) {
            is Failure -> fail("Unexpected $g")
            is Success -> {
                assertTrue(g.value)
            }
        }
        val g2 = gameService.makeMove(gameId, user.id, Move(Square(Column('b'), Row(1)), Piece(Player.w)))
        assertTrue(g2 is Failure)
        when (g2) {
            is Failure -> {
                assertTrue(g2.value is GameMakeMoveError.MoveNotValid)
            }

            is Success -> {
                fail("Unexpected $g2")
            }
        }

    }

    private fun createRandomGame(gameService: GamesService, user: User, user2: User): Id? {
        val gameId = Id(1)
        val gameResult = gameService.getGameById(gameId)
        if (gameResult is Success) {
            gameService.deleteGame(gameId, gameResult.value.hostId)
        }
        val gameCreationResult = gameService.findGame(gameId, user.id)
        val gameCreationResult2 = gameService.findGame(gameId, user2.id)

        if (gameCreationResult is Success && gameCreationResult2 is Success) return gameCreationResult2.value.id

        return null
    }

    private fun createRandomUser(): User {
        // given: a user service
        val userService = createUsersService(TestClock())

        // when: creating a user
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
                    tokenSizeInBytes = PositiveValue(256 / 8),
                    tokenTtl = tokenTtl,
                    tokenRollingTtl = tokenRollingTtl,
                    maxTokensPerUser = PositiveValue(maxTokensPerUser)
                )
            ),
            testClock
        )

        private fun createGamesService(
            testClock: TestClock
        ) = GamesService(
            JdbiTransactionManager(JdbiTestConfiguration.jdbi),
            testClock,
            variants,
        )

    }
}



