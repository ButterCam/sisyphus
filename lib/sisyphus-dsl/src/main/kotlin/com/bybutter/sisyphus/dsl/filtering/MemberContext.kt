package com.bybutter.sisyphus.dsl.filtering

import com.bybutter.sisyphus.dsl.filtering.grammar.FilterParser
import com.bybutter.sisyphus.protobuf.primitives.invoke
import java.lang.IllegalStateException

open class MemberContext {

    fun visit(filter: FilterParser.FilterContext): List<String> {
        return visit(filter.e ?: return emptyList())
    }

    protected open fun visit(expr: FilterParser.ExpressionContext): List<String> {
        return expr.seq.fold(visit(expr.init) as MutableList) { members, seq ->
            members.apply {
                this.addAll(visit(seq))
            }
        }
    }

    protected open fun visit(seq: FilterParser.SequenceContext): List<String> {
        return seq.e.fold(visit(seq.init) as MutableList) { members, e ->
            members.apply {
                this.addAll(visit(e))
            }
        }
    }

    protected open fun visit(factor: FilterParser.FactorContext): List<String> {
        return factor.e.fold(visit(factor.init) as MutableList) { members, e ->
            members.apply {
                this.addAll(visit(e))
            }
        }
    }

    protected open fun visit(condition: FilterParser.ConditionContext): List<String> {
        return when (condition) {
            is FilterParser.NotConditionContext -> {
                visit(condition.expression())
            }
            is FilterParser.CompareConditionContext -> {
                listOfNotNull(visit(condition.left), visit(condition.right))
            }
            else -> throw IllegalStateException()
        }
    }

    protected open fun visit(value: FilterParser.ValueContext): String? {
        return value.member()?.let { return visit(it) }
    }

    protected open fun visit(member: FilterParser.MemberContext): String {
        return member.text
    }
}
