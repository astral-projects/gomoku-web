package gomoku.http.model.user

import gomoku.domain.PaginatedResult
import gomoku.domain.SystemInfo
import gomoku.domain.components.Id
import gomoku.domain.components.Term
import gomoku.domain.user.AuthenticatedUser
import gomoku.domain.user.User
import gomoku.domain.user.UserStatsInfo
import gomoku.http.Rels
import gomoku.http.UriTemplates
import gomoku.http.Uris
import gomoku.http.media.siren.SirenModel
import gomoku.http.media.siren.siren
import gomoku.http.model.IdOutputModel
import gomoku.http.model.game.SystemInfoOutputModel
import gomoku.http.model.token.UserTokenCreateOutputModel
import gomoku.services.user.TokenExternalInfo
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType

class UserOutputModels {

    companion object {
        const val FIRST_PAGE = 1
    }

    fun home(): SirenModel<HomeOutputModel> =
        siren(HomeOutputModel()) {
            clazz("home")
            link(Uris.Users.home(), Rels.SELF)
            recipeLink(UriTemplates.SYSTEM_INFO, Rels.SYSTEM)
            recipeLink(UriTemplates.ME, Rels.ME)
            recipeLink(UriTemplates.REGISTER, Rels.REGISTER)
            recipeLink(UriTemplates.LOGIN, Rels.LOGIN)
            recipeLink(UriTemplates.LOGOUT, Rels.LOGOUT)
            recipeLink(UriTemplates.GAME, Rels.GAME)
            recipeLink(UriTemplates.FIND_GAME, Rels.GAMES)
            recipeLink(UriTemplates.EXIT_GAME, Rels.EXIT_GAME)
            recipeLink(UriTemplates.MAKE_MOVE, Rels.MAKE_MOVE)
            recipeLink(UriTemplates.VARIANTS, Rels.VARIANTS)
            recipeLink(UriTemplates.LOBBY, Rels.LOBBY)
            recipeLink(UriTemplates.EXIT_LOBBY, Rels.EXIT_LOBBY)
            recipeLink(UriTemplates.USER, Rels.USER)
            recipeLink(UriTemplates.USERS_STATS_BY_TERM, Rels.USER_STATS_BY_TERM)
            recipeLink(UriTemplates.USER_STATS, Rels.USER_STATS)
            recipeLink(UriTemplates.USERS_STATS, Rels.USERS_STATS)
        }

