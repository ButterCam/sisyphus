package com.bybutter.sisyphus.rpc

interface Sender<in T> {
    fun send(element: T)

    fun close(cause: Throwable? = null)
}
