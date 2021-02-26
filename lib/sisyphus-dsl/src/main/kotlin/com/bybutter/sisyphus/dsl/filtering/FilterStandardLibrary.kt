package com.bybutter.sisyphus.dsl.filtering

import com.bybutter.sisyphus.protobuf.CustomProtoType
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoEnum
import com.bybutter.sisyphus.protobuf.primitives.BoolValue
import com.bybutter.sisyphus.protobuf.primitives.BytesValue
import com.bybutter.sisyphus.protobuf.primitives.DoubleValue
import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.FloatValue
import com.bybutter.sisyphus.protobuf.primitives.Int32Value
import com.bybutter.sisyphus.protobuf.primitives.Int64Value
import com.bybutter.sisyphus.protobuf.primitives.ListValue
import com.bybutter.sisyphus.protobuf.primitives.NullValue
import com.bybutter.sisyphus.protobuf.primitives.StringValue
import com.bybutter.sisyphus.protobuf.primitives.Struct
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.UInt32Value
import com.bybutter.sisyphus.protobuf.primitives.UInt64Value
import com.bybutter.sisyphus.protobuf.primitives.Value
import com.bybutter.sisyphus.protobuf.primitives.compareTo

open class FilterStandardLibrary {
    open fun and(left: Boolean, right: Boolean): Boolean {
        return left && right
    }

    open fun or(left: Boolean, right: Boolean): Boolean {
        return left || right
    }

    open fun union(left: Boolean, right: Boolean): Boolean {
        return left && right
    }

    open fun not(value: Boolean): Boolean {
        return !value
    }

    open fun compare(left: Long, right: Long): Int {
        return left.compareTo(right)
    }

    open fun compare(left: ULong, right: ULong): Int {
        return left.compareTo(right)
    }

    open fun compare(left: Double, right: Double): Int {
        return left.compareTo(right)
    }

    open fun compare(left: String, right: String): Int {
        return left.compareTo(right)
    }

    open fun compare(left: Duration, right: Duration): Int {
        return left.compareTo(right)
    }

    open fun compare(left: Timestamp, right: Timestamp): Int {
        return left.compareTo(right)
    }

    open fun equals(left: Any?, right: Any?): Boolean {
        return left == right
    }

    open fun notEquals(left: Any?, right: Any?): Boolean {
        return left != right
    }

    open fun has(left: Any?, right: Any?): Any {
        if (right == "*" && left != null) return true
        return when (left) {
            null -> false
            is Map<*, *> -> left.containsKey(right)
            is List<*> -> left.contains(right)
            is Message<*, *> -> if (right is Number) {
                left.has(right.toInt())
            } else {
                left.has(right.toString())
            }
            else -> left == right
        }
    }

    open fun access(left: Map<*, *>?, right: Any?): Any? {
        return left?.get(right).protobufConversion()
    }

    open fun access(left: Message<*, *>?, right: String): Any? {
        return left?.get<Any?>(right).protobufConversion()
    }

    private fun Any?.protobufConversion(): Any? {
        return when (this) {
            is Int -> toLong()
            is UInt -> toULong()
            is Float -> toDouble()
            is ListValue -> this.values.map { it.protobufConversion() }
            is DoubleValue -> this.value
            is FloatValue -> this.value.toDouble()
            is Int64Value -> this.value
            is UInt64Value -> this.value
            is Int32Value -> this.value.toLong()
            is UInt32Value -> this.value.toULong()
            is BoolValue -> this.value
            is StringValue -> this.value
            is BytesValue -> this.value
            is NullValue -> null
            is Struct -> this.fields.mapValues { it.value.protobufConversion() }
            is Value -> when (val kind = this.kind) {
                is Value.Kind.BoolValue -> kind.value
                is Value.Kind.ListValue -> kind.value.protobufConversion()
                is Value.Kind.NullValue -> null
                is Value.Kind.NumberValue -> kind.value
                is Value.Kind.StringValue -> kind.value
                is Value.Kind.StructValue -> kind.value
                null -> null
                else -> throw IllegalStateException("Illegal proto value type '${kind.javaClass}'.")
            }
            is ProtoEnum -> this.proto
            is List<*> -> this.map { it.protobufConversion() }
            is Map<*, *> -> this.mapValues { it.value.protobufConversion() }
            is CustomProtoType<*> -> this.value().protobufConversion()
            null -> null
            is Long, is ULong, is Double, is Boolean, is ByteArray, is String, is Message<*, *> -> this
            else -> throw IllegalStateException("Illegal proto data type '${this.javaClass}'.")
        }
    }
}
