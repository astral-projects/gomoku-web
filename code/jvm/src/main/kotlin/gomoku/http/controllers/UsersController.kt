package gomoku.http.controllers

import gomoku.domain.Id
import gomoku.domain.NonNegativeValue
import gomoku.domain.PaginatedResult
import gomoku.domain.PositiveValue
import gomoku.domain.errors.InvalidUsernameError
import gomoku.domain.user.AuthenticatedUser
import gomoku.domain.user.Email
import gomoku.domain.user.Password
import gomoku.domain.user.User
import gomoku.domain.user.UserRankInfo
import gomoku.domain.user.Username
import gomoku.http.Uris
import gomoku.http.media.Problem
import gomoku.http.model.IdOutputModel
import gomoku.http.model.token.UserTokenCreateOutputModel
import gomoku.http.model.user.UserCreateInputModel
import gomoku.http.model.user.UserCreateTokenInputModel
import gomoku.http.model.user.UserOutputModel
import gomoku.services.user.GettingUserError
import gomoku.services.user.TokenCreationError
import gomoku.services.user.TokenRevocationError
import gomoku.services.user.UserCreationError
import gomoku.services.user.UsersService
import gomoku.utils.Either
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

// TODO(Review username and password validation because they are getting caught by the global exception handler)
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
        return when (val validEmail = Email(input.email)) {
            is Failure -> Problem(
                type = Problem.invalidEmail,
                title = "Received email is invalid",
                status = 400,
                detail = "The received email is invalid",
                instance = Uris.Users.register()
            ).toResponse()
            is Success -> {
                when (val validUsername = Username(input.username)) {
                    is Failure -> when (validUsername.value) {
                        InvalidUsernameError.BlankUsername -> Problem(
                            type = Problem.blankUsername,
                            title = "Received username is blank",
                            status = 400,
                            detail = "The received username is blank",
                            instance = Uris.Users.register()
                        ).toResponse()
                        InvalidUsernameError.InvalidLength -> Problem(
                            type = Problem.invalidUsernameLength,
                            title = "Received username is invalid",
                            status = 400,
                            detail = "The received username must be between 5 and 30 characters",
                            instance = Uris.Users.register()
                        ).toResponse()
                    }
                    is Success -> {
                        val res = userService.createUser(
                            username = validUsername.value,
                            email = validEmail.value,
                            password = Password(input.password)
                        )
                        when (res) {
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
                }
            }
        }
    }

    @PostMapping(Uris.Users.TOKEN)
    fun createToken(
        @Valid @RequestBody
        input: UserCreateTokenInputModel
    ): ResponseEntity<*> {
        logger.info("POST ${Uris.Users.TOKEN}")
        return when (val validUsername = Username(input.username)) {
            is Failure -> when (validUsername.value) {
                InvalidUsernameError.BlankUsername -> Problem(
                    type = Problem.blankUsername,
                    title = "Received username is blank",
                    status = 400,
                    detail = "The received username is blank",
                    instance = Uris.Users.login()
                ).toResponse()
                InvalidUsernameError.InvalidLength -> Problem(
                    type = Problem.invalidUsernameLength,
                    title = "Received username is invalid",
                    status = 400,
                    detail = "The received username must be between 5 and 30 characters",
                    instance = Uris.Users.login()
                ).toResponse()
            }
            is Success -> {
                val res = userService.createToken(
                    username = validUsername.value,
                    password = Password(input.password)
                )
                when (res) {
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
        }
    }

    @PostMapping(Uris.Users.LOGOUT)
    fun logout(authenticatedUser: AuthenticatedUser): ResponseEntity<*> {
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
        return when (val validId = Id(id)) {
            is Failure -> Problem(
                type = Problem.invalidId,
                title = "Received id is invalid",
                status = 404,
                detail = "The received id is invalid",
                instance = Uris.Users.byId(id)
            ).toResponse()

            is Success ->
                when (val res = userService.getUserById(validId.value)) {
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
    }

    @GetMapping(Uris.Users.STATS)
    fun getUsersStats(
        @Valid @Range(min = 0) @RequestParam(name = "offset", defaultValue = "0") offset: Int,
        @Valid @Range(min = 1) @RequestParam(name = "limit", defaultValue = "10") limit: Int
    ): ResponseEntity<*> {
        logger.info("GET ${Uris.Users.STATS}")
        return when (val validNonNegative = NonNegativeValue(offset)) {
            is Failure -> Problem(
                type = Problem.invalidOffset,
                title = "Received offset is invalid",
                status = 400,
                detail = "The received offset must be a non-negative integer",
                instance = Uris.Users.stats()
            ).toResponse()

            is Success -> {
                return when (val validValue = PositiveValue(limit)) {
                    is Failure -> Problem(
                        type = Problem.invalidLimit,
                        title = "Received limit is invalid",
                        status = 400,
                        detail = "The received limit must be a positive integer",
                        instance = Uris.Users.stats()
                    ).toResponse()

                    is Success -> {
                    val paginatedResult =
                        userService.getUsersStats(validNonNegative.value, validValue.value)
                    ResponseEntity.ok(paginatedResult)
                    }
                }
            }
        }
    }

    @GetMapping(Uris.Users.STATS_BY_ID)
    @NotTested
    fun getUserStats(): ResponseEntity<UserRankInfo> {
        TODO("Not yet implemented")
    }

    @PutMapping(Uris.Users.EDIT_BY_ID)
    @NotTested
    fun editUser(user: AuthenticatedUser): ResponseEntity<User> {
        TODO("Not yet implemented")
    }

}
