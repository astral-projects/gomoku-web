package gomoku.repository.jdbi

import gomoku.repository.jdbi.mappers.BoardMapper
import gomoku.repository.jdbi.mappers.GameStateMapper
import gomoku.repository.jdbi.mappers.InstantMapper
import gomoku.repository.jdbi.mappers.PasswordValidationInfoMapper
import gomoku.repository.jdbi.mappers.TokenValidationInfoMapper
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin

fun Jdbi.configureWithAppRequirements(): Jdbi {
    installPlugin(KotlinPlugin())
    installPlugin(PostgresPlugin())

    registerColumnMapper(PasswordValidationInfoMapper())
    registerColumnMapper(TokenValidationInfoMapper())
    registerColumnMapper(InstantMapper())
    registerColumnMapper(BoardMapper())
    registerColumnMapper(GameStateMapper())

    return this
}
