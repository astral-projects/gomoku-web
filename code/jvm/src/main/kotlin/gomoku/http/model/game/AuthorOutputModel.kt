package gomoku.http.model.game

import gomoku.domain.game.Author

data class AuthorOutputModel(
    val firstName: String,
    val lastName: String,
    val gitHubUrl: String
) {
    companion object {
        fun serializeFrom(domainClass: Author): AuthorOutputModel {
            return AuthorOutputModel(
                firstName = domainClass.firstName,
                lastName = domainClass.lastName,
                gitHubUrl = domainClass.gitHubUrl.toString()
            )
        }
    }
}
