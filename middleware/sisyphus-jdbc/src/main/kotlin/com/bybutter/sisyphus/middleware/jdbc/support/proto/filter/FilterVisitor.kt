package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import com.bybutter.sisyphus.dsl.filtering.FilterDsl
import com.bybutter.sisyphus.dsl.filtering.grammar.FilterParser
import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.invoke
import com.bybutter.sisyphus.string.unescape

open class FilterVisitor {
    fun build(builder: SqlBuilder<*>, filter: String): List<Any?> {
        return visit(builder, FilterDsl.parse(filter))
    }

    fun visit(builder: SqlBuilder<*>, filter: FilterParser.FilterContext): List<Any?> {
        return filter.e.map {
            visit(builder, it)
        }
    }

    protected open fun visit(builder: SqlBuilder<*>, expr: FilterParser.ExpressionContext): Any? {
        return expr.e.fold(visit(builder, expr.init)) { cond, seq ->
            builder.runtime.invoke("and", listOf(cond, visit(builder, seq)))
        }
    }

    protected open fun visit(builder: SqlBuilder<*>, factor: FilterParser.FactorContext): Any? {
        return factor.e.fold(visit(builder, factor.init)) { cond, e ->
            builder.runtime.invoke("or", listOf(cond, visit(builder, e)))
        }
    }

    protected open fun visit(builder: SqlBuilder<*>, condition: FilterParser.ConditionContext): Any? {
        return when (condition) {
            is FilterParser.NotConditionContext -> {
                builder.runtime.invoke("not", listOf(visit(builder, condition.expression())))
            }
            is FilterParser.CompareConditionContext -> {
                val field = visit(builder, condition.left)
                val value = builder.value(condition.left, visit(builder, condition.right))
                val op = condition.comparator().text
                return when (op) {
                    "<=" -> builder.runtime.invoke("lessOrEquals", listOf(field, value))
                    "<" -> builder.runtime.invoke("lessThan", listOf(field, value))
                    ">=" -> builder.runtime.invoke("greaterOrEqual", listOf(field, value))
                    ">" -> builder.runtime.invoke("greaterThan", listOf(field, value))
                    "=" -> builder.runtime.invoke("equals", listOf(field, value))
                    "!=" -> builder.runtime.invoke("notEquals", listOf(field, value))
                    ":" -> builder.runtime.invoke("has", listOf(field, value))
                    else -> TODO()
                }
            }
            is FilterParser.FunConditionContext -> visit(builder, condition.function())
            else -> throw IllegalStateException()
        }
    }

    protected open fun visit(builder: SqlBuilder<*>, member: FilterParser.MemberContext): Any? {
        return builder.member(member)
    }

    protected open fun visit(builder: SqlBuilder<*>, value: FilterParser.ValueContext): Any? {
        value.member()?.let { return visit(builder, it) }
        value.literal()?.let { return visit(builder, it) }
        value.function()?.let { return visit(builder, it) }
        return null
    }

    protected open fun visit(builder: SqlBuilder<*>, function: FilterParser.FunctionContext): Any? {
        return builder.runtime.invoke(function.name.text, visit(builder, function.argList()))
    }

    protected open fun visit(builder: SqlBuilder<*>, argList: FilterParser.ArgListContext?): List<Any?> {
        return argList?.args?.map { visit(builder, it) } ?: listOf()
    }

    protected open fun visit(builder: SqlBuilder<*>, literal: FilterParser.LiteralContext): Any? {
        return when (literal) {
            is FilterParser.IntContext -> literal.text.toLong()
            is FilterParser.UintContext -> literal.text.substring(0, literal.text.length - 1).toULong()
            is FilterParser.DoubleContext -> literal.text.toDouble()
            is FilterParser.StringContext -> visit(builder, literal)
            is FilterParser.BoolTrueContext -> true
            is FilterParser.BoolFalseContext -> false
            is FilterParser.NullContext -> null
            is FilterParser.DurationContext -> Duration(literal.text)
            is FilterParser.TimestampContext -> Timestamp(literal.text)
            else -> throw UnsupportedOperationException("Unsupported literal expression '${literal.text}'.")
        }
    }

    protected open fun visit(builder: SqlBuilder<*>, value: FilterParser.StringContext): String {
        val string = value.text
        return when {
            string.startsWith("\"") -> string.substring(1, string.length - 1)
            string.startsWith("'") -> string.substring(1, string.length - 1)
            else -> throw java.lang.IllegalStateException("Wrong string token '${value.text}'.")
        }.unescape()
    }

    companion object {
        val DEFAULT = FilterVisitor()
    }
}
