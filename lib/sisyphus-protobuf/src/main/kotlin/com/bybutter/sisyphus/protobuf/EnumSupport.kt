package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.protobuf.primitives.EnumDescriptorProto
import com.bybutter.sisyphus.reflect.getTypeArgument
import kotlin.reflect.KClass

abstract class EnumSupport<T : ProtoEnum<T>> :
    ProtoSupport<EnumDescriptorProto> {
    val enumClass: KClass<T> by lazy {
        (this.javaClass.getTypeArgument(EnumSupport::class.java, 0) as Class<T>).kotlin
    }

    abstract fun values(): Array<T>

    private val numberMap by lazy {
        values().associateBy {
            it.number
        }
    }

    private val nameMap by lazy {
        values().associateBy {
            it.proto
        }
    }

    fun fromNumber(value: Int): T? {
        return numberMap[value]
    }

    fun fromProto(value: String): T? {
        return nameMap[value]
    }

    operator fun invoke(): T {
        return values().first()
    }

    operator fun invoke(value: Int): T {
        return fromNumber(value) ?: invoke()
    }

    operator fun invoke(value: String): T {
        return fromProto(value) ?: invoke()
    }
}
