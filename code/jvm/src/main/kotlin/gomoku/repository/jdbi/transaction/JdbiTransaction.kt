package gomoku.repository.jdbi.transaction

import gomoku.repository.GamesRepository
import gomoku.repository.UsersRepository
import gomoku.repository.jdbi.JdbiGameRepository
import gomoku.repository.jdbi.JdbiUsersRepository
import gomoku.repository.transaction.Transaction
import org.jdbi.v3.core.Handle

class JdbiTransaction(
    private val handle: Handle
) : Transaction {
    override val usersRepository: UsersRepository = JdbiUsersRepository(handle)
    override val gamesRepository: GamesRepository = JdbiGameRepository(handle)

    override fun setIsolationLevel(level: Int) {
        handle.setTransactionIsolationLevel(level)
    }

    override fun rollback() {
        handle.rollback()
    }
}
