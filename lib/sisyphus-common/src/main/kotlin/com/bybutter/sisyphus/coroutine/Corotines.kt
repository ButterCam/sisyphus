package com.bybutter.sisyphus.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Run task in [Dispatchers.IO] scope.
 */
suspend fun <T> io(block: suspend CoroutineScope.() -> T): T {
    return withContext(Dispatchers.IO, block)
}
