package gomoku.repository.jdbi

import org.jdbi.v3.core.Handle
import gomoku.repository.Transaction
import gomoku.repository.UsersRepository

class JdbiTransaction(
    private val handle: Handle
) : Transaction {

    override val usersRepository: UsersRepository = JdbiUsersRepository(handle)

    override fun rollback() {
        handle.rollback()
    }
}
