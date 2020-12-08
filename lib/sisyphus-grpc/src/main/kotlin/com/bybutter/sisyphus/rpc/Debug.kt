package com.bybutter.sisyphus.rpc

import com.bybutter.sisyphus.protobuf.Message
import io.grpc.Context

object Debug {
    val DEBUG_INFO_KEY: Context.Key<MutableList<Message<*, *>>> = Context.key("sisyphus-debug-context")

    fun initDebug(context: Context): Context {
        if (debugEnabled) return context
        return context.withValue(DEBUG_INFO_KEY, mutableListOf())
    }

    val debugEnabled: Boolean get() = DEBUG_INFO_KEY.get() != null

    val debugInfo: List<Message<*, *>> get() = DEBUG_INFO_KEY.get() ?: listOf()
}

fun debug(message: Message<*, *>) {
    Debug.DEBUG_INFO_KEY.get()?.add(message)
}

inline fun debug(block: () -> Message<*, *>) {
    Debug.DEBUG_INFO_KEY.get()?.let {
        it += block()
    }
}
