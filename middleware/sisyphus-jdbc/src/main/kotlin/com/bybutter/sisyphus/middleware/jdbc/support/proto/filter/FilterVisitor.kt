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
        return filter.expression().map {
            visit(builder, it)
        }
    }

    protected open fun visit(builder: SqlBuilder<*>, expr: FilterParser.ExpressionContext): Any? {
        return expr.factor().fold<FilterParser.FactorContext, Any?>(null) { a, b ->
            val right = visit(builder, b)
            a?.let {
                builder.runtime.invoke("and", listOf(a, right))
            } ?: right
        }
    }

    protected open fun visit(builder: SqlBuilder<*>, factor: FilterParser.FactorContext): Any? {
        return factor.term().fold<FilterParser.TermContext, Any?>(null) { a, b ->
            val right = visit(builder, b)
            a?.let {
                builder.runtime.invoke("or", listOf(a, right))
            } ?: right
        }
    }

    protected open fun visit(builder: SqlBuilder<*>, term: FilterParser.TermContext): Any? {
        val result = visit(builder, term.simple())

        if (term.NOT() != null) {
            return builder.runtime.invoke("not", listOf(result))
        }

        if (term.MINUS() != null) {
            return builder.runtime.invoke("unaryMinus", listOf(result))
        }

        return result
    }

    protected open fun visit(builder: SqlBuilder<*>, condition: FilterParser.SimpleContext): Any? {
        condition.restriction()?.let {
            return visit(builder, it)
        }

        return visit(builder, condition.composite())
    }

    protected open fun visit(builder: SqlBuilder<*>, restriction: FilterParser.RestrictionContext): Any? {
        val field = visit(builder, restriction.comparable())

        restriction.comparator()?.let {
            val value = visit(builder, restriction.arg())
            return when (it.text) {
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

        return field
    }

    protected open fun visit(builder: SqlBuilder<*>, comparable: FilterParser.ComparableContext): Any? {
        comparable.function()?.let {
            return visit(builder, it)
        }

        return visit(builder, comparable.member())
    }

    protected open fun visit(builder: SqlBuilder<*>, member: FilterParser.MemberContext): Any? {
        return builder.member(member)
    }

    protected open fun visit(builder: SqlBuilder<*>, composite: FilterParser.CompositeContext): Any? {
        return visit(builder, composite.expression())
    }

    protected open fun visit(builder: SqlBuilder<*>, args: FilterParser.ArgListContext): List<Any?> {
        return args.arg().map { visit(builder, it) }
    }

    protected open fun visit(builder: SqlBuilder<*>, arg: FilterParser.ArgContext): Any? {
        arg.comparable()?.let {
            return visit(builder, it)
        }

        arg.composite()?.let {
            return visit(builder, it)
        }

        return visit(builder, arg.value())
    }

    protected open fun visit(builder: SqlBuilder<*>, value: FilterParser.ValueContext): Any? {
        return when (value) {
            is FilterParser.IntContext -> value.text.toLong()
            is FilterParser.UintContext -> value.text.substring(0, value.text.length - 1).toULong()
            is FilterParser.DoubleContext -> value.text.toDouble()
            is FilterParser.StringContext -> visit(builder, value)
            is FilterParser.DurationContext -> Duration(value.text)
            is FilterParser.TimestampContext -> Timestamp(value.text)
            is FilterParser.BoolTrueContext -> true
            is FilterParser.BoolFalseContext -> false
            is FilterParser.NullContext -> null
            else -> null
        }
    }

    protected open fun visit(builder: SqlBuilder<*>, function: FilterParser.FunctionContext): Any? {
        return builder.runtime.invoke(function.name().joinToString(".") { it.text }, visit(builder, function.argList()))
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
