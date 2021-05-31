package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import org.jooq.Condition
import org.jooq.Field

open class FilterStandardLibrary {
    fun and(left: Condition, right: Condition): Condition {
        return left.and(right)
    }

    fun or(left: Condition, right: Condition): Condition {
        return left.or(right)
    }

    fun union(left: Condition, right: Condition): Condition {
        return left.and(right)
    }

    fun not(value: Condition): Condition {
        return value.not()
    }

    fun and(left: JooqConditionSupplier, right: Condition): Condition {
        return left.get().and(right)
    }

    fun or(left: JooqConditionSupplier, right: Condition): Condition {
        return left.get().or(right)
    }

    fun union(left: JooqConditionSupplier, right: Condition): Condition {
        return left.get().and(right)
    }

    fun and(left: Condition, right: JooqConditionSupplier): Condition {
        return left.and(right.get())
    }

    fun or(left: Condition, right: JooqConditionSupplier): Condition {
        return left.or(right.get())
    }

    fun union(left: Condition, right: JooqConditionSupplier): Condition {
        return left.and(right.get())
    }

    fun and(left: JooqConditionSupplier, right: JooqConditionSupplier): Condition {
        return left.get().and(right.get())
    }

    fun or(left: JooqConditionSupplier, right: JooqConditionSupplier): Condition {
        return left.get().or(right.get())
    }

    fun union(left: JooqConditionSupplier, right: JooqConditionSupplier): Condition {
        return left.get().and(right.get())
    }

    fun not(value: JooqConditionSupplier): Condition {
        return value.get().not()
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

    fun has(left: Any?, right: Any?): Any {
        if (left !is Field<*>) throw TODO()
        if (right == "*") return left.isNotNull
        return when (right) {
            is String -> {
                if (right.contains('*')) {
                    left.like(right.replace('*', '%'))
                } else {
                    (left as Field<Any>).eq(right)
                }
            }
            null -> left.isNull
            else -> (left as Field<Any?>).eq(right)
        }
    }
}
