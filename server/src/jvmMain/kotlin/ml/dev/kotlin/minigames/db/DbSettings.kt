package ml.dev.kotlin.minigames.db

import kotlinx.coroutines.CoroutineDispatcher
import ml.dev.kotlin.minigames.util.envVar
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager

object DbSettings {

    val db by lazy {
        Database.connect(
                url = envVar("JDBC_DATABASE_URL"),
                driver = envVar("JDBC_DRIVER"),
        )
    }

    val defaultLogger: SqlLogger? = null
}

fun <T> txn(
        transactionIsolation: Int = DbSettings.db.transactionManager.defaultIsolationLevel,
        repetitionAttempts: Int = DbSettings.db.transactionManager.defaultRepetitionAttempts,
        readOnly: Boolean = false,
        logger: SqlLogger? = DbSettings.defaultLogger,
        statement: Transaction.() -> T
): T = transaction(transactionIsolation, repetitionAttempts, readOnly, DbSettings.db) {
    logger?.let { addLogger(it) }
    statement()
}

suspend fun <T> suspendedTxn(
        context: CoroutineDispatcher? = null,
        transactionIsolation: Int = DbSettings.db.transactionManager.defaultIsolationLevel,
        logger: SqlLogger? = DbSettings.defaultLogger,
        statement: Transaction.() -> T
): T = newSuspendedTransaction(context, DbSettings.db, transactionIsolation) {
    logger?.let { addLogger(it) }
    statement()
}
