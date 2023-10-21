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
import gomoku.services.user.TokenRevocationError
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
                UserCreationError.InsecurePassword -> Problem(
                    type = Problem.insecurePassword,
                    title = "Received password is considered insecure",
                    status = 400,
                    detail = "The password must be between 8 and 40 characters",
                    instance = Uris.Users.register()
                ).toResponse()

                UserCreationError.UsernameAlreadyExists -> Problem(
                    type = Problem.usernameAlreadyExists,
                    title = "Received username already exists",
                    status = 400,
                    detail = "The chosen username is already in use by another user",
                    instance = Uris.Users.register()
                ).toResponse()

                UserCreationError.EmailAlreadyExists -> Problem(
                    type = Problem.emailAlreadyExists,
                    title = "Received email already exists",
                    status = 400,
                    detail = "The chosen email is already in use by another user",
                    instance = Uris.Users.register()
                ).toResponse()
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
                TokenCreationError.PasswordIsInvalid -> Problem(
                    type = Problem.passwordIsInvalid,
                    title = "Received password is invalid",
                    status = 400,
                    detail = "The received password is invalid",
                    instance = Uris.Users.login()
                ).toResponse()

                TokenCreationError.UsernameIsInvalid -> Problem(
                    type = Problem.usernameIsInvalid,
                    title = "Received username is invalid",
                    status = 400,
                    detail = "The received username is invalid",
                    instance = Uris.Users.login()
                ).toResponse()
            }
        }
    }

    @PostMapping(Uris.Users.LOGOUT)
    fun logout(authenticatedUser: AuthenticatedUser): ResponseEntity<*> {
        logger.info("POST ${Uris.Users.LOGOUT}")
        return when (val tokenRevocationResult = userService.revokeToken(authenticatedUser.token)) {
            is Success -> ResponseEntity.ok("Logout was successful")
            is Failure -> {
                when (tokenRevocationResult.value) {
                    TokenRevocationError.TokenIsInvalid -> Problem(
                        type = Problem.tokenIsInvalid,
                        title = "Received token is invalid",
                        status = 400,
                        detail = "The received token is invalid",
                        instance = Uris.Users.logout()
                    ).toResponse()
                }
            }
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
                GettingUserError.UserNotFound -> Problem(
                    type = Problem.userNotFound,
                    title = "User not found",
                    status = 404,
                    detail = "The user with the given id was not found",
                    instance = Uris.Users.byId(id)
                ).toResponse()
            }
        }
    }

    @GetMapping(Uris.Users.RANKING)
    fun getUsersRanking(
        @RequestParam(name = "offset", defaultValue = "0") offset: Int,
        @RequestParam(name = "limit", defaultValue = "10") limit: Int
    ): ResponseEntity<PaginatedResult<UserRankInfo>> {
        logger.info("GET ${Uris.Users.RANKING}")
        val paginatedResult =
            userService.getUsersRanking(NonNegativeValue(offset), PositiveValue(limit))
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
