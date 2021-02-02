package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import com.bybutter.sisyphus.dsl.filtering.FilterRuntime
import com.bybutter.sisyphus.dsl.filtering.grammar.FilterParser
import org.jooq.Condition
import org.jooq.impl.DSL

abstract class JooqConditionBuilder(val runtime: FilterRuntime = FilterRuntime()) {
    fun visit(filter: FilterParser.FilterContext): Condition? {
        return visit(filter.e ?: return null)
    }

    protected open fun visit(expr: FilterParser.ExpressionContext): Condition {
        return expr.seq.fold<FilterParser.SequenceContext, Condition?>(null) { r, i ->
            if (r == null) return@fold visit(i).conditioned()
            val right = visit(i).conditioned() ?: return@fold r
            r.and(right)
        }!!
    }

    protected open fun visit(seq: FilterParser.SequenceContext): Condition {
        return seq.e.fold<FilterParser.FactorContext, Condition?>(null) { r, i ->
            if (r == null) return@fold visit(i).conditioned()
            val right = visit(i).conditioned() ?: return@fold r
            r.and(right)
        }!!
    }

    protected open fun visit(fac: FilterParser.FactorContext): Condition {
        return fac.e.fold<FilterParser.TermContext, Condition?>(null) { r, i ->
            if (r == null) return@fold visit(i)?.conditioned()
            val right = visit(i)?.conditioned() ?: return@fold r
            r.or(right)
        }!!
    }

    private fun Any?.conditioned(): Condition? {
        return when (this) {
            is Condition -> this
            is Boolean -> if (this) DSL.trueCondition() else DSL.falseCondition()
            else -> null
        }
    }

    protected open fun visit(term: FilterParser.TermContext): Any? {
        val result = visit(term.simple())

        return when (term.op?.text) {
            "-" -> {
                when (result) {
                    is Condition -> result.not()
                    is Boolean -> !result
                    is Int -> -result
                    is UInt -> (-result.toLong()).toInt()
                    is Long -> -result
                    is ULong -> -result.toLong()
                    is String -> result.toDoubleOrNull()?.let { -it } ?: true
                    null -> true
                    else -> 0
                }
            }
            "NOT" -> {
                when (result) {
                    is Condition -> result.not()
                    is Boolean -> !result
                    is String -> !result.toBoolean()
                    null -> true
                    else -> null
                }
            }
            null -> result
            else -> null
        }
    }

    protected open fun visit(simple: FilterParser.SimpleContext): Any? {
        return when (simple) {
            is FilterParser.RestrictionExprContext -> {
                visit(simple.restriction())
            }
            is FilterParser.CompositeExprContext -> {
                visit(simple.composite())
            }
            else -> throw UnsupportedOperationException("Unsupported simple expression '${simple.text}'.")
        }
    }

    protected open fun visit(rest: FilterParser.RestrictionContext): Any? {
        val left = visit(rest.left)
        if (rest.op == null) {
            return when (left) {
                is Condition -> left
                is Boolean -> left
                else -> throw IllegalArgumentException("Result of restriction must be boolean or Condition.")
            }
        }

        return buildCondition(left as String, rest.op.text, visit(rest.right) as String)
    }

    protected open fun visit(com: FilterParser.ComparableContext): Any? {
        return when (com) {
            is FilterParser.FucntionExprContext -> visit(com.function())
            is FilterParser.MemberExprContext -> visit(com.member())
            else -> throw UnsupportedOperationException("Unsupported comparable expression '${com.text}'.")
        }
    }

    protected open fun visit(com: FilterParser.FunctionContext): Any? {
        val function = com.n.joinToString(".") { it.text }
        return runtime.invoke(function, com.argList()?.e?.map { visit(it) } ?: listOf())
    }

    protected open fun visit(member: FilterParser.MemberContext): String {
        val part = mutableListOf(visit(member.value()))
        part += member.e.map { visit(it) }
        return part.joinToString(".")
    }

    protected open fun visit(field: FilterParser.FieldContext): String {
        return field.value()?.let { visit(it) } ?: field.text
    }

    protected open fun visit(value: FilterParser.ValueContext): String? {
        if (value.STRING() != null) {
            return string(value.text)
        }

        if (value.TEXT() != null) {
            if (value.text == "null") return null
            return value.text
        }

        return null
    }

    protected open fun visit(arg: FilterParser.ArgContext): Any? {
        return when (arg) {
            is FilterParser.ArgComparableExprContext -> visit(arg.comparable())
            is FilterParser.ArgCompositeExprContext -> visit(arg.composite())
            else -> throw UnsupportedOperationException("Unsupported arg expression '${arg.text}'.")
        }
    }

    protected open fun visit(com: FilterParser.CompositeContext): Any? {
        return visit(com.expression())
    }

    private fun string(data: String): String {
        return when {
            data.startsWith("\"\"\"") -> data.substring(3, data.length - 3)
            data.startsWith("\"") -> data.substring(1, data.length - 1)
            data.startsWith("'") -> data.substring(1, data.length - 1)
            else -> throw IllegalStateException("Wrong string token '$data'.")
        }
    }

    abstract fun buildCondition(name: String, op: String, value: String): Condition?
}
