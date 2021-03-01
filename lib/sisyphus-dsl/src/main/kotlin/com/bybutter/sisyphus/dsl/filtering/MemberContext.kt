package com.bybutter.sisyphus.dsl.filtering

import com.bybutter.sisyphus.dsl.filtering.grammar.FilterParser
import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.invoke
import com.bybutter.sisyphus.string.unescape
import java.lang.IllegalStateException

open class MemberContext(val engine: FilterEngine, global: Map<String, Any?> = mapOf()) {
    val global: MutableMap<String, Any?> = global.toMutableMap()

    fun fork(): MemberContext {
        return MemberContext(engine, this.global)
    }

    fun visit(filter: FilterParser.FilterContext): List<Any> {
        return visit(filter.e).filterNotNull()
    }

    protected open fun visit(expr: FilterParser.ExpressionContext): List<Any?> {
        return expr.seq.fold(visit(expr.init) as MutableList) { members, seq ->
            members.apply {
                this.addAll(visit(seq))
            }
        }
    }

    protected open fun visit(seq: FilterParser.SequenceContext): List<Any?> {
        return seq.e.fold(visit(seq.init) as MutableList) { members, e ->
            members.apply {
                this.addAll(visit(e))
            }
        }
    }

    protected open fun visit(factor: FilterParser.FactorContext): List<Any?> {
        return factor.e.fold(mutableListOf(visit(factor.init))) { members, e ->
            members.apply {
                this.add(visit(e))
            }
        }
    }

    protected open fun visit(condition: FilterParser.ConditionContext): Any? {
        return when (condition) {
            is FilterParser.NotConditionContext -> {
                visit(condition.expression())
            }
            is FilterParser.CompareConditionContext -> {
                visit(condition.left)
            }
            else -> throw IllegalStateException()
        }
    }

    protected open fun visit(member: FilterParser.MemberContext): Any? {
        return engine.runtime.access(member, global)
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
