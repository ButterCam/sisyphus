package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.spi.ServiceLoader

interface ProtobufBooster {
    val order: Int
        get() = 0

    operator fun invoke(reflection: ProtoReflection)

    companion object {
        private val reflections: MutableSet<ProtoReflection> = mutableSetOf()

        fun boost(reflection: ProtoReflection) {
            if (reflections.contains(reflection)) return

            synchronized(this) {
                if (reflections.contains(reflection)) return

                val booster = ServiceLoader.load(ProtobufBooster::class.java)
                booster.sortedBy { it.order }.forEach {
                    it(reflection)
                }

                reflections += reflection
            }
        }
    }
}
