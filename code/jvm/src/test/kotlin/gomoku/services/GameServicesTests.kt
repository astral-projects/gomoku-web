package gomoku.services

import gomoku.domain.components.Id
import gomoku.domain.components.PositiveValue
import gomoku.domain.game.GameState
import gomoku.domain.game.board.BoardDraw
import gomoku.domain.game.board.BoardWin
import gomoku.domain.game.board.errors.MakeMoveError
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row
import gomoku.domain.game.variant.FreestyleVariant
import gomoku.domain.game.variant.GameVariant
import gomoku.domain.game.variant.Variant
import gomoku.domain.game.variant.config.VariantConfig
import gomoku.domain.token.Sha256TokenEncoder
import gomoku.domain.user.User
import gomoku.domain.user.UsersDomain
import gomoku.domain.user.UsersDomainConfig
import gomoku.repository.jdbi.JdbiTestConfiguration
import gomoku.repository.jdbi.transaction.JdbiTransactionManager
import gomoku.repository.transaction.TransactionManager
import gomoku.services.game.FindGameSuccess
import gomoku.services.game.GameCreationError
import gomoku.services.game.GameDeleteError
import gomoku.services.game.GameMakeMoveError
import gomoku.services.game.GameUpdateError
import gomoku.services.game.GameWaitError
import gomoku.services.game.GamesService
import gomoku.services.game.GettingGameError
import gomoku.services.game.LobbyDeleteError
import gomoku.services.game.NoVariantImplementationFoundException
import gomoku.services.game.WaitForGameSuccess
import gomoku.services.user.UsersService
import gomoku.utils.Either
import gomoku.utils.Failure
import gomoku.utils.IntrusiveTests
import gomoku.utils.MultiThreadTestHelper
import gomoku.utils.RequiresDatabaseConnection
import gomoku.utils.Success
import gomoku.utils.TestClock
import gomoku.utils.TestConfiguration.NR_OF_TEST_ITERATIONS
import gomoku.utils.TestConfiguration.stressTestTimeoutDuration
import gomoku.utils.TestDataGenerator.newTestEmail
import gomoku.utils.TestDataGenerator.newTestId
import gomoku.utils.TestDataGenerator.newTestPassword
import gomoku.utils.TestDataGenerator.newTestUserName
import gomoku.utils.TestDataGenerator.randomTo
import gomoku.utils.TestVariantImpl
import gomoku.utils.get
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.RepeatedTest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.fail
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

@RequiresDatabaseConnection
@IntrusiveTests
class GameServicesTests {

    init {
        gameTestVariant
    }

