package gomoku.http.controllers

import gomoku.domain.PaginatedResult
import gomoku.domain.components.EmailError
import gomoku.domain.components.Id
import gomoku.domain.components.NonNegativeValue
import gomoku.domain.components.PasswordError
import gomoku.domain.components.PositiveValue
import gomoku.domain.components.Term
import gomoku.domain.components.TermError
import gomoku.domain.components.UsernameError
import gomoku.domain.user.AuthenticatedUser
import gomoku.domain.user.UserStatsInfo
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
import gomoku.utils.Success
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Range
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class UsersController(
    private val userService: UsersService,
) {

    companion object {
        const val HEADER_LOCATION_NAME = "Location"
        const val DEFAULT_OFFSET = "0"
        const val DEFAULT_LIMIT = "10"
    }

    /**
     * Creates a new user.
     * @param input the user input with registration data.
     * @return A [ResponseEntity] containing the [IdOutputModel] result of the user or an
     */
    @PostMapping(Uris.Users.REGISTER)
    fun createUser(
        @RequestBody
        input: UserCreateInputModel,
    ): ResponseEntity<*> {
        val instance = Uris.Users.register()
        return when (val emailResult = Email(input.email)) {
            is Failure -> when (emailResult.value) {
                EmailError.InvalidEmail -> Problem.invalidEmail(instance)
            }

            is Success -> {
                when (val usernameResult = Username(input.username)) {
                    is Failure -> when (usernameResult.value) {
                        UsernameError.UsernameBlank -> Problem.blankUsername(instance)
                        UsernameError.InvalidLength -> Problem.invalidUsernameLength(instance)
                    }

                    is Success -> {
                        when (val passwordResult = Password(input.password)) {
                            is Failure -> when (passwordResult.value) {
                                PasswordError.PasswordNotSafe -> Problem.insecurePassword(instance)
                                PasswordError.PasswordBlank -> Problem.blankPassword(instance)
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
                                            HEADER_LOCATION_NAME,
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
     * @param input the user input model with login data.
     * @return A [ResponseEntity] containing the [UserTokenCreateOutputModel] result of the user or an
     * appropriate [Problem] response.
     */
    @PostMapping(Uris.Users.TOKEN)
    fun createToken(
        @RequestBody
        input: UserCreateTokenInputModel,
    ): ResponseEntity<*> {
        val instance = Uris.Users.login()
        return when (val usernameResult = Username(input.username)) {
            is Failure -> when (usernameResult.value) {
                UsernameError.UsernameBlank -> Problem.blankUsername(instance)
                UsernameError.InvalidLength -> Problem.invalidUsernameLength(instance)
            }

            is Success -> {
                when (val passwordResult = Password(input.password)) {
                    is Failure -> when (passwordResult.value) {
                        PasswordError.PasswordNotSafe -> Problem.insecurePassword(instance)
                        PasswordError.PasswordBlank -> Problem.blankPassword(instance)
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
                                TokenCreationError.PasswordIsWrong -> Problem.invalidPassword(instance)

                                TokenCreationError.UsernameNotExists -> Problem.usernameDoesNotExist(
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
     * Revokes the token of the user, resulting in a logout.
     * @param authenticatedUser the authenticated user.
     * @return A [ResponseEntity] containing the [UserLogoutOutputModel] result of the user or an
     * appropriate [Problem] response.
     */
    @PostMapping(Uris.Users.LOGOUT)
    @RequiresAuthentication
    fun logout(
        authenticatedUser: AuthenticatedUser,
    ): ResponseEntity<*> {
        val instance = Uris.Users.logout()
        return when (val tokenRevocationResult = userService.revokeToken(authenticatedUser.token)) {
            is Success -> ResponseEntity.ok(UserLogoutOutputModel())
            // TODO("there's no way to get this error since interceptor does this work, but it's here for completeness sake")
            is Failure -> when (tokenRevocationResult.value) {
                TokenRevocationError.TokenIsInvalid -> Problem.invalidToken(instance)
            }
        }
    }

    /**
     * Retrieves user home data.
     * @param authenticatedUser the authenticated user.
     * @return A [ResponseEntity] containing the [UserOutputModel] result of the user or an
     * appropriate [Problem] response.
     */
    @GetMapping(Uris.Users.HOME)
    @RequiresAuthentication
    fun getUserHome(
        authenticatedUser: AuthenticatedUser,
    ): ResponseEntity<UserOutputModel> =
        ResponseEntity.ok(UserOutputModel.serializeFrom(authenticatedUser.user))

    /**
     * Retrieves an user by id.
     * @param id the user id.
     * @return A [ResponseEntity] containing the [UserOutputModel] result of the user or an
     * appropriate [Problem] response.
     */
    @GetMapping(Uris.Users.GET_BY_ID)
    fun getUserById(
        @Valid
        @Range(min = 1)
        @PathVariable
        id: Int,
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
     * Retrieves users statistic data.
     * @param offset the offset to start from.
     * @param limit the number of users to retrieve.
     * @return A [ResponseEntity] containing the [PaginatedResult] result of user statistics or an
     * appropriate [Problem] response.
     */
    @GetMapping(Uris.Users.STATS)
    fun getUsersStats(
        @Valid
        @Range(min = 0)
        @RequestParam(name = "offset", defaultValue = DEFAULT_OFFSET)
        offset: Int,
        @Valid
        @Range(min = 1)
        @RequestParam(name = "limit", defaultValue = DEFAULT_LIMIT)
        limit: Int,
    ): ResponseEntity<*> {
        val instance = Uris.Users.stats(offset = offset, limit = limit)
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
     * Retrieves user statistics data by id.
     * @param id the user id.
     * @return A [ResponseEntity] containing the [UserStatsOutputModel] result of user statistics or an
     * appropriate [Problem] response.
     */
    @GetMapping(Uris.Users.STATS_BY_ID)
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
     * Retrieves user statistics by username. Normally used for search queries.
     * @param user The authenticated user making the request.
     * @param offset The optional offset to start from (default is **0**).
     * @param limit The optional maximum number of user statistics results to be returned (default is **10**).
     * @param term The term to search for.
     * @return A [ResponseEntity] containing the [PaginatedResult] result of user statistics or an
     * appropriate [Problem] response.
     */
    @GetMapping(Uris.Users.STATS_BY_TERM)
    @RequiresAuthentication
    fun getUserStatsByTerm(
        user: AuthenticatedUser,
        @Valid
        @Range(min = 0)
        @RequestParam(name = "offset", defaultValue = DEFAULT_OFFSET)
        offset: Int,
        @Valid
        @Range(min = 1)
        @RequestParam(name = "limit", defaultValue = DEFAULT_LIMIT)
        limit: Int,
        @Valid
        @NotBlank
        @RequestParam(name = "term")
        term: String,
    ): ResponseEntity<*> {
        val instance = Uris.Users.statsByTerm(term = term, offset = offset, limit = limit)
        return when (val limitResult = PositiveValue(limit)) {
            is Failure -> Problem.invalidLimit(instance)
            is Success -> when (val offsetResult = NonNegativeValue(offset)) {
                is Failure -> Problem.invalidOffset(instance)
                is Success -> when (val termResult = Term(term)) {
                    is Failure -> when (termResult.value) {
                        TermError.InvalidLength -> Problem.invalidTermLength(instance)
                    }

                    is Success -> {
                        val paginatedResult: PaginatedResult<UserStatsInfo> = userService.getUserStatsByTerm(
                            term = termResult.value,
                            offset = offsetResult.value,
                            limit = limitResult.value
                        )
                        ResponseEntity.ok(paginatedResult)
                    }
                }
            }
        }
    }
}
