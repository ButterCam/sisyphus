package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.toLocalDateTime
import com.bybutter.sisyphus.protobuf.primitives.toTime
import com.bybutter.sisyphus.protobuf.primitives.unaryMinus
import org.jooq.Condition
import org.jooq.Field
import java.util.concurrent.TimeUnit

open class FilterStandardLibrary {
    fun and(left: SqlFilterPart, right: SqlFilterPart): SqlFilterPart {
        return left.and(right)
    }

    fun or(left: SqlFilterPart, right: SqlFilterPart): SqlFilterPart {
        return left.or(right)
    }

    fun not(value: SqlFilterPart): SqlFilterPart {
        return value.not()
    }

    fun and(left: SqlFilterPart, right: Condition): SqlFilterPart {
        return left.and(right.filterPart())
    }

    fun or(left: SqlFilterPart, right: Condition): SqlFilterPart {
        return left.or(right.filterPart())
    }

    fun and(left: Condition, right: SqlFilterPart): SqlFilterPart {
        return left.filterPart().and(right)
    }

    fun or(left: Condition, right: SqlFilterPart): SqlFilterPart {
        return left.filterPart().or(right)
    }

    fun and(left: Condition, right: Condition): Condition {
        return left.and(right)
    }

    fun or(left: Condition, right: Condition): Condition {
        return left.or(right)
    }

    fun not(value: Condition): Condition {
        return value.not()
    }

    fun lessOrEquals(left: Field<*>, right: Any?): Condition {
        return (left as Field<Any>).le(databaseValueMapping(right))
    }

    fun lessOrEquals(left: FieldHandle, right: Any?): Condition {
        return lessOrEquals(left.field(), left.valueConverter().invoke(right) ?: right)
    }

    fun lessThan(left: Field<*>, right: Any?): Condition {
        return (left as Field<Any>).lt(databaseValueMapping(right))
    }

    fun lessThan(left: FieldHandle, right: Any?): Condition {
        return lessThan(left.field(), left.valueConverter().invoke(right) ?: right)
    }

    fun greaterOrEqual(left: Field<*>, right: Any?): Condition {
        return (left as Field<Any>).ge(databaseValueMapping(right))
    }

    fun greaterOrEqual(left: FieldHandle, right: Any?): Condition {
        return greaterOrEqual(left.field(), left.valueConverter().invoke(right) ?: right)
    }

    fun greaterThan(left: Field<*>, right: Any?): Condition {
        return (left as Field<Any>).gt(databaseValueMapping(right))
    }

    fun greaterThan(left: FieldHandle, right: Any?): Condition {
        return greaterThan(left.field(), left.valueConverter().invoke(right) ?: right)
    }

    fun equals(left: Field<*>, right: Any?): Condition {
        right ?: return left.isNull
        return (left as Field<Any>).eq(databaseValueMapping(right))
    }

    fun equals(left: FieldHandle, right: Any?): Condition {
        return equals(left.field(), left.valueConverter().invoke(right) ?: right)
    }

    fun notEquals(left: Field<*>, right: Any?): Condition {
        right ?: return left.isNotNull
        return (left as Field<Any>).notEqual(databaseValueMapping(right))
    }

    fun notEquals(left: FieldHandle, right: Any?): Condition {
        return notEquals(left.field(), left.valueConverter().invoke(right) ?: right)
    }

    fun has(left: Field<*>, right: Any?): Condition {
        val mapped = databaseValueMapping(right)
        if (mapped == "*") return left.isNotNull
        return when (mapped) {
            "*" -> left.isNotNull
            null -> left.isNull
            is String -> {
                if (mapped.contains('*')) {
                    left.like(mapped.replace('*', '%'))
                } else {
                    equals(left, mapped)
                }
            }

            else -> equals(left, mapped)
        }
    }

    fun has(left: FieldHandle, right: Any): Condition {
        return has(left.field(), left.valueConverter().invoke(right) ?: right)
    }

    fun unaryMinus(condition: Condition): Condition {
        return condition.not()
    }

    fun unaryMinus(value: Long): Long {
        return -value
    }

    fun unaryMinus(value: Double): Double {
        return -value
    }

    fun unaryMinus(duration: Duration): Duration {
        return -duration
    }

    private fun databaseValueMapping(value: Any?): Any? {
        return when (value) {
            is Timestamp -> value.toLocalDateTime()
            is Duration -> value.toTime(TimeUnit.MILLISECONDS)
            else -> value
        }
    }
}
