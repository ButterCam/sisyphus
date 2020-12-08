package com.bybutter.sisyphus.rpc

import io.grpc.Context
import io.grpc.Metadata

object Trailers {
    val CUSTOM_TAILS_KEY: Context.Key<Metadata> = Context.key("sisyphus-custom-tails")

    fun initTrailers(context: Context, init: Metadata.() -> Unit = {}): Context {
        if (CUSTOM_TAILS_KEY.get() != null) return context
        return context.withValue(CUSTOM_TAILS_KEY, Metadata().apply(init))
    }
}

fun <T> trailers(key: Metadata.Key<T>, value: T) {
    Trailers.CUSTOM_TAILS_KEY.get()?.put(key, value)
}

fun trailers(key: String, value: String) {
    Trailers.CUSTOM_TAILS_KEY.get()?.put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), value)
}

fun <T> trailers(key: Metadata.Key<T>): T? {
    return Trailers.CUSTOM_TAILS_KEY.get()?.get(key)
}
