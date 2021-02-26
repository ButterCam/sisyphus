package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import com.bybutter.sisyphus.collection.unaryPlus
import com.bybutter.sisyphus.dsl.filtering.FilterEngine
import com.bybutter.sisyphus.dsl.filtering.FilterRuntime
import com.bybutter.sisyphus.dsl.filtering.FilterStandardLibrary
import com.bybutter.sisyphus.dsl.filtering.grammar.FilterParser
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.toSql
import org.jooq.Condition
import org.jooq.Field

abstract class JooqConditionBuilder() {
    private val engine = FilterEngine(runtime = JooqFilterRuntime(this))

    fun build(filter: String): Condition? {
        return engine.eval(filter) as Condition?
    }

    abstract fun resolveMember(member: String): Field<*>?

    open fun resolveValue(field: Field<*>?, value: Any?): Any? {
        return when (value) {
            is Timestamp -> value.toSql()
            else -> value
        }
    }

    private class JooqFilterRuntime(val builder: JooqConditionBuilder) : FilterRuntime(JooqFilterLibrary()) {
        override fun access(member: FilterParser.MemberContext, global: Map<String, Any?>): Any? {
            return builder.resolveMember(member.text)
        }

        override fun invokeOrDefault(function: String, arguments: List<Any?>, block: () -> Any?): Any? {
            val args = +arguments

            when (function) {
                "lessOrEquals", "lessThan", "greaterOrEqual", "greaterThan", "equals", "notEquals" -> {
                    if (arguments[0] is Field<*>) {
                        args[1] = builder.resolveValue(arguments[0] as Field<*>, args[1])
                    }
                }
            }
            return super.invokeOrDefault(function, arguments, block)
        }
    }
}

class JooqFilterLibrary : FilterStandardLibrary() {
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

    override fun has(left: Any?, right: Any?): Any {
        if (left is Field<*>) {
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
        return super.has(left, right)
    }
}
