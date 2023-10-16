package gomoku.http.model.game

data class SystemInfoOutputModel(
    val gameName: String,
    val authors: List<AuthorOutputModel>,
    val version: String,
    val releaseDate: String
)
