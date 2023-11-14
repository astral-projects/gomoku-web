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
     * Sets the isolation level of the transaction.
     * @param level the isolation level to set.
     */
    fun setIsolationLevel(level: Int)

    /**
     * Rollbacks the transaction.
     */
    fun rollback()
}