    private val testVariantId = gameTestVariant.id

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `create a game`() {
        // given: two users
        val host = createRandomUser()
        val guest = createRandomUser()

        // and: a game service
        val testClock = TestClock()
        val gamesService = createGamesService(testClock)

        // when: the host tries to create a game with an invalid variant
        val gameCreationResult = gamesService.findGame(newTestId(), host.id)

        // then: the game creation fails
        when (gameCreationResult) {
            is Failure -> assertIs<GameCreationError.VariantNotFound>(gameCreationResult.value)
            is Success -> fail("Unexpected $gameCreationResult")
        }

        // when: joining a game
        val gameCreationResult2 = gamesService.findGame(testVariantId, host.id)

        // then: a lobby is created
        when (gameCreationResult2) {
            is Failure -> fail("Unexpected $gameCreationResult2")
            is Success -> assertIs<FindGameSuccess.LobbyCreated>(gameCreationResult2.value)
        }

        // then: another player wants to play the same variant, so its match between the two
        val gameCreationResult4 = gamesService.findGame(testVariantId, guest.id)

        // then: the match is successful
        when (gameCreationResult4) {
            is Failure -> fail("Unexpected $gameCreationResult4")
            is Success -> assertIs<FindGameSuccess.GameMatch>(gameCreationResult4.value)
        }
    }

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `get game by id`() {
        // given: two users
        val host = createRandomUser()
        val guest = createRandomUser()

        // and: a game service
        val testClock = TestClock()
        val gameService = createGamesService(testClock)

        // when: creating a game with two users
        val gameId = createRandomGame(gameService, testVariantId, host, guest)

        // then: after the correct creation of the game, we can get the game by id
        when (val gameResult = gameService.getGameById(gameId)) {
            is Success -> assertEquals(gameId, gameResult.value.id)
            is Either.Left -> fail("Unexpected $gameResult")
        }
    }

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `exit a game`() {
        // given: two users
        val host = createRandomUser()
        val guest = createRandomUser()

        // and: a game service
        val gameService = createGamesService(TestClock())
        val gameId = createRandomGame(gameService, testVariantId, host, guest)

        // when: the game is retrieved by id
        val game = gameService.getGameById(gameId)

        // then: the game is created successfully and is in progress
        when (game) {
            is Failure -> fail("Unexpected $game")
            is Success -> {
                assertEquals(gameId, game.value.id)
                assertEquals(GameState.IN_PROGRESS, game.value.state)
            }
        }

        // when: a user exits a game that they are not in
        val exitGameOnUserNotInGame = gameService.exitGame(gameId, newTestId())

        // then: the exit is not valid
        when (exitGameOnUserNotInGame) {
            is Failure -> assertIs<GameUpdateError.UserNotInGame>(exitGameOnUserNotInGame.value)
            is Success -> fail("Unexpected $exitGameOnUserNotInGame")
        }

        // when: a user exits a game that doesn't exist
        val exitGameOnGameNotFound = gameService.exitGame(newTestId(), guest.id)

        // then: the exit is not valid
        when (exitGameOnGameNotFound) {
            is Failure -> assertIs<GameUpdateError.GameNotFound>(exitGameOnGameNotFound.value)
            is Success -> fail("Unexpected $exitGameOnGameNotFound")
        }

        // then: the guest exits the game
        val gameUpdateResult = gameService.exitGame(gameId, guest.id)

        // then: the exit is successful
        when (gameUpdateResult) {
            is Failure -> fail("Unexpected $gameUpdateResult")
            is Success -> assertTrue(gameUpdateResult.value)
        }

        // when: the host exits the game
        val gameUpdateResult2 = gameService.exitGame(gameId, host.id)

        // then: the exit is not successful, because the game is already finished
        when (gameUpdateResult2) {
            is Failure -> assertIs<GameUpdateError.GameAlreadyFinished>(gameUpdateResult2.value)
            is Success -> fail("Unexpected $gameUpdateResult2")
        }

        // when: the game is retrieved by id again
        val checkExit = gameService.getGameById(gameId)

        // then: the game is finished
        when (checkExit) {
            is Failure -> fail("Unexpected $checkExit")
            is Success -> {
                assertEquals(gameId, checkExit.value.id)
                assertEquals(GameState.FINISHED, checkExit.value.state)
            }
        }
    }

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `delete a game`() {
        // given: two users
        val host = createRandomUser()
        val guest = createRandomUser()

        // and: a game service
        val gameService = createGamesService(TestClock())

        // when: create a game with two users
        val gameId = createRandomGame(gameService, testVariantId, host, guest)

        // and: get the game by id
        val game = gameService.getGameById(gameId)

        // then: the game is created
        when (game) {
            is Failure -> fail("Unexpected $game")
            is Success -> {
                assertEquals(gameId, game.value.id)
                assertEquals(GameState.IN_PROGRESS, game.value.state)
            }
        }

        // when: the guest deletes the game
        val deleteGame = gameService.deleteGame(gameId, guest.id)

        // then: the game deletion is not possible, because the guest is not the host
        when (deleteGame) {
            is Failure -> assertIs<GameDeleteError.UserIsNotTheHost>(deleteGame.value)
            is Success -> fail("Unexpected $deleteGame")
        }

        // when: the host tries to delete the game
        val deleteGame1 = gameService.deleteGame(gameId, host.id)

        // then: the game deletion is not possible, because the game is still in progress
        when (deleteGame1) {
            is Failure -> assertIs<GameDeleteError.GameIsInProgress>(deleteGame1.value)
            is Success -> fail("Unexpected $deleteGame1")
        }

        // when: the guest exits the game and the game is set to finished
        val gameUpdateResult = gameService.exitGame(gameId, guest.id)

        // then: the exit is successful
        when (gameUpdateResult) {
            is Failure -> fail("Unexpected $gameUpdateResult")
            is Success -> assertTrue(gameUpdateResult.value)
        }

        // when: the host deletes the game
        val deleteGame2 = gameService.deleteGame(gameId, host.id)

        // then: the game deletion is successful
        when (deleteGame2) {
            is Failure -> fail("Unexpected $deleteGame2")
            is Success -> assertTrue(deleteGame2.value)
        }

        // and: the game is searched by id again
        val gameAfterDeletion = gameService.getGameById(gameId)

        // then: the game cannot be found
        when (gameAfterDeletion) {
            is Failure -> assertIs<GettingGameError.GameNotFound>(gameAfterDeletion.value)
            is Success -> fail("Unexpected $gameAfterDeletion")
        }

        // and: the host tries to delete the game again
        val deleteGame3 = gameService.deleteGame(gameId, host.id)

        // then: the game cannot be found
        when (deleteGame3) {
            is Failure -> assertIs<GameDeleteError.GameNotFound>(deleteGame3.value)
            is Success -> fail("Unexpected $deleteGame3")
        }
    }

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `make valid and invalid moves`() {
        // given: two users
        val host = createRandomUser()
        val guest = createRandomUser()

        // and: a game service
        val gamesService = createGamesService(TestClock())

        // when: a game is created and retrieved
        val gameId = createRandomGame(
            gameService = gamesService,
            variantId = testVariantId,
            host = host,
            guest = guest
        )
        val game = gamesService.getGameById(gameId)

        // then: the game is created successfully and is in progress
        when (game) {
            is Failure -> fail("Unexpected $game")
            is Success -> {
                assertEquals(gameId, game.value.id)
                assertEquals(GameState.IN_PROGRESS, game.value.state)
            }
        }

        // when: a move is made in a game that doesn't exist
        val moveOnGameNotFound = gamesService.makeMove(
            gameId = newTestId(),
            userId = host.id,
            toSquare = Square("a1")
        )

        // then: the move is not valid
        when (moveOnGameNotFound) {
            is Failure -> assertIs<GameMakeMoveError.GameNotFound>(moveOnGameNotFound.value)
            is Success -> fail("Unexpected $moveOnGameNotFound")
        }

        // when: a move is made by a user that does not belong to the game
        val validHostMove = Square("a1")
        val moveMadeByAUserThatDoesNotBelongToTheGame = gamesService.makeMove(
            gameId = gameId,
            userId = newTestId(),
            toSquare = validHostMove
        )

        // then: the move is not valid
        when (moveMadeByAUserThatDoesNotBelongToTheGame) {
            is Failure -> assertIs<GameMakeMoveError.UserNotInGame>(moveMadeByAUserThatDoesNotBelongToTheGame.value)
            is Success -> fail("Unexpected $moveMadeByAUserThatDoesNotBelongToTheGame")
        }

        // when: a user makes a move outside the board
        val moveOutsideOfBoard = gamesService.makeMove(
            gameId = gameId,
            userId = host.id,
            toSquare = Square(Column('a').get(), Row(Int.MAX_VALUE).get())
        )

        // then: the move is not valid
        when (moveOutsideOfBoard) {
            is Failure -> {
                assertIs<GameMakeMoveError.MoveNotValid>(moveOutsideOfBoard.value)
                val moveNotValid = moveOutsideOfBoard.value as GameMakeMoveError.MoveNotValid
                assertIs<MakeMoveError.InvalidPosition>(moveNotValid.error)
            }

            is Success -> fail("Unexpected $moveOutsideOfBoard")
        }

        // when: a user makes a valid move
        val validMoveResult = gamesService.makeMove(
            gameId = gameId,
            userId = host.id,
            toSquare = validHostMove
        )

        // then: the move is valid
        when (validMoveResult) {
            is Failure -> fail("Unexpected $validMoveResult")
            is Success -> {
                assertTrue(validMoveResult.value)
            }
        }

        // when: the same user tries to make a move again
        val secondMoveResult = gamesService.makeMove(
            gameId = gameId,
            userId = host.id,
            toSquare = validHostMove
        )

        // then: the move is not valid, because it's not the user's turn
        when (secondMoveResult) {
            is Failure -> {
                assertIs<GameMakeMoveError.MoveNotValid>(secondMoveResult.value)
                val moveNotValid = secondMoveResult.value as GameMakeMoveError.MoveNotValid
                assertIs<MakeMoveError.NotYourTurn>(moveNotValid.error)
            }

            is Success -> fail("Unexpected $secondMoveResult")
        }

        // when: guest makes a move to the same square
        val invalidGuestMove = gamesService.makeMove(
            gameId = gameId,
            userId = guest.id,
            toSquare = validHostMove
        )

        // then: the move is not valid, because the square is already taken
        when (invalidGuestMove) {
            is Failure -> {
                assertIs<GameMakeMoveError.MoveNotValid>(invalidGuestMove.value)
                val moveNotValid = invalidGuestMove.value as GameMakeMoveError.MoveNotValid
                assertIs<MakeMoveError.PositionTaken>(moveNotValid.error)
            }

            is Success -> fail("Unexpected $invalidGuestMove")
        }

        // when: the host exits the game
        val exitGameResult = gamesService.exitGame(gameId, host.id)

        // then: the exit is successful
        when (exitGameResult) {
            is Failure -> fail("Unexpected $exitGameResult")
            is Success -> assertTrue(exitGameResult.value)
        }

        // when: the game is retrieved by id again
        val gameAfterExit = gamesService.getGameById(gameId)

        // then: the game is finished
        when (gameAfterExit) {
            is Failure -> fail("Unexpected $gameAfterExit")
            is Success -> {
                assertEquals(gameId, gameAfterExit.value.id)
                assertEquals(GameState.FINISHED, gameAfterExit.value.state)
            }
        }

        // when: the guest tries to make a move in a finished game
        val guestMoveResult = gamesService.makeMove(
            gameId = gameId,
            userId = guest.id,
            toSquare = Square(Column('a').get(), Row(2).get())
        )

        // then: the move is not valid
        when (guestMoveResult) {
            is Failure -> {
                assertIs<GameMakeMoveError.MoveNotValid>(guestMoveResult.value)
                val moveNotValid = guestMoveResult.value as GameMakeMoveError.MoveNotValid
                assertIs<MakeMoveError.GameOver>(moveNotValid.error)
            }

            is Success -> fail("Unexpected $guestMoveResult")
        }
    }

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `wait for a game within a lobby and exit lobby`() {
        // given: two users
        val host = createRandomUser()
        val guest = createRandomUser()

        // and: a game service
        val testClock = TestClock()
        val gamesService = createGamesService(testClock)

        // when: the host exits a lobby that they are not in
        val hostExitsLobbyThatTheyAreNotIn = gamesService.exitLobby(newTestId(), host.id)

        // then: the exit is not valid
        when (hostExitsLobbyThatTheyAreNotIn) {
            is Failure -> assertIs<LobbyDeleteError.LobbyNotFound>(hostExitsLobbyThatTheyAreNotIn.value)
            is Success -> fail("Unexpected $hostExitsLobbyThatTheyAreNotIn")
        }

        // when: the host tries to wait for a game in a lobby that doesn't exist
        val waitInLobbyOnLobbyNotFound = gamesService.waitForGame(newTestId(), host.id)

        // then: the wait is not valid
        when (waitInLobbyOnLobbyNotFound) {
            is Failure -> assertIs<GameWaitError.UserNotInAnyGameOrLobby>(waitInLobbyOnLobbyNotFound.value)
            is Success -> fail("Unexpected $waitInLobbyOnLobbyNotFound")
        }

        // when: the host tries to wait for a game in a lobby that they are not in
        val waitInLobbyOnUserNotInLobby = gamesService.waitForGame(newTestId(), host.id)

        // then: the wait is not valid
        when (waitInLobbyOnUserNotInLobby) {
            is Failure -> assertIs<GameWaitError.UserNotInAnyGameOrLobby>(waitInLobbyOnUserNotInLobby.value)
            is Success -> fail("Unexpected $waitInLobbyOnUserNotInLobby")
        }

        // when: the host tries to find a game
        val waitingInLobby = gamesService.findGame(testVariantId, host.id)

        // then: a lobby is created
        when (waitingInLobby) {
            is Failure -> fail("Unexpected $waitingInLobby")
            is Success -> assertIs<FindGameSuccess.LobbyCreated>(waitingInLobby.value)
        }

        // when: the host waits for a game in the lobby
        val lobbyId = Id(waitingInLobby.value.id).get()
        val hostWaitsInLobby = gamesService.waitForGame(lobbyId, host.id)

        // then: the wait is successful
        when (hostWaitsInLobby) {
            is Failure -> fail("Unexpected $hostWaitsInLobby")
            is Success -> assertIs<WaitForGameSuccess.WaitingInLobby>(hostWaitsInLobby.value)
        }

        // when: the host exits a random lobby
        val hostExitsRandomLobby = gamesService.exitLobby(newTestId(), host.id)

        // then: the exit is not valid
        when (hostExitsRandomLobby) {
            is Failure -> assertIs<LobbyDeleteError.LobbyNotFound>(hostExitsRandomLobby.value)
            is Success -> fail("Unexpected $hostExitsRandomLobby")
        }

        // when: the host exits the lobby
        val exitLobby = gamesService.exitLobby(lobbyId, host.id)

        // then: the exit is successful
        when (exitLobby) {
            is Failure -> fail("Unexpected $exitLobby")
            is Success -> assertTrue(exitLobby.value)
        }

        // when: the host rejoins the lobby
        val rejoinLobby = gamesService.findGame(testVariantId, host.id)

        // then: the lobby is created
        when (rejoinLobby) {
            is Failure -> fail("Unexpected $rejoinLobby")
            is Success -> assertIs<FindGameSuccess.LobbyCreated>(rejoinLobby.value)
        }

        // when: the guest joins the lobby
        val guestCreatesGameResult = gamesService.findGame(testVariantId, guest.id)

        // then: the game is created
        when (guestCreatesGameResult) {
            is Failure -> fail("Unexpected $guestCreatesGameResult")
            is Success -> assertIs<FindGameSuccess.GameMatch>(guestCreatesGameResult.value)
        }

        // when: the host waits for a game in the lobby again
        val newLobbyId = Id(rejoinLobby.value.id).get()
        val hostWaitsInLobbyAgain = gamesService.waitForGame(newLobbyId, host.id)

        // then: the wait is successful
        when (hostWaitsInLobbyAgain) {
            is Failure -> fail("Unexpected $hostWaitsInLobbyAgain")
            is Success -> {
                val waitForGameSuccess = hostWaitsInLobbyAgain.value
                assertIs<WaitForGameSuccess.GameMatch>(waitForGameSuccess)
                val gameId = waitForGameSuccess.id
                assertEquals(gameId, guestCreatesGameResult.value.id)
            }
        }
    }

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `variant points are correctly distributed in forfeit game`() {
        // given: two users
        val host = createRandomUser()
        val guest = createRandomUser()

        // and: a game service
        val testClock = TestClock()
        val gamesService = createGamesService(testClock)

        // and: a user service
        val usersService = createUsersService(testClock)

        // when: checking the variant points
        val forfeiterPoints = testVariant.points.onForfeitOrTimer.forfeiter
        val winnerPoints = testVariant.points.onForfeitOrTimer.winner

        // and: the current host and guest stats
        val hostStatsBeforeGame = usersService.getUserStats(host.id)
        assertNotNull(hostStatsBeforeGame)
        val guestStatsBeforeGame = usersService.getUserStats(guest.id)
        assertNotNull(guestStatsBeforeGame)

        // when a game is created and retrieved
        val gameId = createRandomGame(
            gameService = gamesService,
            variantId = testVariantId,
            host = host,
            guest = guest
        )
        val game = gamesService.getGameById(gameId)

        // then: the game is created successfully and is in progress
        when (game) {
            is Failure -> fail("Unexpected $game")
            is Success -> {
                assertEquals(gameId, game.value.id)
                assertEquals(GameState.IN_PROGRESS, game.value.state)
            }
        }

        // when: the guest exits the game
        val exitGameResult = gamesService.exitGame(gameId, guest.id)

        // then: the exit is successful
        when (exitGameResult) {
            is Failure -> fail("Unexpected $exitGameResult")
            is Success -> assertTrue(exitGameResult.value)
        }

        // when: the game is retrieved by id again
        val gameAfterExit = gamesService.getGameById(gameId)

        // then: the game is finished with a win
        when (gameAfterExit) {
            is Failure -> fail("Unexpected $gameAfterExit")
            is Success -> {
                assertEquals(gameId, gameAfterExit.value.id)
                assertEquals(GameState.FINISHED, gameAfterExit.value.state)
                assertIs<BoardWin>(gameAfterExit.value.board)
            }
        }

        // when: the current host and guest stats are retrieved
        val hostStatsAfterGame = usersService.getUserStats(host.id)
        assertNotNull(hostStatsAfterGame)
        val guestStatsAfterGame = usersService.getUserStats(guest.id)
        assertNotNull(guestStatsAfterGame)

        // then: the host stats are updated according to the expected points
        val hostPointsDiffer = hostStatsAfterGame.points.value - hostStatsBeforeGame.points.value
        assertEquals(winnerPoints.value, hostPointsDiffer)

        // and: the guest stats are updated according to the expected points
        val guestPointsDiffer = guestStatsAfterGame.points.value - guestStatsBeforeGame.points.value
        assertEquals(forfeiterPoints.value, guestPointsDiffer)
    }

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `variant points are correctly distributed in draw game`() {
        // given: two users
        val host = createRandomUser()
        val guest = createRandomUser()

        // and: a game service
        val testClock = TestClock()
        val gamesService = createGamesService(testClock)

        // and: a user service
        val usersService = createUsersService(testClock)

        // when: checking the variant points
        val sharedPoints = testVariant.points.onDraw.shared

        // and: the current host and guest stats
        val hostStatsBeforeGame = usersService.getUserStats(host.id)
        assertNotNull(hostStatsBeforeGame)
        val guestStatsBeforeGame = usersService.getUserStats(guest.id)
        assertNotNull(guestStatsBeforeGame)

        // when a game is created and retrieved
        val gameId = createRandomGame(
            gameService = gamesService,
            variantId = testVariantId,
            host = host,
            guest = guest
        )
        val game = gamesService.getGameById(gameId)

        // then: the game is created successfully and is in progress
        when (game) {
            is Failure -> fail("Unexpected $game")
            is Success -> {
                assertEquals(gameId, game.value.id)
                assertEquals(GameState.IN_PROGRESS, game.value.state)
            }
        }

        // when: the host makes a move, followed by the guest, and so on
        // The game ends in a draw because, according to the test variant rules,
        // two moves in the same row were made
        makeMoves(
            gamesService = gamesService,
            gameId = gameId,
            squares = listOf(
                Square("a1"),
                Square("b1")
            ),
            host = host,
            guest = guest
        )

        // when: the game is retrieved by id again
        val gameAfterExit = gamesService.getGameById(gameId)

        // then: the game is finished with a draw
        when (gameAfterExit) {
            is Failure -> fail("Unexpected $gameAfterExit")
            is Success -> {
                assertEquals(gameId, gameAfterExit.value.id)
                assertEquals(GameState.FINISHED, gameAfterExit.value.state)
                assertIs<BoardDraw>(gameAfterExit.value.board)
            }
        }

        // when: the current host and guest stats are retrieved
        val hostStatsAfterGame = usersService.getUserStats(host.id)
        assertNotNull(hostStatsAfterGame)
        val guestStatsAfterGame = usersService.getUserStats(guest.id)
        assertNotNull(guestStatsAfterGame)

        // then: the host stats are updated according to the expected points
        val hostPointsDiffer = hostStatsAfterGame.points.value - hostStatsBeforeGame.points.value
        assertEquals(sharedPoints.value, hostPointsDiffer)

        // and: the guest stats are updated according to the expected points
        val guestPointsDiffer = guestStatsAfterGame.points.value - guestStatsBeforeGame.points.value
        assertEquals(sharedPoints.value, guestPointsDiffer)
    }

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `variant points are correctly distributed in a win game`() {
        // given: two users
        val host = createRandomUser()
        val guest = createRandomUser()

        // and: a game service
        val testClock = TestClock()
        val gamesService = createGamesService(testClock)

        // and: a user's service
        val usersService = createUsersService(testClock)

        // when: checking the variant points
        val winnerPoints = testVariant.points.onFinish.winner
        val loserPoints = testVariant.points.onFinish.loser

        // and: the current host and guest stats
        val hostStatsBeforeGame = usersService.getUserStats(host.id)
        assertNotNull(hostStatsBeforeGame)
        val guestStatsBeforeGame = usersService.getUserStats(guest.id)
        assertNotNull(guestStatsBeforeGame)

        // when a game is created and retrieved
        val gameId = createRandomGame(
            gameService = gamesService,
            variantId = testVariantId,
            host = host,
            guest = guest
        )
        val game = gamesService.getGameById(gameId)

        // then: the game is created successfully and is in progress
        when (game) {
            is Failure -> fail("Unexpected $game")
            is Success -> {
                assertEquals(gameId, game.value.id)
                assertEquals(GameState.IN_PROGRESS, game.value.state)
            }
        }

        // when: the host makes a move, followed by the guest, and so on
        // The host wins because, according to the test variant rules,
        // the first player to reach 3 <total> valid moves wins
        makeMoves(
            gamesService = gamesService,
            gameId = gameId,
            squares = listOf(
                Square("a1"),
                Square("b3"),
                Square("a2")
            ),
            host = host,
            guest = guest
        )

        // when: the game is retrieved by id again
        val gameAfterExit = gamesService.getGameById(gameId)

        // then: the game is finished with a win
        when (gameAfterExit) {
            is Failure -> fail("Unexpected $gameAfterExit")
            is Success -> {
                assertEquals(gameId, gameAfterExit.value.id)
                assertEquals(GameState.FINISHED, gameAfterExit.value.state)
                assertIs<BoardWin>(gameAfterExit.value.board)
            }
        }

        // when: the current host and guest stats are retrieved
        val hostStatsAfterGame = usersService.getUserStats(host.id)
        assertNotNull(hostStatsAfterGame)
        val guestStatsAfterGame = usersService.getUserStats(guest.id)
        assertNotNull(guestStatsAfterGame)

        // then: the host stats are updated according to the expected points
        val hostPointsDiffer = hostStatsAfterGame.points.value - hostStatsBeforeGame.points.value
        assertEquals(winnerPoints.value, hostPointsDiffer)

        // and: the guest stats are updated according to the expected points
        val guestPointsDiffer = guestStatsAfterGame.points.value - guestStatsBeforeGame.points.value
        assertEquals(loserPoints.value, guestPointsDiffer)
    }

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `get available variants`() {
        // given: a game service
        val gameService = createGamesService(TestClock())

        // when: getting the available variants
        val availableVariants = gameService.getVariants()

        // then: the test variant is the only available variant, because
        // its the only one in the variants list passed to the games service
        assertEquals(1, availableVariants.size)
        assertEquals(testVariantId, availableVariants.first().id)

        // when: getting the available variants with an empty list
        // then: the constructor throws an exception
        assertFailsWith<NoVariantImplementationFoundException> {
            createGamesService(TestClock(), emptyList())
        }

        // when: getting more than one variant
        val variantsList = listOf(FreestyleVariant, TestVariantImpl)

        // and: constructing the game service with the variants
        val gameServiceWithVariants = createGamesService(TestClock(), variantsList)

        // when: getting the available variants
        val moreVariantsFromService = gameServiceWithVariants.getVariants()

        // then: the available variants are the ones passed to the constructor
        assertEquals(variantsList.size, moreVariantsFromService.size)
        variantsList.forEach { variant ->
            assertTrue(
                moreVariantsFromService.any {
                    val variantConfig = variant.config
                    it.name == variantConfig.name &&
                        it.openingRule == variantConfig.openingRule &&
                        it.boardSize == variantConfig.boardSize
                }
            )
        }
    }

