package com.bybutter.sisyphus.middleware.jdbc.transaction

import java.lang.IllegalStateException
import java.sql.Connection
import java.util.concurrent.atomic.AtomicInteger

class ConnectionHandle(private val delegate: Connection) : Connection by delegate {
    private val referenceCounter: AtomicInteger = AtomicInteger(1)

    fun refer(): Connection {
        referenceCounter.incrementAndGet()
        return this
    }

    override fun close() {
        val referenceCount = referenceCounter.decrementAndGet()

        if (referenceCount < 0) {
            throw IllegalStateException()
        }

        if (referenceCount == 0) {
            delegate.close()
        }
    }
}
