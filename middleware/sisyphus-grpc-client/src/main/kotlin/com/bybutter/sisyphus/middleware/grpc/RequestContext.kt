package com.bybutter.sisyphus.middleware.grpc

import io.grpc.Context
import io.grpc.Metadata

abstract class RequestContextProvider<T> {
    abstract val metaKey: Metadata.Key<T>
    abstract val key: RequestContext<T>
}

interface MutableRequestContextMerger<T> {
    fun merge(old: T, new: T)
}

open class RequestContext<T>(name: String) {
    companion object {
        const val REQUEST_CONTEXT_PREFIX = "r-x-"
    }

    val key: Context.Key<T> = Context.key("$REQUEST_CONTEXT_PREFIX$name")

    fun get(): T? {
        return key.get()
    }

    override fun toString(): String {
        return key.toString()
    }
}

class MutableRequestContext<T>(name: String, private val merger: MutableRequestContextMerger<T>) :
    RequestContext<T>(name) {
    var changed: Boolean = false
        private set

    fun set(value: T) {
        merger.merge(key.get(), value)
        changed = true
    }
}
