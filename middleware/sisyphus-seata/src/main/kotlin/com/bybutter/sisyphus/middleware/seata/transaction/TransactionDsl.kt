package com.bybutter.sisyphus.middleware.seata.transaction

import io.seata.core.context.RootContext
import io.seata.tm.api.GlobalTransactionContext
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * seata supports coroutine
 */
suspend fun <T> seataTransaction(block: suspend CoroutineScope.() -> T): T {
    return if (coroutineContext[SeataCoroutineTransactionContext] != null) {
        coroutineScope(block)
    } else {
        val globalTransactionContext = GlobalTransactionContext.getCurrentOrCreate()
        try {
            globalTransactionContext.begin()
            withContext(SeataCoroutineTransactionContext()) {
                block()
            }.also {
                globalTransactionContext.commit()
                RootContext.unbind()
            }
        } catch (e: Exception) {
            globalTransactionContext.rollback()
            RootContext.unbind()
            throw e
        }
    }
}