    // @RepeatedTest(NR_OF_STRESS_TEST_ITERATIONS)
    fun `stress test simulating several users joining lobbies and creating games`() {
        // given: a game service
        val gamesService = createGamesService(TestClock())

        // and: a multi-thread test helper and a set of threads
        val testDuration = stressTestTimeoutDuration
        val nrOfThreads = 15 randomTo 24
        val testHelper = MultiThreadTestHelper(testDuration)

        // and: a set of atomic counters
        val lobbiesCreated = AtomicInteger(0)
        val gamesCreated = AtomicInteger(0)
        val usersCreated = AtomicInteger(0)
        val stillInLobby = AtomicInteger(0)
        val failures = AtomicInteger(0)

        // and: a map of users in games (thread id -> repetion id)
        val usersInGames = ConcurrentHashMap<Int, Int>()
        val threadsIdsList = List(nrOfThreads) { it to -1 }
        usersInGames.putAll(threadsIdsList)

        // when: several users join lobbies and create games
        testHelper.createAndStartMultipleThreads(nrOfThreads) { threadId, isTestFinished ->
            var repetionId = 0
            while (!isTestFinished()) {
                // and: a random user
                val userId = createRandomUser().also { usersCreated.incrementAndGet() }
                // and: the user tries to find a game
                when (val gameCreationResult = gamesService.findGame(testVariantId, userId.id)) {
                    is Failure -> failures.incrementAndGet()
                    is Success -> when (gameCreationResult.value) {
                        // then: the user creates a lobby
                        is FindGameSuccess.LobbyCreated -> lobbiesCreated.incrementAndGet()
                        // or: the user creates a game
                        is FindGameSuccess.GameMatch -> {
                            val previousRepetion = usersInGames[threadId]
                            requireNotNull(previousRepetion)
                            // and: the user was added in fifo order
                            if (previousRepetion >= repetionId) {
                                throw AssertionError(
                                    "Repetion id is not in fifo order: " +
                                            "previous repetion id: $previousRepetion, " +
                                            "current repetion id: $repetionId"
                                )
                            }
                            usersInGames[threadId] = repetionId++
                            gamesCreated.incrementAndGet()
                        }
                    }
                }
            }
        }

        // and: all launched threads are joined
        testHelper.join()

        println("Users created: ${usersCreated.get()}")
        println("Games created: ${gamesCreated.get()}")
        println("Lobbies created: ${lobbiesCreated.get()}")
        println("Still in lobby: ${stillInLobby.get()}")
        println("Failures: ${failures.get()}")

        // when: an error margin is defined
        val errorMargin = 0.05f

        // then: the number of games created by a thread is not too different from the others,
        // which means that the hosts are being added in fifo order
        val values = usersInGames.values.toList()
        val maxGamesCreatedByAThread = values.maxOrNull()
        assertNotNull(maxGamesCreatedByAThread)
        val minGamesCreatedByAThread = values.minOrNull()
        assertNotNull(minGamesCreatedByAThread)
        val lobbies = lobbiesCreated.get()
        val users = usersCreated.get()
        val games = gamesCreated.get()
        val errorFormat = formatError(errorMargin)
        val inversedErrorFormat = formatError(1 - errorMargin)
        assertTrue(
            (maxGamesCreatedByAThread - minGamesCreatedByAThread) <= errorMargin * (lobbies / nrOfThreads),
            "The difference between the maximum and minimum number of games created by a thread is greater than $errorFormat of the total number of lobbies created"
        )
        kotlin.test.assertTrue(
            lobbies >= (users / 2) * (1 - errorMargin),
            "The number of lobbies created is less than $inversedErrorFormat of half the total number of users created"
        )
        kotlin.test.assertTrue(
            games >= (users / 2) * (1 - errorMargin),
            "The number of games created is less than $inversedErrorFormat of half the total number of users created"
        )
        kotlin.test.assertTrue(
            games >= lobbies * (1 - errorMargin),
            "The number of games created is less than $inversedErrorFormat of the total number of lobbies created"
        )
    }

