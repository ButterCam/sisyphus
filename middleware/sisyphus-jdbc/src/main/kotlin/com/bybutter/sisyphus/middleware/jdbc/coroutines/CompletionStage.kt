package com.bybutter.sisyphus.middleware.jdbc.coroutines

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import java.util.concurrent.CompletionStage

suspend fun <T> CompletionStage<T>.await(): T {
    return this.asDeferred().await()
}

fun <T> CompletionStage<T>.asDeferred(): Deferred<T> {
    val deferred = CompletableDeferred<T>()

    thenAccept {
        deferred.complete(it)
    }.exceptionally {
        deferred.completeExceptionally(it)
        null
    }

    return deferred
}
