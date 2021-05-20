package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.spi.ServiceLoader

interface ProtobufBooster {
    val order: Int
        get() = 0

    operator fun invoke()

    companion object {
        private var initialized = false

        fun boost() {
            if (initialized) return
            synchronized(this) {
                if (initialized) return
                val booster = ServiceLoader.load(ProtobufBooster::class.java)
                booster.sortedBy { it.order }.forEach {
                    it()
                }
                initialized = true
            }
        }
    }
}
