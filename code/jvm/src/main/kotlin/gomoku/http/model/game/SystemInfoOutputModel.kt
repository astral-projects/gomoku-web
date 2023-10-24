package gomoku.http.model.game

import gomoku.domain.SystemInfo
import gomoku.http.model.JsonOutputModel

data class SystemInfoOutputModel(
    val gameName: String,
    val version: String,
    val description: String,
    val releaseDate: String,
    val authors: List<AuthorOutputModel>
) {
    companion object : JsonOutputModel<SystemInfo, SystemInfoOutputModel> {
        override fun serializeFrom(domainClass: SystemInfo): SystemInfoOutputModel {
            return SystemInfoOutputModel(
                gameName = domainClass.GAME_NAME,
                version = domainClass.VERSION,
                description = domainClass.DESCRIPTION,
                releaseDate = domainClass.releaseDate,
                authors = domainClass.authors.map { AuthorOutputModel.serializeFrom(it) },
            )
        }
    }
}
