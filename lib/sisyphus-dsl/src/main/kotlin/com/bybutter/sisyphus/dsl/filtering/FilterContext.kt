package com.bybutter.sisyphus.dsl.filtering

import com.bybutter.sisyphus.dsl.filtering.grammar.FilterParser
import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.invoke
import com.bybutter.sisyphus.string.unescape
import java.lang.IllegalStateException

open class FilterContext(val engine: FilterEngine, global: Map<String, Any?> = mapOf()) {
    val global: MutableMap<String, Any?> = global.toMutableMap()

    fun fork(): FilterContext {
        return FilterContext(engine, this.global)
    }

    fun visit(filter: FilterParser.FilterContext): Any? {
        return visit(filter.e ?: return null)
    }

    protected open fun visit(expr: FilterParser.ExpressionContext): Any? {
        return expr.seq.fold(visit(expr.init)) { cond, seq ->
            engine.runtime.invoke("and", listOf(cond, visit(seq)))
        }
    }

    protected open fun visit(seq: FilterParser.SequenceContext): Any? {
        return seq.e.fold(visit(seq.init)) { cond, e ->
            engine.runtime.invoke("union", listOf(cond, visit(e)))
        }
    }

    protected open fun visit(factor: FilterParser.FactorContext): Any? {
        return factor.e.fold(visit(factor.init)) { cond, e ->
            engine.runtime.invoke("or", listOf(cond, visit(e)))
        }
    }

    protected open fun visit(condition: FilterParser.ConditionContext): Any? {
        return when (condition) {
            is FilterParser.NotConditionContext -> {
                engine.runtime.invoke("not", listOf(visit(condition.expression())))
            }
            is FilterParser.CompareConditionContext -> {
                val field = visit(condition.left)
                val value = visit(condition.right)
                val op = condition.comparator().text
                return when (op) {
                    "<=" -> engine.runtime.invokeOrDefault("lessOrEquals", listOf(field, value)) {
                        engine.runtime.invoke("compare", listOf(field, value)) as Int <= 0
                    }
                    "<" -> engine.runtime.invokeOrDefault("lessThan", listOf(field, value)) {
                        (engine.runtime.invoke("compare", listOf(field, value)) as Int) < 0
                    }
                    ">=" -> engine.runtime.invokeOrDefault("greaterOrEqual", listOf(field, value)) {
                        engine.runtime.invoke("compare", listOf(field, value)) as Int >= 0
                    }
                    ">" -> engine.runtime.invokeOrDefault("greaterThan", listOf(field, value)) {
                        engine.runtime.invoke("compare", listOf(field, value)) as Int > 0
                    }
                    "=" -> engine.runtime.invoke("equals", listOf(field, value))
                    "!=" -> engine.runtime.invoke("notEquals", listOf(field, value))
                    ":" -> engine.runtime.invoke("has", listOf(field, value))
                    else -> TODO()
                }
            }
            else -> throw IllegalStateException()
        }
    }

    protected open fun visit(member: FilterParser.MemberContext): Any? {
        return engine.runtime.access(member, global)
    }

    protected open fun visit(value: FilterParser.ValueContext): Any? {
        value.member()?.let { return visit(it) }
        value.literal()?.let { return visit(it) }
        value.function()?.let { return visit(it) }
        return null
    }

    protected open fun visit(function: FilterParser.FunctionContext): Any? {
        return engine.runtime.invoke(function.name.text, visit(function.argList()))
    }

    protected open fun visit(argList: FilterParser.ArgListContext): List<Any?> {
        return argList.args.map { visit(it) }
    }

    protected open fun visit(literal: FilterParser.LiteralContext): Any? {
        return when (literal) {
            is FilterParser.IntContext -> literal.text.toLong()
            is FilterParser.UintContext -> literal.text.substring(0, literal.text.length - 1).toULong()
            is FilterParser.DoubleContext -> literal.text.toDouble()
            is FilterParser.StringContext -> visit(literal)
            is FilterParser.BoolTrueContext -> true
            is FilterParser.BoolFalseContext -> false
            is FilterParser.NullContext -> null
            is FilterParser.DurationContext -> Duration(literal.text)
            is FilterParser.TimestampContext -> Timestamp(literal.text)
            else -> throw UnsupportedOperationException("Unsupported literal expression '${literal.text}'.")
        }
    }

    protected open fun visit(value: FilterParser.StringContext): String {
        val string = value.text
        return when {
            string.startsWith("\"") -> string.substring(1, string.length - 1)
            string.startsWith("'") -> string.substring(1, string.length - 1)
            else -> throw IllegalStateException("Wrong string token '${value.text}'.")
        }.unescape()
    }
}
