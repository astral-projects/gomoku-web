package gomoku.http.model.game

import gomoku.domain.game.SystemInfo
import gomoku.http.model.JsonOutputModel

data class SystemInfoOutputModel(
    val gameName: String,
    val authors: List<AuthorOutputModel>,
    val version: String,
    val releaseDate: String
) {
    companion object : JsonOutputModel<SystemInfo, SystemInfoOutputModel> {
        override fun serializeFrom(domainClass: SystemInfo): SystemInfoOutputModel {
            return SystemInfoOutputModel(
                gameName = domainClass.GAME_NAME,
                authors = domainClass.authors.map { AuthorOutputModel.serializeFrom(it) },
                version = domainClass.VERSION,
                releaseDate = domainClass.releaseDate
            )
        }
    }
}
