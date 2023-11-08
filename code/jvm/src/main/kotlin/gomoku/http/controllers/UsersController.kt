package gomoku.http.controllers

import gomoku.domain.components.Id
import gomoku.domain.components.NonNegativeValue
import gomoku.domain.components.PasswordError
import gomoku.domain.components.PositiveValue
import gomoku.domain.components.UsernameError
import gomoku.domain.user.AuthenticatedUser
import gomoku.domain.user.User
import gomoku.domain.user.components.Email
import gomoku.domain.user.components.Password
import gomoku.domain.user.components.Username
import gomoku.http.Uris
import gomoku.http.media.Problem
import gomoku.http.model.IdOutputModel
import gomoku.http.model.token.UserTokenCreateOutputModel
import gomoku.http.model.user.UserCreateInputModel
import gomoku.http.model.user.UserCreateTokenInputModel
import gomoku.http.model.user.UserLogoutOutputModel
import gomoku.http.model.user.UserOutputModel
import gomoku.http.model.user.UserStatsOutputModel
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
import org.springframework.http.HttpStatus
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

    /**
     * Creates a new user.
     * @param input the user input with registration data.
     */
    @PostMapping(Uris.Users.REGISTER)
    fun createUser(
        @RequestBody
        input: UserCreateInputModel
    ): ResponseEntity<*> {
        val instance = Uris.Users.register()
        return when (val emailResult = Email(input.email)) {
            is Failure -> Problem.invalidEmail(instance)
            is Success -> {
                when (val usernameResult = Username(input.username)) {
                    is Failure -> when (usernameResult.value) {
                        UsernameError.BlankUsername -> Problem.blankUsername(instance)
                        UsernameError.InvalidLength -> Problem.invalidUsernameLength(instance)
                        UsernameError.EmptyUsername -> Problem.emptyUsername(instance)
                    }

                    is Success -> {
                        when (val passwordResult = Password(input.password)) {
                            is Failure -> when (passwordResult.value) {
                                PasswordError.PasswordNotSafe -> Problem.insecurePassword(instance)
                                PasswordError.PasswordBlank -> Problem.blankPassword(instance)
                                PasswordError.PasswordIsEmpty -> Problem.emptyPassword(instance)
                            }

                            is Success -> {
                                val result = userService.createUser(
                                    username = usernameResult.value,
                                    email = emailResult.value,
                                    password = passwordResult.value
                                )
                                when (result) {
                                    is Success -> ResponseEntity.status(HttpStatus.CREATED)
                                        .header(
                                            "Location",
                                            Uris.Users.byId(result.value.value).toASCIIString()
                                        )
                                        .body(IdOutputModel.serializeFrom(result.value))

                                    is Failure -> when (result.value) {
                                        UserCreationError.UsernameAlreadyExists -> Problem.usernameAlreadyExists(
                                            username = usernameResult.value,
                                            instance = instance
                                        )

                                        UserCreationError.EmailAlreadyExists -> Problem.emailAlreadyExists(
                                            email = emailResult.value,
                                            instance = instance
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates a new token for the user.
     * @param input the user input with username and password.
     */
    @PostMapping(Uris.Users.TOKEN)
    fun createToken(
        @RequestBody
        input: UserCreateTokenInputModel
    ): ResponseEntity<*> {
        val instance = Uris.Users.login()
        return when (val usernameResult = Username(input.username)) {
            is Failure -> when (usernameResult.value) {
                UsernameError.BlankUsername -> Problem.blankUsername(instance)
                UsernameError.InvalidLength -> Problem.invalidUsernameLength(instance)
                UsernameError.EmptyUsername -> Problem.emptyUsername(instance)
            }

            is Success -> {
                when (val passwordResult = Password(input.password)) {
                    is Failure -> when (passwordResult.value) {
                        PasswordError.PasswordNotSafe -> Problem.insecurePassword(instance)
                        PasswordError.PasswordBlank -> Problem.blankPassword(instance)
                        PasswordError.PasswordIsEmpty -> Problem.emptyPassword(instance)
                    }

                    is Success -> {
                        val tokenCreationResult = userService.createToken(
                            username = usernameResult.value,
                            password = passwordResult.value
                        )
                        when (tokenCreationResult) {
                            is Success ->
                                ResponseEntity.ok(UserTokenCreateOutputModel(tokenCreationResult.value.tokenValue))

                            is Failure -> when (tokenCreationResult.value) {
                                TokenCreationError.PasswordIsWrong -> Problem.passwordIsWrong(instance)

                                TokenCreationError.UsernameNotExists -> Problem.usernameAlreadyExists(
                                    username = usernameResult.value,
                                    instance = instance
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Revokes the token of the user.
     * @param authenticatedUser the authenticated user.
     */
    @PostMapping(Uris.Users.LOGOUT)
    @RequiresAuthentication
    fun logout(
        authenticatedUser: AuthenticatedUser,
    ): ResponseEntity<*> {
        val instance = Uris.Users.logout()
        return when (val tokenRevocationResult = userService.revokeToken(authenticatedUser.token)) {
            is Success -> ResponseEntity.ok(UserLogoutOutputModel())
            is Failure -> when (tokenRevocationResult.value) {
                TokenRevocationError.TokenIsInvalid -> Problem.invalidToken(instance)
            }
        }
    }

    /**
     * Retrieves user home data.
     * @param authenticatedUser the authenticated user.
     */
    @GetMapping(Uris.Users.HOME)
    @RequiresAuthentication
    fun getUserHome(
        authenticatedUser: AuthenticatedUser,
    ): ResponseEntity<UserOutputModel> =
        ResponseEntity.ok(UserOutputModel.serializeFrom(authenticatedUser.user))

    /**
     * Retrieves users by id.
     * @param id the user id.
     */
    @GetMapping(Uris.Users.GET_BY_ID)
    fun getUserById(
        @Valid
        @Range(min = 1)
        @PathVariable
        id: Int
    ): ResponseEntity<*> {
        val instance = Uris.Users.byId(id)
        return when (val userIdResult = Id(id)) {
            is Failure -> Problem.invalidUserId(instance)
            is Success -> when (val getUserResult = userService.getUserById(userIdResult.value)) {
                is Success -> ResponseEntity.ok(UserOutputModel.serializeFrom(getUserResult.value))
                is Failure -> when (getUserResult.value) {
                    GettingUserError.UserNotFound -> Problem.userNotFound(
                        userId = userIdResult.value,
                        instance = instance
                    )
                }
            }
        }
    }

    /**
     * Retrieves users stats.
     * @param offset the offset to start from.
     * @param limit the number of users to retrieve.
     */
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
        val instance = Uris.Users.stats()
        return when (val offsetResult = NonNegativeValue(offset)) {
            is Failure -> Problem.invalidOffset(instance)
            is Success -> when (val limitResult = PositiveValue(limit)) {
                is Failure -> Problem.invalidLimit(instance)
                is Success -> {
                    val paginatedResult =
                        userService.getUsersStats(offsetResult.value, limitResult.value)
                    ResponseEntity.ok(paginatedResult)
                }
            }
        }
    }

    /**
     * Retrieves user stats by id.
     * @param id the user id.
     */
    @GetMapping(Uris.Users.STATS_BY_ID)
    @NotTested
    fun getUserStats(
        @Valid
        @Range(min = 1)
        @PathVariable
        id: Int,
    ): ResponseEntity<*> {
        val instance = Uris.Users.byIdStats(id)
        return when (val idResult = Id(id)) {
            is Failure -> Problem.invalidUserId(instance)
            is Success -> {
                val getUserStatsResult = userService.getUserStats(idResult.value)
                    ?: return Problem.userNotFound(userId = idResult.value, instance = instance)
                ResponseEntity.ok(UserStatsOutputModel.serializeFrom(getUserStatsResult))
            }
        }
    }

    /**
     * Edits user data.
     * @param user the authenticated user.
     */
    @PutMapping(Uris.Users.EDIT_BY_ID)
    @RequiresAuthentication
    @NotTested
    fun editUser(
        user: AuthenticatedUser,
    ): ResponseEntity<User> {
        TODO("Not yet implemented")
    }
}