    /**
     * Creates a random game with the given variant and users or fails the test.
     * @param gameService the game service to use.
     * @param variantId the id of the variant to create the game with.
     * @param host the id of the host of the game.
     * @param guest the id of the guest of the game.
     * @return the id of the created game.
     */
    private fun createRandomGame(gameService: GamesService, variantId: Id, host: User, guest: User): Id {
        val hostGameCreationResult = gameService.findGame(variantId, host.id)
        val guestGameCreationResult = gameService.findGame(variantId, guest.id)
        return if (hostGameCreationResult is Success && guestGameCreationResult is Success) {
            Id(guestGameCreationResult.value.id).get()
        } else {
            null
                ?: fail("Unexpected null game")
        }
    }

    /**
     * Makes a move on the board and fails the test if the move is not valid.
     * @param gamesService the game service to use.
     * @param gameId the id of the game to make the move on.
     * @param user the user that makes the move.
     * @param toSquare the square to make the move to.
     */
    private fun makeAMoveOnTheBoard(
        gamesService: GamesService,
        gameId: Id,
        user: User,
        toSquare: Square,
    ) {
        val hostMoveResult = gamesService.makeMove(
            gameId = gameId,
            userId = user.id,
            toSquare = toSquare
        )
        // then: the move is valid
        when (hostMoveResult) {
            is Failure -> fail("Unexpected $hostMoveResult")
            is Success -> assertTrue(hostMoveResult.value)
        }
    }

