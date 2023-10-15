package gomoku.repository.transaction

interface TransactionManager {
    fun <R> run(block: (Transaction) -> R): R
}
