package gomoku.repository.transaction

import gomoku.repository.GamesRepository
import gomoku.repository.UsersRepository

/**
 * Represents a transaction.
 */
interface Transaction {

    val gamesRepository: GamesRepository
    val usersRepository: UsersRepository
    // other repository types

    /**
     * Rollbacks the transaction.
     */
    fun rollback()
}