    /**
     * Output model for the user registration using media type application/vnd.siren+json
     * implemented using [SirenModel].
     *
     * @param userId The user id.
     * @return A [SirenModel] containing the [userId] representation.
     */
    fun createUser(userId: Id): SirenModel<IdOutputModel> =
        siren(IdOutputModel.serializeFrom(userId)) {
            clazz("user")
            link(Uris.Users.byId(userId.value), Rels.USER)
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

    /**
     * Output model for the user login using media type application/vnd.siren+json
     * implemented using [SirenModel].
     *
     * @param loggedUser The logged user.
     * @param tokenCreationResult The token creation result.
     * @return A [SirenModel] containing the [loggedUser] and [tokenCreationResult] representation.
     */
    fun tokenCreation(
        loggedUser: User,
        tokenCreationResult: TokenExternalInfo
    ): SirenModel<UserTokenCreateOutputModel> =
        siren(
            UserTokenCreateOutputModel(tokenCreationResult.tokenValue)
        ) {
            clazz("token")
            link(Uris.Users.login(), Rels.SELF)
            entity(loggedUser, Rels.USER) {
                clazz("user")
                link(Uris.Users.byId(loggedUser.id.value), Rels.SELF)
            }
        }

    /**
     * Output model for the logout using media type application/vnd.siren+json
     * implemented using [SirenModel].
     *
     */
    fun logout() =
        siren(UserLogoutOutputModel()) {
            clazz("logout")
            requireAuth()
            link(Uris.Users.logout(), Rels.SELF)
        }

    /**
     * Output model for the home using media type application/vnd.siren+json
     * implemented using [SirenModel].
     *
     * @param authenticatedUser The authenticated user.
     * @param usersStats The paginated result of the users stats.
     * @param userStats The user stats.
     * @param systemInfo The system information.
     * @return A [SirenModel] containing the [authenticatedUser], [usersStats], [userStats] and [systemInfo] representation.
     */
    fun homeAuthenticated(
        authenticatedUser: AuthenticatedUser,
        usersStats: PaginatedResult<UserStatsInfo>,
        userStats: UserStatsInfo?,
        systemInfo: SystemInfo
    ): SirenModel<User> =
        siren(authenticatedUser.user) {
            clazz("home")
            requireAuth()
            link(Uris.Users.home(), Rels.SELF)
            entity(
                usersStats,
                Rels.USERS_STATS
            ) {
                clazz("users-stats")
                link(Uris.Users.stats(), Rels.SELF)
                // Link to the next page if available and to the last page
                if (usersStats.currentPage < usersStats.totalPages) {
                    link(
                        Uris.Users.stats(
                            usersStats.currentPage + 1,
                            usersStats.itemsPerPage
                        ),
                        Rels.NEXT
                    )
                    link(
                        Uris.Users.stats(usersStats.totalPages, usersStats.itemsPerPage),
                        Rels.LAST
                    )
                }

                // Link to the previous page if available and to the first page
                if (usersStats.currentPage > FIRST_PAGE) {
                    link(
                        Uris.Users.stats(
                            usersStats.currentPage - 1,
                            usersStats.itemsPerPage
                        ),
                        Rels.PREV
                    )
                    link(Uris.Users.stats(FIRST_PAGE, usersStats.itemsPerPage), Rels.FIRST)
                }
                entity(userStats, Rels.USER_STATS) {
                    clazz("user-stats")
                    link(Uris.Users.byIdStats(authenticatedUser.user.id.value), Rels.SELF)
                }
            }

            entity(SystemInfoOutputModel.serializeFrom(systemInfo), Rels.SYSTEM) {
                clazz("system-info")
                link(Uris.System.getSystemInfo(), Rels.SELF)
            }
            action(
                name = "find-game",
                href = Uris.Games.findGame(),
                method = HttpMethod.POST,
                type = MediaType.APPLICATION_JSON_VALUE
            ) {
                clazz("find-game")
                requireAuth()
            }
            action(
                name = "logout",
                href = Uris.Users.logout(),
                method = HttpMethod.POST,
                type = MediaType.APPLICATION_JSON_VALUE
            ) {
                clazz("logout")
                requireAuth()
            }
        }

    /**
     * Output model for the user using media type application/vnd.siren+json
     * implemented by [SirenModel].
     *
     * @param user The user to be serialized.
     * @return A [SirenModel] containing the [User] representation.
     */
    fun userById(user: User): SirenModel<User> =
        siren(user) {
            clazz("user")
            link(Uris.Users.byId(user.id.value), Rels.SELF)
        }

    /**
     * Output model for the user stats using media type application/vnd.siren+json
     * implemented by [SirenModel].
     *
     * @param paginatedResult The paginated result of the user stats.
     * @return A [SirenModel] containing the [paginatedResult] representation.
     */
    fun usersStats(
        paginatedResult: PaginatedResult<UserStatsInfo>
    ): SirenModel<PaginatedResult<UserStatsInfo>> = siren(paginatedResult) {
        clazz("users-stats")
        link(
            Uris.Users.stats(paginatedResult.currentPage, paginatedResult.itemsPerPage),
            Rels.SELF
        )
        // Link to the next page if available and to the last page
        if (paginatedResult.currentPage < paginatedResult.totalPages) {
            link(
                Uris.Users.stats(
                    paginatedResult.currentPage + 1,
                    paginatedResult.itemsPerPage
                ),
                Rels.NEXT
            )
            link(
                Uris.Users.stats(paginatedResult.totalPages - 1, paginatedResult.itemsPerPage),
                Rels.LAST
            )
        }

        // Link to the previous page if available and to the first page
        if (paginatedResult.currentPage > FIRST_PAGE) {
            link(
                Uris.Users.stats(
                    paginatedResult.currentPage - 1,
                    paginatedResult.itemsPerPage
                ),
                Rels.PREV
            )
            link(Uris.Users.stats(FIRST_PAGE, paginatedResult.itemsPerPage), Rels.FIRST)
        }
    }

    /**
     * Output model for the user stats by term using media type application/vnd.siren+json
     * implemented by [SirenModel].
     *
     * @param paginatedResult The paginated result of the user stats.
     * @param termResult The term used to search the user stats.
     * @return A [SirenModel] containing the [paginatedResult] representation.
     */
    fun usersStatsByTerm(
        paginatedResult: PaginatedResult<UserStatsInfo>,
        termResult: Term
    ): SirenModel<PaginatedResult<UserStatsInfo>> =
        siren(paginatedResult) {
            clazz("users-stats")
            requireAuth()
            link(
                Uris.Users.statsByTerm(
                    termResult.value,
                    paginatedResult.currentPage,
                    paginatedResult.itemsPerPage
                ),
                Rels.SELF
            )
            // Link to the next page if available and to the last page
            if (paginatedResult.currentPage < paginatedResult.totalPages) {
                link(
                    Uris.Users.statsByTerm(
                        termResult.value,
                        paginatedResult.currentPage + 1,
                        paginatedResult.itemsPerPage
                    ),
                    Rels.NEXT
                )
                link(
                    Uris.Users.statsByTerm(
                        termResult.value,
                        paginatedResult.totalPages - 1,
                        paginatedResult.itemsPerPage
                    ),
                    Rels.LAST
                )
            }
            // Link to the previous page if available and to the first page
            if (paginatedResult.currentPage > FIRST_PAGE) {
                link(
                    Uris.Users.statsByTerm(
                        termResult.value,
                        paginatedResult.currentPage - 1,
                        paginatedResult.itemsPerPage
                    ),
                    Rels.PREV
                )
                link(
                    Uris.Users.statsByTerm(
                        termResult.value,
                        FIRST_PAGE,
                        paginatedResult.itemsPerPage
                    ),
                    Rels.FIRST
                )
            }
        }
}
