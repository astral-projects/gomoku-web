package gomoku.http.controllers

import gomoku.domain.Id
import gomoku.domain.NonNegativeValue
import gomoku.domain.PositiveValue
import gomoku.domain.errors.InvalidPasswordError
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
import gomoku.utils.Failure
import gomoku.utils.NotTested
import gomoku.utils.Success
import jakarta.validation.Valid
import org.hibernate.validator.constraints.Range
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
        @RequestBody
        input: UserCreateInputModel
    ): ResponseEntity<*> {
        return when (val validEmail = Email(input.email)) {
            is Failure -> Problem(
                type = Problem.invalidEmail,
                title = "Email invalid",
                status = 400,
                detail = "The email is not a valid email address",
                instance = Uris.Users.register()
            ).toResponse()
            is Success -> {
                when (val validUsername = Username(input.username)) {
                    is Failure -> when (validUsername.value) {
                        InvalidUsernameError.BlankUsername -> Problem(
                            type = Problem.blankUsername,
                            title = "Username blank",
                            status = 400,
                            detail = "The received username is blank",
                            instance = Uris.Users.register()
                        ).toResponse()
                        InvalidUsernameError.InvalidLength -> Problem(
                            type = Problem.invalidUsernameLength,
                            title = "Username invalid",
                            status = 400,
                            detail = "The received username must be between 5 and 30 characters",
                            instance = Uris.Users.register()
                        ).toResponse()
                    }
                    is Success -> {
                        when (val validPassword = Password(input.password)) {
                            is Failure -> when (validPassword.value) {
                                InvalidPasswordError.PasswordIsEmpty -> Problem(
                                    type = Problem.passwordIsEmpty,
                                    title = "Password empty",
                                    status = 400,
                                    detail = "Password must not be empty",
                                    instance = Uris.Users.register()
                                ).toResponse()
                                InvalidPasswordError.PasswordNotSafe -> Problem(
                                    type = Problem.insecurePassword,
                                    title = "Password not safe",
                                    status = 400,
                                    detail = "Password must be between 8 and 40 characters",
                                    instance = Uris.Users.register()
                                ).toResponse()
                            }
                            is Success -> {
                                val res = userService.createUser(
                                    username = validUsername.value,
                                    email = validEmail.value,
                                    password = validPassword.value
                                )
                                when (res) {
                                    is Success -> ResponseEntity.status(201)
                                        .header(
                                            "Location",
                                            Uris.Users.byId(res.value.value).toASCIIString()
                                        )
                                        .body(IdOutputModel.serializeFrom(res.value))

                                    is Failure -> when (res.value) {
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
        }
    }

    @PostMapping(Uris.Users.TOKEN)
    fun createToken(
        @RequestBody
        input: UserCreateTokenInputModel
    ): ResponseEntity<*> {
        return when (val validUsername = Username(input.username)) {
            is Failure -> when (validUsername.value) {
                InvalidUsernameError.BlankUsername -> Problem(
                    type = Problem.blankUsername,
                    title = "Username is blank",
                    status = 400,
                    detail = "Username must have a value",
                    instance = Uris.Users.login()
                ).toResponse()
                InvalidUsernameError.InvalidLength -> Problem(
                    type = Problem.invalidUsernameLength,
                    title = "Username invalid",
                    status = 400,
                    detail = "Username must be between 5 and 30 characters",
                    instance = Uris.Users.login()
                ).toResponse()
            }
            is Success -> {
                when (val validPassword = Password(input.password)) {
                    is Failure -> when (validPassword.value) {
                        InvalidPasswordError.PasswordIsEmpty -> Problem(
                            type = Problem.passwordIsEmpty,
                            title = "Password is empty",
                            status = 400,
                            detail = "Password must have a value",
                            instance = Uris.Users.login()
                        ).toResponse()
                        InvalidPasswordError.PasswordNotSafe -> Problem(
                            type = Problem.insecurePassword,
                            title = "Password not safe",
                            status = 400,
                            detail = "Password must be between 8 and 40 characters",
                            instance = Uris.Users.login()
                        ).toResponse()
                    }
                    is Success -> {
                        val res = userService.createToken(
                            username = validUsername.value,
                            password = validPassword.value
                        )
                        when (res) {
                            is Success ->
                                ResponseEntity.status(200)
                                    .body(UserTokenCreateOutputModel(res.value.tokenValue))

                            is Failure -> when (res.value) {
                                TokenCreationError.PasswordIsWrong -> Problem(
                                    type = Problem.wrongPassword,
                                    title = "Wrong password",
                                    status = 400,
                                    detail = "Invalid password",
                                    instance = Uris.Users.login()
                                ).toResponse()

                                TokenCreationError.UsernameNotExists -> Problem(
                                    type = Problem.usernameDoesNotExist,
                                    title = "Username not exists",
                                    status = 400,
                                    detail = "Username does not exists",
                                    instance = Uris.Users.login()
                                ).toResponse()
                            }
                        }
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
        @Valid
        @Range(min = 0)
        @RequestParam(name = "offset", defaultValue = "0")
        offset: Int,
        @Valid
        @Range(min = 1)
        @RequestParam(name = "limit", defaultValue = "10")
        limit: Int
    ): ResponseEntity<*> {
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