    /**
     * Makes a list of moves on the board and fails the test if any of the moves is not valid.
     * The host makes the first move, then the guest, then the host again, and so on.
     * @param gamesService the game service to use.
     * @param gameId the id of the game to make the move on.
     * @param squares the squares to make the moves to.
     * @param host the host of the game.
     * @param guest the guest of the game.
     */
    private fun makeMoves(
        gamesService: GamesService,
        gameId: Id,
        squares: List<Square>,
        host: User,
        guest: User,
    ) = squares.fold(host) { currentTurn, square ->
        makeAMoveOnTheBoard(
            gamesService = gamesService,
            gameId = gameId,
            user = currentTurn,
            toSquare = square
        )
        if (currentTurn == host) guest else host
    }

    /**
     * Creates a random user successfully or fails the test.
     */
    private fun createRandomUser(): User {
        val userService = createUsersService(TestClock())
        val username = newTestUserName()
        val email = newTestEmail()
        val password = newTestPassword()
        userService.createUser(username, email, password)
        val token = when (val createTokenResult = userService.createToken(username, password)) {
            is Failure -> fail("Unexpected $createTokenResult")
            is Success -> createTokenResult.value.tokenValue
        }
        return userService.getUserByToken(token) ?: fail("User not found")
    }

    companion object {
        private val transactionManager: TransactionManager = JdbiTransactionManager(JdbiTestConfiguration.jdbi)
        private val testVariant: Variant = TestVariantImpl
        private val variantsList: List<Variant> = listOf(testVariant)
        val gameTestVariant: GameVariant =
            transactionManager.run { transaction ->
                val variantsConfig: List<VariantConfig> = variantsList.map { it.config }
                transaction.gamesRepository.insertVariants(variantsConfig)
                val gameVariants = transaction.gamesRepository.getVariants()
                require(gameVariants.isNotEmpty()) { "No variants found in the database" }
                val id = transaction.gamesRepository.getVariantByName(this.testVariant.config.name)
                requireNotNull(id) { "Test variant not found in the database" }
                GameVariant(
                    id,
                    this.testVariant.config.name,
                    this.testVariant.config.openingRule,
                    this.testVariant.config.boardSize
                )
            }

        fun createUsersService(
            testClock: TestClock,
            tokenTtl: Duration = 30.days,
            tokenRollingTtl: Duration = 30.minutes,
            maxTokensPerUser: Int = 3,
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
            testClock: TestClock,
            variants: List<Variant> = variantsList,
        ) = GamesService(
            transactionManager = JdbiTransactionManager(JdbiTestConfiguration.jdbi),
            clock = testClock,
            variants = variants
        )

        /**
         * Creates a string with the error margin in percentage. E.g. 0.05f -> "5%".
         * @param errorMargin the error margin to be converted.
         */
        private fun formatError(errorMargin: Float): String = "${errorMargin * 100}%"
    }
}
