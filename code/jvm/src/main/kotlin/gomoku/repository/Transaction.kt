package gomoku.repository

interface Transaction {

    val gamesRepository: GamesRepository
    val usersRepository: UsersRepository
    // other repository types

    fun rollback()
}
