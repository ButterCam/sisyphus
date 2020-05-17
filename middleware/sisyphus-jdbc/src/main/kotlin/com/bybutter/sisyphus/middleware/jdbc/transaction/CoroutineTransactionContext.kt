package com.bybutter.sisyphus.middleware.jdbc.transaction

import java.sql.Connection
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.sql.DataSource
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.ThreadContextElement

class CoroutineTransactionContext(transactionActive: Boolean = true) :
    AbstractCoroutineContextElement(CoroutineTransactionContext),
    ThreadContextElement<CoroutineTransactionContext?>,
    TransactionContext {
    var transactionActive: Boolean = transactionActive
        private set

    private val savePointCounter: AtomicInteger = AtomicInteger()

    private val connectionHandle = ConcurrentHashMap<DataSource, ConnectionHandle>()

    fun getConnection(dataSource: DataSource): Connection {
        if (!transactionActive) return dataSource.connection

        return connectionHandle.getOrPut(dataSource) {
            val result = ConnectionHandle(dataSource.connection)
            result.autoCommit = false
            result
        }.refer()
    }

    fun close() {
        transactionActive = false
        for ((_, connection) in connectionHandle) {
            connection.close()
            if (!connection.isClosed) {
                throw IllegalStateException("Connection not released.")
            }
        }
        connectionHandle.clear()
    }

    override fun nest(): TransactionContext {
        return TransactionSavePointContext(this, connectionHandle.values.associateWith { it.setSavepoint("Savepoint${savePointCounter.incrementAndGet()}") })
    }

    override fun rollback() {
        transactionActive = false
        for ((_, connection) in connectionHandle) {
            connection.rollback()
        }
    }

    override fun commit() {
        transactionActive = false
        for ((_, connection) in connectionHandle) {
            connection.commit()
        }
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: CoroutineTransactionContext?) {
        threadContext.set(oldState)
    }

    override fun updateThreadContext(context: CoroutineContext): CoroutineTransactionContext? {
        return threadContext.get().also {
            threadContext.set(this)
        }
    }

    companion object : CoroutineContext.Key<CoroutineTransactionContext> {
        private val threadContext = ThreadLocal<CoroutineTransactionContext>()

        fun current(): CoroutineTransactionContext? {
            return threadContext.get()
        }
    }
}
