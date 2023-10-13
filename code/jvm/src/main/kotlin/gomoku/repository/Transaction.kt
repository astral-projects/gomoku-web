package gomoku.repository

interface Transaction {

    val usersRepository: UsersRepository

    // other repository types
    fun rollback()
}
