package gomoku.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.transaction.LocalTransactionHandler
import org.jdbi.v3.core.transaction.SerializableTransactionRunner
import org.jdbi.v3.core.transaction.TransactionIsolationLevel
import org.postgresql.ds.PGSimpleDataSource

// Constants
private const val DB_URL = "jdbc:postgresql://localhost:5432/db?user=dbuser&password=changeit"

/**
 * Provides JDBI test configurations and utility functions.
 */
object JdbiTestConfiguration {

    /**
     * Runs the given block with a handle and a transaction with the given isolation level.
     * @param isolationLevel the isolation level of the transaction.
     * Defaults to [TransactionIsolationLevel.READ_COMMITTED] since it is the default in PGSQL.
     * @param block the block to be executed.
     */
    fun runWithHandle(
        isolationLevel: TransactionIsolationLevel = TransactionIsolationLevel.READ_COMMITTED,
        block: (Handle) -> Unit
    ) {
        if (isolationLevel == TransactionIsolationLevel.SERIALIZABLE) {
            jdbi.setTransactionHandler(SerializableTransactionRunner())
        } else {
            jdbi.setTransactionHandler(LocalTransactionHandler())
        }
        jdbi.useTransaction<Exception>(isolationLevel, block)
    }

    /**
     * Runs the given block with a handle and a transaction with the given isolation level,
     * and rolls back the transaction after the block is executed.
     * @param isolationLevel the isolation level of the transaction. Defaults to
     * [TransactionIsolationLevel.READ_COMMITTED] since it is the default in PGSQL.
     * @param block the block to be executed.
     * @see runWithHandle
     */
    fun runWithHandleAndRollback(
        isolationLevel: TransactionIsolationLevel = TransactionIsolationLevel.READ_COMMITTED,
        block: (Handle) -> Unit
    ) = runWithHandle(isolationLevel) { handle ->
        try {
            block(handle)
        } finally {
            handle.rollback()
        }
    }

    val jdbi = Jdbi.create(
        PGSimpleDataSource().apply {
            setURL(DB_URL)
        }
    ).configureWithAppRequirements()
}
