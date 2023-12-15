package gomoku.http.controllers

import gomoku.domain.PaginatedResult
import gomoku.domain.components.EmailError
import gomoku.domain.components.Id
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
import gomoku.http.Rels
import gomoku.http.Uris
import gomoku.http.media.Problem
import gomoku.http.media.siren.siren
import gomoku.http.media.siren.sirenResponse
import gomoku.http.model.user.UserCreateInputModel
import gomoku.http.model.user.UserCreateTokenInputModel
import gomoku.http.model.user.UserOutputModels
import gomoku.services.system.SystemService
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
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class UsersController(
    private val userService: UsersService,
    private val systemService: SystemService
) {

    companion object {
        private val userOutputModels = UserOutputModels()
        const val DEFAULT_ITEMS_PER_PAGE = "10"
        const val DEFAULT_PAGE = "1"
        const val HEADER_SET_COOKIE_NAME = "Set-Cookie"
        const val AUTHORIZATION_COOKIE_NAME = "_autho"
        const val USER_NAME_COOKIE = "user_name"
        const val USER_ID_COOKIE = "user_id"
        const val USER_COOKIE_PROPS = "Path=/"
        const val AUTHORIZATION_COOKIE_PROPS = "HttpOnly; SameSite=Strict; Path=/"
        const val AUTHORIZATION_COOKIE_DELETE_PROPS =
            "Expires=Thu, 01 Jan 1970 00:00:00 GMT; $AUTHORIZATION_COOKIE_PROPS"
        const val USER_COOKIE_DELETE_PROPS = "Expires=Thu, 01 Jan 1970 00:00:00 GMT; $USER_COOKIE_PROPS"
        private const val MOZILLA_USER_AGENT = "Mozilla"
        private const val CHROME_USER_AGENT = "Chrome"
        private const val SAFARI_USER_AGENT = "Safari"

        private fun isWeb(userAgent: String): Boolean {
            return userAgent.contains(MOZILLA_USER_AGENT) || userAgent.contains(CHROME_USER_AGENT) || userAgent.contains(
                SAFARI_USER_AGENT
            )
        }
    }

    /**
     * Retrieves all the recipes of the API.
     * @return A [ResponseEntity] containing the [siren] representation of the recipes.
     */
    @GetMapping(Uris.Users.HOME)
    fun getHome(): ResponseEntity<*> {
        return ResponseEntity.ok().sirenResponse(
            userOutputModels.home()
        )
    }

    /**
     * Creates a new user.
     * @param input the user input with registration data.
     * @return A [ResponseEntity] containing the [siren] result of the user or an
     * appropriate [Problem] response.
     */
    @PostMapping(Uris.Users.REGISTER)
    fun createUser(
        @RequestBody
        input: UserCreateInputModel
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

                                    is Success -> {
                                        ResponseEntity.created(
                                            Uris.Users.byId(result.value.value)
                                        ).sirenResponse(
                                            userOutputModels.createUser(result.value)
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
     * @return A [ResponseEntity] containing the [siren] representation of the user or an
     * appropriate [Problem] response.
     */
    @PostMapping(Uris.Users.TOKEN)
    fun createToken(
        @RequestBody
        input: UserCreateTokenInputModel,
        @RequestHeader("User-Agent") userAgent: String
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
                            is Failure -> when (tokenCreationResult.value) {
                                TokenCreationError.PasswordIsWrong -> Problem.invalidPassword(instance)

                                TokenCreationError.UsernameNotExists -> Problem.usernameDoesNotExist(
                                    username = usernameResult.value,
                                    instance = instance
                                )
                            }

                            is Success -> {
                                val loggedUser = userService.getUserByToken(tokenCreationResult.value.tokenValue)
                                    ?: return Problem.invalidToken(instance)

                                // evaluate if the user agent is web or not and set the cookie accordingly
                                if (isWeb(userAgent)) {
                                    ResponseEntity.ok()
                                        // set the _autho cookie with token value
                                        .header(
                                            HEADER_SET_COOKIE_NAME,
                                            "$AUTHORIZATION_COOKIE_NAME=${tokenCreationResult.value.tokenValue}; $AUTHORIZATION_COOKIE_PROPS"

                                        )
                                        // set the _user cookie with username value.
                                        .header(
                                            HEADER_SET_COOKIE_NAME,
                                            "$USER_NAME_COOKIE=${loggedUser.username}; $USER_COOKIE_PROPS"
                                        )
                                        .header(
                                            HEADER_SET_COOKIE_NAME,
                                            "$USER_ID_COOKIE=${loggedUser.id}; $USER_COOKIE_PROPS"
                                        )
                                        .sirenResponse(
                                            userOutputModels.tokenCreation(loggedUser, tokenCreationResult.value)
                                        )
                                } else {
                                    ResponseEntity.ok().sirenResponse(
                                        userOutputModels.tokenCreation(loggedUser, tokenCreationResult.value)
                                    )
                                }
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
     * @return A [ResponseEntity] containing the [siren] result of the user or an
     * appropriate [Problem] response.
     */
    @PostMapping(Uris.Users.LOGOUT)
    fun logout(
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Users.logout()
        return when (val tokenRevocationResult = userService.revokeToken(authenticatedUser.token)) {
            is Failure -> when (tokenRevocationResult.value) {
                TokenRevocationError.TokenIsInvalid -> Problem.invalidToken(instance)
            }

            is Success -> {
                ResponseEntity.ok()
                    .header(HEADER_SET_COOKIE_NAME, "$AUTHORIZATION_COOKIE_NAME=; $AUTHORIZATION_COOKIE_DELETE_PROPS")
                    .header(HEADER_SET_COOKIE_NAME, "$USER_NAME_COOKIE=; $USER_COOKIE_DELETE_PROPS")
                    .header(HEADER_SET_COOKIE_NAME, "$USER_ID_COOKIE=; $USER_COOKIE_DELETE_PROPS")
                    .sirenResponse(
                        userOutputModels.logout()
                    )
            }
        }
    }

    /**
     * Retrieves user home data.
     * @param authenticatedUser the authenticated user.
     * @return A [ResponseEntity] containing the [siren] representation of the user or an
     * appropriate [Problem] response.
     */
    @GetMapping(Uris.Users.ME)
    fun getUserHome(
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        return ResponseEntity.ok().sirenResponse(
            userOutputModels.homeAuthenticated(
                authenticatedUser = authenticatedUser,
                usersStats = userService.getUsersStats(),
                userStats = userService.getUserStats(authenticatedUser.user.id),
                systemInfo = systemService.getSystemInfo()
            )
        )
    }

    /**
     * Retrieves a user by id.
     * @param id the user id.
     * @return A [ResponseEntity] containing the [siren] result of the user or an
     * appropriate [Problem] response.
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
                is Failure -> when (getUserResult.value) {
                    GettingUserError.UserNotFound -> Problem.userNotFound(
                        userId = userIdResult.value,
                        instance = instance
                    )
                }

                is Success -> ResponseEntity.ok().sirenResponse(
                    userOutputModels.userById(getUserResult.value)
                )
            }
        }
    }

    /**
     * Retrieves users statistic data.
     * @param page The optional page to start from (default is **1**).
     * @param itemsPerPage The optional maximum number of user statistics results to be returned (default is **10**).
     * @return A [ResponseEntity] containing the [siren] representation of user statistics or an
     * appropriate [Problem] response.
     */
    @GetMapping(Uris.Users.STATS)
    fun getUsersStats(
        @Valid
        @Range(min = 1)
        @RequestParam(name = "page", defaultValue = DEFAULT_PAGE)
        page: Int,
        @Valid
        @Range(min = 1)
        @RequestParam(name = "itemsPerPage", defaultValue = DEFAULT_ITEMS_PER_PAGE)
        itemsPerPage: Int
    ): ResponseEntity<*> {
        val instance = Uris.Users.stats(page, itemsPerPage)
        return when (val pageResult = PositiveValue(page)) {
            is Failure -> Problem.invalidPage(instance)
            is Success -> {
                when (val itemsPerPageResult = PositiveValue(itemsPerPage)) {
                    is Failure -> Problem.invalidItemsPerPage(instance)
                    is Success -> {
                        val paginatedResult: PaginatedResult<UserStatsInfo> =
                            userService.getUsersStats(pageResult.value, itemsPerPageResult.value)
                        ResponseEntity.ok().sirenResponse(
                            userOutputModels.usersStats(
                                paginatedResult = paginatedResult
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Retrieves user statistics data by id.
     * @param id the user id.
     * @return A [ResponseEntity] containing the [siren] result of user statistics or an
     * appropriate [Problem] response.
     */
    @GetMapping(Uris.Users.STATS_BY_ID)
    fun getUserStats(
        @Valid
        @Range(min = 1)
        @PathVariable
        id: Int
    ): ResponseEntity<*> {
        val instance = Uris.Users.byIdStats(id)
        return when (val idResult = Id(id)) {
            is Failure -> Problem.invalidUserId(instance)
            is Success -> {
                val getUserStatsResult = userService.getUserStats(idResult.value)
                    ?: return Problem.userNotFound(userId = idResult.value, instance = instance)
                ResponseEntity.ok().sirenResponse(
                    siren(getUserStatsResult) {
                        clazz("user-stats")
                        requireAuth()
                        link(instance, Rels.SELF)
                    }
                )
            }
        }
    }

    /**
     * Retrieves user statistics by username. Normally used for search queries.
     * @param page The optional page to start from (default is **1**).
     * @param itemsPerPage The optional maximum number of user statistics results to be returned (default is **10**).
     * @param term The term to search for.
     * @return A [ResponseEntity] containing a [siren] representation of the user statistics or an
     * appropriate [Problem] response.
     */
    @GetMapping(Uris.Users.STATS_BY_TERM)
    fun getUserStatsByTerm(
        @Valid
        @Range(min = 1)
        @RequestParam(name = "page", defaultValue = DEFAULT_PAGE)
        page: Int,
        @Valid
        @Range(min = 1)
        @RequestParam(name = "itemsPerPage", defaultValue = DEFAULT_ITEMS_PER_PAGE)
        itemsPerPage: Int,
        @Valid
        @NotBlank
        @RequestParam(name = "term")
        term: String
    ): ResponseEntity<*> {
        val instance = Uris.Users.statsByTerm(term, page, itemsPerPage)
        return when (val pageResult = PositiveValue(page)) {
            is Failure -> Problem.invalidPage(instance)
            is Success -> when (val termResult = Term(term)) {
                is Failure -> when (termResult.value) {
                    TermError.InvalidLength -> Problem.invalidTermLength(instance)
                }

                is Success -> {
                    when (val itemsPerPageResult = PositiveValue(itemsPerPage)) {
                        is Failure -> Problem.invalidItemsPerPage(instance)
                        is Success -> {
                            val paginatedResult: PaginatedResult<UserStatsInfo> =
                                userService.getUserStatsByTerm(
                                    term = termResult.value,
                                    page = pageResult.value,
                                    itemsPerPage = itemsPerPageResult.value
                                )
                            ResponseEntity.ok().sirenResponse(
                                userOutputModels.usersStatsByTerm(
                                    paginatedResult = paginatedResult,
                                    termResult = termResult.value
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
