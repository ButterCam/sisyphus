package com.bybutter.sisyphus.middleware.jdbc.transaction

import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * Always open a new non-transaction transaction.
 */
suspend fun <T> noTransaction(block: suspend CoroutineScope.() -> T): T {
    val transactionContext = coroutineContext[CoroutineTransactionContext]

    return if (transactionContext?.transactionActive == true) {
        withContext(CoroutineTransactionContext(false), block)
    } else {
        coroutineScope(block)
    }
}

/**
 * Open a transaction scope, when it be called in a existed transaction scope it will inherit the scope.
 * When it be called in non transaction scope, it will create new scope.
 */
suspend fun <T> transaction(block: suspend CoroutineScope.() -> T): T {
    val transactionContext = coroutineContext[CoroutineTransactionContext]

    return if (transactionContext?.transactionActive == true) {
        coroutineScope(block)
    } else {
        newTransaction(block)
    }
}

/**
 * Open a nest transaction scope when it be called in a existed transaction scope, open a new transaction scope
 * when it be called in non transaction scope.
 */
suspend fun <T> nestTransaction(block: suspend CoroutineScope.() -> T): T {
    val transactionContext = coroutineContext[CoroutineTransactionContext]

    return if (transactionContext?.transactionActive == true) {
        val nestContext = transactionContext.nest()
        try {
            coroutineScope(block).also {
                nestContext.commit()
            }
        } catch (e: Exception) {
            nestContext.rollback()
            throw e
        }
    } else {
        newTransaction(block)
    }
}

/**
 * Always open a new transaction scope.
 */
suspend fun <T> newTransaction(block: suspend CoroutineScope.() -> T): T {
    val transactionContext = CoroutineTransactionContext()
    return withContext(transactionContext) {
        try {
            block().also {
                transactionContext.commit()
            }
        } catch (e: Exception) {
            transactionContext.rollback()
            throw e
        } finally {
            transactionContext.close()
        }
    }
}
