package gomoku.repository.transaction

interface TransactionManager {
    // add a repository to the transaction and return the result of the block
    fun <R> run(block: (Transaction) -> R): R
}
