package com.bybutter.sisyphus.rpc

import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

fun <T> StreamObserver<T>.asSender(): Sender<T> {
    return StreamObserverAsSender(this)
}

private class StreamObserverAsSender<T>(private val observer: StreamObserver<T>) : Sender<T> {
    override fun send(element: T) {
        observer.onNext(element)
    }

    override fun close(cause: Throwable?) {
        if (cause != null) {
            observer.onError(cause)
        } else {
            observer.onCompleted()
        }
    }
}

fun <T> CompletableDeferred<T>.asStreamObserver(): StreamObserver<T> {
    return DeferredAsStreamObserver(this)
}

private class DeferredAsStreamObserver<T>(
    private val deferred: CompletableDeferred<T>
) : StreamObserver<T> {
    override fun onNext(value: T) {
        deferred.complete(value)
    }

    override fun onError(t: Throwable) {
        deferred.completeExceptionally(t)
    }

    override fun onCompleted() {}
}

fun <T> Channel<T>.asStreamObserver(): StreamObserver<T> {
    return StreamObserverWithChannel(this)
}

private class StreamObserverWithChannel<T>(
    private val channel: Channel<T>
) : StreamObserver<T> {
    override fun onNext(value: T) {
        channel.offer(value)
    }

    override fun onError(exception: Throwable?) {
        channel.close(exception)
    }

    override fun onCompleted() {
        channel.close(null)
    }
}

fun <T> channelToStreamObserver(
    channel: ReceiveChannel<T>,
    observer: StreamObserver<T>
): Job = GlobalScope.launch {
    try {
        channel.consumeEach {
            observer.onNext(it)
        }
    } catch (e: Exception) {
        observer.onError(e)
        throw e
    }
    observer.onCompleted()
}
