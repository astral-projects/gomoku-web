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
import gomoku.http.model.IdOutputModel
import gomoku.http.model.token.UserTokenCreateOutputModel
import gomoku.http.model.user.UserCreateInputModel
import gomoku.http.model.user.UserCreateTokenInputModel
import gomoku.http.model.user.UserLogoutOutputModel
import gomoku.infra.siren
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
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class UsersController(
    private val userService: UsersService
) {

    companion object {
        const val HEADER_LOCATION_NAME = "Location"
        const val DEFAULT_ITEMS_PER_PAGE = "10"
        const val DEFAULT_PAGE = "1"
        const val FIRST_PAGE = 1
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

                                    is Success -> ResponseEntity.status(HttpStatus.CREATED)
                                        .header(
                                            HEADER_LOCATION_NAME,
                                            Uris.Users.byId(result.value.value).toASCIIString()
                                        )
                                        .body(
                                            siren(IdOutputModel.serializeFrom(result.value)) {
                                                clazz("user")
                                                link(Uris.Users.byId(result.value.value), Rels.USER)
                                                action(
                                                    name = "login",
                                                    href = Uris.Users.login(),
                                                    method = HttpMethod.POST,
                                                    type = MediaType.APPLICATION_JSON_VALUE
                                                ) {
                                                    clazz("login")
                                                    textField("username")
                                                    textField("password")
                                                    link(Uris.Users.login(), Rels.SELF)
                                                }
                                            }
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
     * Creates a new token for the user.
     * @param input the user input model with login data.
     * @return A [ResponseEntity] containing the [siren] representation of the user or an
     * appropriate [Problem] response.
     */
    @PostMapping(Uris.Users.TOKEN)
    fun createToken(
        @RequestBody
        input: UserCreateTokenInputModel
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

                                ResponseEntity.ok(
                                    siren(
                                        UserTokenCreateOutputModel(tokenCreationResult.value.tokenValue)
                                    ) {
                                        clazz("token")
                                        link(Uris.Users.login(), Rels.SELF)
                                        entity(loggedUser, Rels.USER) {
                                            clazz("user")
                                            link(Uris.Users.byId(loggedUser.id.value), Rels.SELF)
                                        }
                                    }
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
     * @return A [ResponseEntity] containing the [siren] result of the user or an
     * appropriate [Problem] response.
     */
    @PostMapping(Uris.Users.LOGOUT)
    @RequiresAuthentication
    fun logout(
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Users.logout()
        return when (val tokenRevocationResult = userService.revokeToken(authenticatedUser.token)) {
            is Failure -> when (tokenRevocationResult.value) {
                TokenRevocationError.TokenIsInvalid -> Problem.invalidToken(instance)
            }

            is Success -> ResponseEntity.ok(
                siren(UserLogoutOutputModel()) {
                    clazz("logout")
                    requireAuth()
                    link(Uris.Users.logout(), Rels.SELF)
                }
            )
        }
    }

    /**
     * Retrieves user home data.
     * @param authenticatedUser the authenticated user.
     * @return A [ResponseEntity] containing the [siren] representation of the user or an
     * appropriate [Problem] response.
     */
    @GetMapping(Uris.Users.HOME)
    @RequiresAuthentication
    fun getUserHome(
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> =
        ResponseEntity.ok(
            siren(authenticatedUser.user) {
                clazz("home")
                requireAuth()
                link(Uris.Users.home(), Rels.SELF)
                entity(authenticatedUser.user, Rels.USER) {
                    clazz("user")
                    link(Uris.Users.byId(authenticatedUser.user.id.value), Rels.SELF)
                }
                action(
                    name = "logout",
                    href = Uris.Users.logout(),
                    method = HttpMethod.POST,
                    type = MediaType.APPLICATION_JSON_VALUE
                ) {
                    clazz("logout")
                    // should create a field for header?
                }
            }
        )

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

                is Success -> ResponseEntity.ok(
                    siren(getUserResult.value) {
                        clazz("user")
                        link(Uris.Users.byId(getUserResult.value.id.value), Rels.SELF)
                    }
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
                        ResponseEntity.ok(
                            siren(paginatedResult) {
                                clazz("users-stats")
                                link(
                                    Uris.Users.stats(paginatedResult.currentPage, paginatedResult.itemsPerPage),
                                    Rels.SELF
                                )
                                // Link to the next page if available
                                if (paginatedResult.currentPage < paginatedResult.totalPages) {
                                    link(
                                        Uris.Users.stats(
                                            paginatedResult.currentPage + 1,
                                            paginatedResult.itemsPerPage
                                        ),
                                        Rels.NEXT
                                    )
                                }

                                // Link to the previous page if available
                                if (paginatedResult.currentPage > 1) {
                                    link(
                                        Uris.Users.stats(
                                            paginatedResult.currentPage - 1,
                                            paginatedResult.itemsPerPage
                                        ),
                                        Rels.PREV
                                    )
                                }

                                // Link to the first page if the current page is not the first page
                                if (paginatedResult.currentPage > 1) {
                                    link(Uris.Users.stats(1, paginatedResult.itemsPerPage), Rels.FIRST)
                                }

                                // Link to the last page if the current page is not the last page
                                if (paginatedResult.currentPage < paginatedResult.totalPages) {
                                    link(
                                        Uris.Users.stats(paginatedResult.totalPages, paginatedResult.itemsPerPage),
                                        Rels.LAST
                                    )
                                }
                            }
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
        id: Int,
        user: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Users.byIdStats(id)
        return when (val idResult = Id(id)) {
            is Failure -> Problem.invalidUserId(instance)
            is Success -> {
                val getUserStatsResult = userService.getUserStats(idResult.value)
                    ?: return Problem.userNotFound(userId = idResult.value, instance = instance)
                ResponseEntity.ok(
                    siren(getUserStatsResult) {
                        clazz("user-stats")
                        requireAuth()
                        link(Uris.Users.byIdStats(getUserStatsResult.id.value), Rels.SELF)
                    }
                )
            }
        }
    }

    /**
     * Retrieves user statistics by username. Normally used for search queries.
     * @param user The authenticated user making the request.
     * @param page The optional page to start from (default is **1**).
     * @param itemsPerPage The optional maximum number of user statistics results to be returned (default is **10**).
     * @param term The term to search for.
     * @return A [ResponseEntity] containing a [siren] representation of the user statistics or an
     * appropriate [Problem] response.
     */
    @GetMapping(Uris.Users.STATS_BY_TERM)
    @RequiresAuthentication
    fun getUserStatsByTerm(
        user: AuthenticatedUser,
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
                            ResponseEntity.ok(
                                siren(paginatedResult) {
                                    clazz("users-stats")
                                    requireAuth()
                                    link(
                                        Uris.Users.statsByTerm(
                                            termResult.value.value,
                                            paginatedResult.currentPage,
                                            paginatedResult.itemsPerPage
                                        ),
                                        Rels.SELF
                                    )
                                    // Link to the next page if available
                                    if (paginatedResult.currentPage < paginatedResult.totalPages) {
                                        link(
                                            Uris.Users.statsByTerm(
                                                termResult.value.value,
                                                paginatedResult.currentPage + 1,
                                                paginatedResult.itemsPerPage
                                            ),
                                            Rels.NEXT
                                        )
                                    }

                                    // Link to the previous page if available
                                    if (paginatedResult.currentPage > 1) {
                                        link(
                                            Uris.Users.statsByTerm(
                                                termResult.value.value,
                                                paginatedResult.currentPage - 1,
                                                paginatedResult.itemsPerPage
                                            ),
                                            Rels.PREV
                                        )
                                    }

                                    // Link to the first page if the current page is not the first page
                                    if (paginatedResult.currentPage > 1) {
                                        link(
                                            Uris.Users.statsByTerm(
                                                termResult.value.value,
                                                FIRST_PAGE,
                                                paginatedResult.itemsPerPage
                                            ),
                                            Rels.FIRST
                                        )
                                    }

                                    // Link to the last page if the current page is not the last page
                                    if (paginatedResult.currentPage < paginatedResult.totalPages) {
                                        link(
                                            Uris.Users.statsByTerm(
                                                termResult.value.value,
                                                paginatedResult.totalPages,
                                                paginatedResult.itemsPerPage
                                            ),
                                            Rels.LAST
                                        )
                                    }

                                    entity(user.user, Rels.USER) {
                                        clazz("user")
                                        link(Uris.Users.byId(user.user.id.value), Rels.SELF)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
