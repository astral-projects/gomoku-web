package gomoku.repository.transaction

/**
 * Abstracts the transaction management.
 */
interface TransactionManager {
    /**
     * Adds a repository to the transaction and returns the result of the block.
     */
    fun <R> run(block: (Transaction) -> R): R
}
