package com.bybutter.sisyphus.api.filtering

import com.bybutter.sisyphus.protobuf.EnumSupport
import com.bybutter.sisyphus.protobuf.ProtoEnum
import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.invoke
import com.bybutter.sisyphus.protobuf.primitives.string
import kotlin.reflect.full.companionObjectInstance

object DynamicCaster {
    fun Any?.cast(target: Class<*>): Any? {
        if (target.isInstance(this)) return this

        return when (this) {
            is Number -> cast(this, target)
            is String -> cast(this, target)
            is Boolean -> cast(this, target)
            is Timestamp -> cast(this, target)
            is Duration -> cast(this, target)
            else -> null
        }
    }

    fun cast(value: Number, target: Class<*>): Any? {
        return when (target) {
            Int::class.java -> value.toInt()
            UInt::class.java -> value.toInt().toUInt()
            Long::class.java -> value.toLong()
            ULong::class.java -> value.toLong().toULong()
            Double::class.java -> value.toDouble()
            Float::class.java -> value.toFloat()
            String::class.java -> value.toString()
            Boolean::class.java -> value.toInt() != 0
            else -> null
        }
    }

    fun cast(value: Boolean, target: Class<*>): Any? {
        return when (target) {
            Int::class.java -> 1
            UInt::class.java -> 1U
            Long::class.java -> 1L
            ULong::class.java -> 1UL
            Double::class.java -> 1.0
            Float::class.java -> 1.0f
            String::class.java -> value.toString()
            else -> null
        }
    }

    fun cast(value: String, target: Class<*>): Any? {
        return when (target) {
            Int::class.java -> value.toIntOrNull()
            UInt::class.java -> value.toUIntOrNull()
            Long::class.java -> value.toLongOrNull()
            ULong::class.java -> value.toULongOrNull()
            Double::class.java -> value.toDoubleOrNull()
            Float::class.java -> value.toFloatOrNull()
            Boolean::class.java -> value.toBoolean()
            Number::class.java -> value.toDoubleOrNull()
            Timestamp::class.java -> Timestamp(value)
            Duration::class.java -> Duration(value)
            else -> {
                if (ProtoEnum::class.java.isAssignableFrom(target)) {
                    val support = target.kotlin.companionObjectInstance as? EnumSupport<*>
                            ?: return null
                    return support.fromProto(value)
                }
                return null
            }
        }
    }

    fun cast(value: Timestamp, target: Class<*>): Any? {
        return when (target) {
            String::class.java -> value.string()
            else -> return null
        }
    }

    fun cast(value: Duration, target: Class<*>): Any? {
        return when (target) {
            String::class.java -> value.string()
            else -> return null
        }
    }
}
