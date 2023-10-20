package gomoku.http.controllers

import gomoku.domain.Id
import gomoku.domain.NonNegativeValue
import gomoku.domain.PaginatedResult
import gomoku.domain.PositiveValue
import gomoku.domain.user.AuthenticatedUser
import gomoku.domain.user.Email
import gomoku.domain.user.Password
import gomoku.domain.user.User
import gomoku.domain.user.UserRankInfo
import gomoku.domain.user.Username
import gomoku.http.Uris
import gomoku.http.model.IdOutputModel
import gomoku.http.model.Problem
import gomoku.http.model.token.UserTokenCreateOutputModel
import gomoku.http.model.user.UserCreateInputModel
import gomoku.http.model.user.UserCreateTokenInputModel
import gomoku.http.model.user.UserOutputModel
import gomoku.services.user.GettingUserError
import gomoku.services.user.TokenCreationError
import gomoku.services.user.UserCreationError
import gomoku.services.user.UsersService
import gomoku.utils.Failure
import gomoku.utils.NotTested
import gomoku.utils.Success
import jakarta.validation.Valid
import org.hibernate.validator.constraints.Range
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class UsersController(
    private val userService: UsersService
) {

    @PostMapping(Uris.Users.REGISTER)
    fun createUser(
        @Valid @RequestBody
        input: UserCreateInputModel
    ): ResponseEntity<*> {
        logger.info("POST ${Uris.Users.REGISTER}")
        val res = userService.createUser(
            username = Username(input.username),
            email = Email(input.email),
            password = Password(input.password)
        )
        return when (res) {
            is Success -> ResponseEntity.status(201)
                .header(
                    "Location",
                    Uris.Users.byId(res.value.value).toASCIIString()
                )
                .body(IdOutputModel.serializeFrom(res.value))

            is Failure -> when (res.value) {
                UserCreationError.InsecurePassword -> Problem.response(400, Problem.insecurePassword)
                UserCreationError.UsernameAlreadyExists -> Problem.response(400, Problem.usernameAlreadyExists)
                UserCreationError.EmailAlreadyExists -> Problem.response(400, Problem.emailAlreadyExists)
            }
        }
    }

    @PostMapping(Uris.Users.TOKEN)
    fun createToken(
        @Valid @RequestBody
        input: UserCreateTokenInputModel
    ): ResponseEntity<*> {
        logger.info("POST ${Uris.Users.TOKEN}")
        val res = userService.createToken(
            username = Username(input.username),
            password = Password(input.password)
        )
        return when (res) {
            is Success ->
                ResponseEntity.status(200)
                    .body(UserTokenCreateOutputModel(res.value.tokenValue))

            is Failure -> when (res.value) {
                TokenCreationError.PasswordIsInvalid ->
                    Problem.response(400, Problem.passwordIsInvalid)

                TokenCreationError.UsernameIsInvalid ->
                    Problem.response(400, Problem.usernameIsInvalid)
            }
        }
    }

    @PostMapping(Uris.Users.LOGOUT)
    fun logout(authenticatedUser: AuthenticatedUser): ResponseEntity<*> {
        logger.info("POST ${Uris.Users.LOGOUT}")
        val wasTokenRevoked = userService.revokeToken(authenticatedUser.token)
        return when (wasTokenRevoked) {
            is Success -> ResponseEntity.ok("Logout successful")
            is Failure -> Problem.response(400, Problem.logoutError)
        }
    }

    @GetMapping(Uris.Users.HOME)
    fun getUserHome(authenticatedUser: AuthenticatedUser): ResponseEntity<UserOutputModel> {
        logger.info("GET ${Uris.Users.HOME}")
        val userOutputmodel = UserOutputModel.serializeFrom(authenticatedUser.user)
        return ResponseEntity.ok(userOutputmodel)
    }

    @GetMapping(Uris.Users.GET_BY_ID)
    fun getUserById(
        @Valid
        @Range(min = 1)
        @PathVariable
        id: Int
    ): ResponseEntity<*> {
        logger.info("GET ${Uris.Users.GET_BY_ID}")
        return when (val res = userService.getUserById(Id(id))) {
            is Success -> ResponseEntity.ok(UserOutputModel.serializeFrom(res.value))
            is Failure -> when (res.value) {
                GettingUserError.UserNotFound -> Problem.response(404, Problem.userNotFound)
            }
        }
    }

    @GetMapping(Uris.Users.RANKING)
    fun getUsersRanking(
        @RequestParam(name = "offset", defaultValue = "0") offset: Int,
        @RequestParam(name = "limit", defaultValue = "10") limit: Int
    ): ResponseEntity<PaginatedResult<UserRankInfo>> {
        logger.info("GET ${Uris.Users.RANKING}")
        val paginatedResult = userService.getUsersRanking(NonNegativeValue(offset), PositiveValue(limit))
        return ResponseEntity.ok(paginatedResult)
    }

    @GetMapping(Uris.Users.STATS_BY_ID)
    @NotTested
    fun getUserStats(user: AuthenticatedUser): ResponseEntity<UserRankInfo> {
        logger.info("GET ${Uris.Users.RANKING}")
        TODO("Not yet implemented")
    }

    @PutMapping(Uris.Users.EDIT_USER_PROFILE)
    @NotTested
    fun editUser(user: AuthenticatedUser): ResponseEntity<User> {
        logger.info("PUT ${Uris.Users.EDIT_USER_PROFILE}")
        TODO("Not yet implemented")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UsersController::class.java)
    }
}
