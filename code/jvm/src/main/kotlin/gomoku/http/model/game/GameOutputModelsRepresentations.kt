package gomoku.http.model.game

import gomoku.domain.components.Id
import gomoku.domain.game.Game
import gomoku.domain.game.variant.GameVariant
import gomoku.domain.user.User
import gomoku.http.Rels
import gomoku.http.Uris
import gomoku.http.media.siren.SirenModel
import gomoku.http.media.siren.siren
import gomoku.http.model.user.UserOutputModel
import gomoku.services.game.FindGameSuccess
import org.springframework.http.HttpMethod

class GameOutputModelsRepresentations {

    /**
     * Output model representation for a game lobby using [SirenModel].
     * This is used to represent a game lobby in the game lobby list.
     *
     * @param gameCreation The game lobby to be represented.
     */
    fun findGameLobbyCreated(
        gameCreation: FindGameSuccess.LobbyCreated
    ): SirenModel<FindGameSuccess.LobbyCreated> {
        return siren(gameCreation) {
            clazz("lobby")
            requireAuth()
            link(Uris.Lobby.getLobby(gameCreation.id), Rels.SELF)
            action(
                name = "Exit Lobby",
                href = Uris.Lobby.exitLobby(gameCreation.id),
                method = HttpMethod.DELETE,
                type = "application/json"
            ) {
                clazz("exit-lobby")
                requireAuth()
            }
        }
    }

    /**
     * Output model representation for a game match using [SirenModel].
     * This is used to represent a game match in the game lobby.
     *
     * @param gameMatch The game match to be represented.
     */
    fun findGameMatch(
        gameMatch: FindGameSuccess.GameMatch
    ): SirenModel<FindGameSuccess.GameMatch> {
        return siren(gameMatch) {
            clazz("game")
            requireAuth()
            link(Uris.Games.byId(gameMatch.id), Rels.SELF)
            action(
                name = "Make Move",
                href = Uris.Games.makeMove(gameMatch.id),
                method = HttpMethod.POST,
                type = "application/json"
            ) {
                clazz("make-move")
                requireAuth()
                textField("col")
                numberField("row")
            }
            action(
                name = "Exit Game",
                href = Uris.Games.exitGame(gameMatch.id),
                method = HttpMethod.POST,
                type = "application/json"
            ) {
                clazz("exit-game")
                requireAuth()
            }
        }
    }

    /**
     * Output model representation for getting a game by id using [SirenModel].
     *
     * @param game The game to be represented.
     */
    fun gameById(
        game: Game,
        host: User,
        guest: User
    ): SirenModel<GameOutputModel> {
        return siren(GameOutputModel.serializeFrom(game)) {
            clazz("game")
            link(Uris.Games.byId(game.id.value), Rels.SELF)
            entity(UserOutputModel.serializeFrom(host), Rels.USER) {
                link(Uris.Users.byId(host.id.value), Rels.SELF)
            }
            entity(UserOutputModel.serializeFrom(guest), Rels.USER) {
                link(Uris.Users.byId(guest.id.value), Rels.SELF)
            }
            action(
                name = "Make Move",
                href = Uris.Games.makeMove(game.id.value),
                method = HttpMethod.POST,
                type = "application/json"
            ) {
                clazz("make-move")
                requireAuth()
                textField("col")
                numberField("row")
                requireAuth()
            }
            action(
                name = "Exit Game",
                href = Uris.Games.exitGame(game.id.value),
                method = HttpMethod.POST,
                type = "application/json"
            ) {
                clazz("exit-game")
                requireAuth()
            }
        }
    }

    /**
     * Output model representation when user leaves a game using [SirenModel].
     *
     * @param gameId The id of the game to be left.
     */
    fun exitGame(gameId: Int, userId: Int) =
        siren(GameExitOutputModel(userId, gameId)) {
            clazz("game")
            requireAuth()
            link(Uris.Games.exitGame(gameId), Rels.SELF)
        }


    fun makeMove(game: Game, gameId: Id) =
        siren(
            GameOutputModel.serializeFrom(game)
        ) {
            clazz("game")
            requireAuth()
            link(Uris.Games.makeMove(gameId.value), Rels.SELF)
        }

    fun variants(variants: List<GameVariant>) =
        siren(variants) {
            clazz("variants")
            link(Uris.Games.getVariants(), Rels.SELF)
        }
}
