package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import com.bybutter.sisyphus.dsl.filtering.FilterEngine
import com.bybutter.sisyphus.dsl.filtering.grammar.FilterParser
import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.invoke
import com.bybutter.sisyphus.string.unescape
import org.jooq.Condition
import org.jooq.Field

open class JooqSqlBuilder(val runtime: JooqFilterRuntime = JooqFilterRuntime()) {
    fun build(context: JooqFilterContext<*>, filter: String): Condition? {
        return visit(context, FilterEngine.parse(filter))
    }

    fun visit(context: JooqFilterContext<*>, filter: FilterParser.FilterContext): Condition? {
        return visit(context, filter.e ?: return null)
    }

    protected open fun visit(context: JooqFilterContext<*>, expr: FilterParser.ExpressionContext): Condition? {
        return expr.seq.fold(visit(context, expr.init)) { cond, seq ->
            runtime.invoke("and", listOf(cond, visit(context, seq))) as? Condition
        }
    }

    protected open fun visit(context: JooqFilterContext<*>, seq: FilterParser.SequenceContext): Condition? {
        return seq.e.fold(visit(context, seq.init)) { cond, e ->
            runtime.invoke("union", listOf(cond, visit(context, e))) as? Condition
        }
    }

    protected open fun visit(context: JooqFilterContext<*>, factor: FilterParser.FactorContext): Condition? {
        return factor.e.fold(visit(context, factor.init)) { cond, e ->
            runtime.invoke("or", listOf(cond, visit(context, e))) as? Condition
        }
    }

    protected open fun visit(context: JooqFilterContext<*>, condition: FilterParser.ConditionContext): Condition? {
        return when (condition) {
            is FilterParser.NotConditionContext -> {
                runtime.invoke("not", listOf(visit(context, condition.expression()))) as? Condition
            }
            is FilterParser.CompareConditionContext -> {
                val field = visit(context, condition.left)
                val value = visit(context, condition.right)
                val op = condition.comparator().text
                return when (op) {
                    "<=" -> runtime.invoke("lessOrEquals", listOf(field, value)) as? Condition
                    "<" -> runtime.invoke("lessThan", listOf(field, value)) as? Condition
                    ">=" -> runtime.invoke("greaterOrEqual", listOf(field, value)) as? Condition
                    ">" -> runtime.invoke("greaterThan", listOf(field, value)) as? Condition
                    "=" -> runtime.invoke("equals", listOf(field, value)) as? Condition
                    "!=" -> runtime.invoke("notEquals", listOf(field, value)) as? Condition
                    ":" -> runtime.invoke("has", listOf(field, value)) as? Condition
                    else -> TODO()
                }
            }
            is FilterParser.FunConditionContext -> visit(context, condition.function()) as? Condition
            else -> throw IllegalStateException()
        }
    }

    protected open fun visit(context: JooqFilterContext<*>, member: FilterParser.MemberContext): Field<*> {
        return context.member(member)
    }

    protected open fun visit(context: JooqFilterContext<*>, value: FilterParser.ValueContext): Any? {
        value.member()?.let { return visit(context, it) }
        value.literal()?.let { return visit(context, it) }
        value.function()?.let { return visit(context, it) }
        return null
    }

    protected open fun visit(context: JooqFilterContext<*>, function: FilterParser.FunctionContext): Any? {
        return runtime.invoke(function.name.text, visit(context, function.argList()))
    }

    protected open fun visit(context: JooqFilterContext<*>, argList: FilterParser.ArgListContext): List<Any?> {
        return argList.args.map { visit(context, it) }
    }

    protected open fun visit(context: JooqFilterContext<*>, literal: FilterParser.LiteralContext): Any? {
        return when (literal) {
            is FilterParser.IntContext -> literal.text.toLong()
            is FilterParser.UintContext -> literal.text.substring(0, literal.text.length - 1).toULong()
            is FilterParser.DoubleContext -> literal.text.toDouble()
            is FilterParser.StringContext -> visit(context, literal)
            is FilterParser.BoolTrueContext -> true
            is FilterParser.BoolFalseContext -> false
            is FilterParser.NullContext -> null
            is FilterParser.DurationContext -> Duration(literal.text)
            is FilterParser.TimestampContext -> Timestamp(literal.text)
            else -> throw UnsupportedOperationException("Unsupported literal expression '${literal.text}'.")
        }
    }

    protected open fun visit(context: JooqFilterContext<*>, value: FilterParser.StringContext): String {
        val string = value.text
        return when {
            string.startsWith("\"") -> string.substring(1, string.length - 1)
            string.startsWith("'") -> string.substring(1, string.length - 1)
            else -> throw java.lang.IllegalStateException("Wrong string token '${value.text}'.")
        }.unescape()
    }

    companion object {
        val DEFAULT = JooqSqlBuilder()
    }
}
