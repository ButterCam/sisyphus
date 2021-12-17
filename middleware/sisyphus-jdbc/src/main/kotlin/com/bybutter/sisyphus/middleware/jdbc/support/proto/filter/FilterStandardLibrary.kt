package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import org.jooq.Condition
import org.jooq.Field

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

    fun lessOrEquals(left: Field<*>, right: Any): Condition {
        return (left as Field<Any>).le(right)
    }

    fun lessThan(left: Field<*>, right: Any): Condition {
        return (left as Field<Any>).lt(right)
    }

    fun greaterOrEqual(left: Field<*>, right: Any): Condition {
        return (left as Field<Any>).ge(right)
    }

    fun greaterThan(left: Field<*>, right: Any): Condition {
        return (left as Field<Any>).gt(right)
    }

    fun equals(left: Field<*>, right: Any?): Condition {
        right ?: return left.isNull
        return (left as Field<Any>).eq(right)
    }

    fun notEquals(left: Field<*>, right: Any?): Condition {
        right ?: return left.isNotNull
        return (left as Field<Any>).notEqual(right)
    }

    fun has(left: Field<*>, right: Any?): Condition {
        if (right == "*") return left.isNotNull
        return when (right) {
            "*" -> left.isNotNull
            null -> left.isNull
            is String -> {
                if (right.contains('*')) {
                    left.like(right.replace('*', '%'))
                } else {
                    equals(left, right)
                }
            }
            else -> equals(left, right)
        }
    }
}
