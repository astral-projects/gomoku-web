package gomoku.repository.transaction

import gomoku.repository.GamesRepository
import gomoku.repository.UsersRepository

interface Transaction {

    val gamesRepository: GamesRepository
    val usersRepository: UsersRepository
    // other repository types

    fun rollback()
}
