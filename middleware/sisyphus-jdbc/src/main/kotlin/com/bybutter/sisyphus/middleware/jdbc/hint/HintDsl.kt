package com.bybutter.sisyphus.middleware.jdbc.hint

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

suspend fun <T> hint(hint: ExecuteHint, block: suspend CoroutineScope.() -> T): T {
    val hintContext = coroutineContext[CoroutineExecuteHintContext]
        ?: CoroutineExecuteHintContext()
    hintContext.hint(hint)

    return try {
        if (coroutineContext[CoroutineExecuteHintContext] == null) {
            withContext(hintContext, block)
        } else {
            coroutineScope(block)
        }
    } finally {
        hintContext.popHint()
    }
}
