package gomoku.http.model.user

import gomoku.domain.PaginatedResult
import gomoku.domain.SystemInfo
import gomoku.domain.components.Id
import gomoku.domain.components.Term
import gomoku.domain.user.AuthenticatedUser
import gomoku.domain.user.User
import gomoku.domain.user.UserStatsInfo
import gomoku.http.Rels
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
            recipeLink(Uris.Users.REGISTER, Rels.REGISTER_RECIPE)
            recipeLink(Uris.Users.TOKEN, Rels.LOGIN_RECIPE)
            recipeLink(Uris.Users.LOGOUT, Rels.LOGOUT_RECIPE)
            recipeLink(Uris.Games.FIND_GAME, Rels.FIND_GAME_RECIPE)
            recipeLink(Uris.Games.GET_BY_ID, Rels.GAME_RECIPE)
            recipeLink(Uris.Games.GET_VARIANTS, Rels.GAME_VARIANTS_RECIPE)
            recipeLink(Uris.Lobby.GET_IS_IN_LOBBY, Rels.GET_IS_IN_LOBBY_RECIPE)
            recipeLink(Uris.Users.GET_BY_ID, Rels.USER_RECIPE)
            recipeLink(Uris.Users.STATS_BY_TERM, Rels.GET_USERS_STATS_RECIPE)
            recipeLink(Uris.Users.STATS_BY_ID, Rels.GET_USER_STATS_RECIPE)
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

            entity(SystemInfoOutputModel.serializeFrom(systemInfo), Rels.SYSTEM_INFO) {
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
                Uris.Users.stats(paginatedResult.totalPages, paginatedResult.itemsPerPage),
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
                        paginatedResult.totalPages,
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
