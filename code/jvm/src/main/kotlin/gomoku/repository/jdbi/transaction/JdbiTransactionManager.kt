package gomoku.repository.jdbi.transaction

import gomoku.repository.transaction.Transaction
import gomoku.repository.transaction.TransactionManager
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.transaction.SerializableTransactionRunner
import org.jdbi.v3.core.transaction.SerializableTransactionRunner.Configuration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class JdbiTransactionManager(
    private val jdbi: Jdbi,
) : TransactionManager {

    companion object {
        private const val MAX_RETRIES = 1
        private val logger: Logger = LoggerFactory.getLogger(JdbiTransactionManager::class.java)
    }

    init {
        jdbi.setTransactionHandler(SerializableTransactionRunner())
    }

    override fun <R> run(block: (Transaction) -> R): R =
        jdbi.inTransaction<R, Exception> { handle: Handle ->
            handle.configure(Configuration::class.java) { config: Configuration ->
                config
                    .setMaxRetries(MAX_RETRIES)
                    .setOnFailure { exceptions ->
                        logger.error("Transaction failed due to a serialization issue: {}", exceptions)
                    }
            }
            val transaction = JdbiTransaction(handle)
            block(transaction)
        }
}
