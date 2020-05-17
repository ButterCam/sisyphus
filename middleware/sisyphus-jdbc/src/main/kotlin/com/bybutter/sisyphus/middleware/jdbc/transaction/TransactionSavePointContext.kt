package com.bybutter.sisyphus.middleware.jdbc.transaction

import java.sql.Connection
import java.sql.Savepoint

class TransactionSavePointContext(private val context: CoroutineTransactionContext, private val savePoints: Map<out Connection, Savepoint>) : TransactionContext {
    override fun nest(): TransactionContext {
        return context.nest()
    }

    override fun rollback() {
        for ((connection, savePoint) in savePoints) {
            connection.rollback(savePoint)
        }
    }

    override fun commit() {
        for ((connection, savePoint) in savePoints) {
            connection.releaseSavepoint(savePoint)
        }
    }
}
