package gomoku.repository.jdbi

import gomoku.repository.GamesRepository
import gomoku.repository.Transaction
import gomoku.repository.UsersRepository
import org.jdbi.v3.core.Handle

class JdbiTransaction(
    private val handle: Handle
) : Transaction {

    override val usersRepository: UsersRepository = JdbiUsersRepository(handle)

    override val gamesRepository: GamesRepository = JdbiGamesRepository(handle)

    override fun rollback() {
        handle.rollback()
    }
}
