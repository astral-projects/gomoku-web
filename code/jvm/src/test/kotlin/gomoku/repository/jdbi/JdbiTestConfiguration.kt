package gomoku.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource

// Constants
private const val DB_URL = "jdbc:postgresql://localhost:5432/db?user=dbuser&password=changeit"

object JdbiTestConfiguration {

    fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

    val jdbi = Jdbi.create(
        PGSimpleDataSource().apply {
            setURL(DB_URL)
        }
    ).configureWithAppRequirements()
}
