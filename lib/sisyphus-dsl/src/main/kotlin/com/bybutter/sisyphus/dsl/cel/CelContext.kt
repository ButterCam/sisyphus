package com.bybutter.sisyphus.dsl.cel

import com.bybutter.sisyphus.dsl.cel.grammar.CelParser
import com.bybutter.sisyphus.protobuf.InternalProtoApi
import com.bybutter.sisyphus.string.unescape

@OptIn(InternalProtoApi::class)
class CelContext internal constructor(private val engine: CelEngine, global: Map<String, Any?> = mapOf()) {
    val global: MutableMap<String, Any?> = global.toMutableMap()

    fun fork(): CelContext {
        return CelContext(engine, this.global)
    }

    fun visit(start: CelParser.StartContext): Any? {
        val expr = start.e ?: return null
        return visit(expr)
    }

    fun visit(expr: CelParser.ExprContext): Any? {
        return if (expr.op != null) {
            val condition =
                visit(expr.e) as? Boolean
                    ?: throw IllegalStateException("Conditional expr '${expr.e.text}' must be bool")
            if (condition) {
                visit(expr.e1)
            } else {
                visit(expr.e2)
            }
        } else {
            visit(expr.e)
        }
    }

    fun visit(or: CelParser.ConditionalOrContext): Any? {
        var result = visit(or.e)

        for (relation in or.e1) {
            if (result == true) break
            result = or(result, relation)
        }

        return result
    }

    private fun or(
        left: Any?,
        right: CelParser.ConditionalAndContext,
    ): Any? {
        if (left == true) return left
        if (left == false) return visit(right)

        val right = visit(right)
        if (right == true) return right
        if (right == false) return left

        TODO()
    }

    fun visit(and: CelParser.ConditionalAndContext): Any? {
        var result = visit(and.e)

        for (relation in and.e1) {
            if (result == false) break
            result = and(result, relation)
        }

        return result
    }

    private fun and(
        left: Any?,
        right: CelParser.RelationContext,
    ): Any? {
        if (left == false) return left
        if (left == true) return visit(right)

        val right = visit(right)
        if (right == false) return right
        if (right == true) return left

        TODO()
    }

    fun visit(relation: CelParser.RelationContext): Any? {
        relation.calc()?.let {
            return visit(it)
        }

        return when (relation.op.text) {
            "<", "<=", ">", ">=" -> compare(relation.relation(0), relation.op.text, relation.relation(1))
            "==", "!=" -> equals(relation.relation(0), relation.op.text, relation.relation(1))
            "in" -> engine.runtime.invoke(null, "contains", visit(relation.relation(1)), visit(relation.relation(0)))
            else -> TODO()
        }
    }

    private fun compare(
        left: CelParser.RelationContext,
        operator: String,
        right: CelParser.RelationContext,
    ): Boolean {
        val result =
            engine.runtime.invoke(null, "compare", visit(left), visit(right)) as? Long
                ?: throw IllegalStateException("Compare function must return CEL int(java.Long).")
        return when (operator) {
            "<" -> result < 0
            "<=" -> result <= 0
            ">" -> result > 0
            ">=" -> result >= 0
            else -> throw IllegalStateException("Wrong compare operator '$operator'.")
        }
    }

    private fun equals(
        left: CelParser.RelationContext,
        operator: String,
        right: CelParser.RelationContext,
    ): Boolean {
        val result =
            engine.runtime.invoke(null, "equals", visit(left), visit(right)) as? Boolean
                ?: throw IllegalStateException("Equals function must return CEL bool(java.Boolean).")
        return when (operator) {
            "==" -> result
            "!=" -> !result
            else -> throw IllegalStateException("Wrong equals operator '$operator'.")
        }
    }

    fun visit(calc: CelParser.CalcContext): Any? {
        calc.unary()?.let {
            return visit(it)
        }

        return when (calc.op.text) {
            "*" -> engine.runtime.invoke(null, "times", visit(calc.calc(0)), visit(calc.calc(1)))
            "/" -> engine.runtime.invoke(null, "div", visit(calc.calc(0)), visit(calc.calc(1)))
            "%" -> engine.runtime.invoke(null, "rem", visit(calc.calc(0)), visit(calc.calc(1)))
            "+" -> engine.runtime.invoke(null, "plus", visit(calc.calc(0)), visit(calc.calc(1)))
            "-" -> engine.runtime.invoke(null, "minus", visit(calc.calc(0)), visit(calc.calc(1)))
            else -> TODO()
        }
    }

    fun visit(unary: CelParser.UnaryContext): Any? {
        return when (unary) {
            is CelParser.MemberExprContext -> {
                visit(unary.member())
            }
            is CelParser.LogicalNotContext -> {
                engine.runtime.invoke(null, "logicalNot", visit(unary.member()))
            }
            is CelParser.NegateContext -> {
                engine.runtime.invoke(null, "negate", visit(unary.member()))
            }
            else -> throw UnsupportedOperationException("Unsupported unary expression '${unary.text}'.")
        }
    }

    fun visit(member: CelParser.MemberContext): Any? {
        return when (member) {
            is CelParser.PrimaryExprContext -> {
                visit(member.primary())
            }
            is CelParser.SelectOrCallContext -> {
                if (member.open != null) {
                    engine.runtime.invokeMarco(
                        this,
                        visit(member.member()),
                        member.IDENTIFIER().text,
                        member.args?.e
                            ?: listOf(),
                    ) { return it }
                    engine.runtime.invoke(
                        visit(member.member()),
                        member.IDENTIFIER().text,
                        member.args?.e?.map { visit(it) }
                            ?: listOf(),
                    )
                } else {
                    if (member.text.startsWith(".")) {
                        engine.runtime.getGlobalField(member.text, global)
                    } else {
                        engine.runtime.invoke(null, "access", visit(member.member()), member.IDENTIFIER().text)
                    }
                }
            }
            is CelParser.IndexContext -> {
                engine.runtime.invoke(null, "index", visit(member.member()), visit(member.index))
            }
            is CelParser.CreateMessageContext -> {
                engine.runtime.createMessage(
                    member.member().text,
                    (
                        member.fieldInitializerList()?.fields
                            ?: listOf()
                    ).asSequence().mapIndexed { index, token ->
                        token.text to visit(member.fieldInitializerList().values[index])
                    }.associate { it },
                )
            }
            else -> throw UnsupportedOperationException("Unsupported member expression '${member.text}'.")
        }
    }

    fun visit(primary: CelParser.PrimaryContext): Any? {
        return when (primary) {
            is CelParser.IdentOrGlobalCallContext -> {
                if (primary.op != null) {
                    engine.runtime.invokeMarco(
                        this,
                        null,
                        primary.IDENTIFIER().text,
                        primary.args?.e
                            ?: listOf(),
                    ) { return it }
                    engine.runtime.invoke(
                        null,
                        primary.IDENTIFIER().text,
                        primary.args?.e?.map { visit(it) }
                            ?: listOf(),
                    )
                } else {
                    engine.runtime.getGlobalField(primary.text, global)
                }
            }
            is CelParser.NestedContext -> visit(primary.expr())
            is CelParser.CreateListContext -> primary.elems?.e?.map { visit(it) } ?: listOf<Any?>()
            is CelParser.CreateStructContext -> {
                primary.entries?.keys?.withIndex()?.associate { (index, key) ->
                    key.text to visit(primary.entries.values[index])
                } ?: mapOf<String, Any?>()
            }
            is CelParser.ConstantLiteralContext -> visit(primary.literal())
            else -> throw UnsupportedOperationException("Unsupported primary expression '${primary.text}'.")
        }
    }

    fun visit(literal: CelParser.LiteralContext): Any? {
        return when (literal) {
            is CelParser.IntContext -> literal.text.toLong()
            is CelParser.UintContext -> literal.text.substring(0, literal.text.length - 1).toULong()
            is CelParser.DoubleContext -> literal.text.toDouble()
            is CelParser.StringContext -> celString(literal.text)
            is CelParser.BytesContext -> celBytes(literal.text)
            is CelParser.BoolTrueContext -> true
            is CelParser.BoolFalseContext -> false
            is CelParser.NullContext -> null
            else -> throw UnsupportedOperationException("Unsupported literal expression '${literal.text}'.")
        }
    }

    private fun celBytes(data: String): ByteArray {
        return if (data.startsWith("b") || data.startsWith("B")) {
            celString(data.substring(1)).toByteArray()
        } else {
            throw IllegalStateException("Wrong bytes token '$data'.")
        }
    }

    private fun celString(data: String): String {
        val rawMode: Boolean
        val string =
            if (data.startsWith("r") || data.startsWith("R")) {
                rawMode = true
                data.subSequence(1, data.length)
            } else {
                rawMode = false
                data
            }

        val rawString =
            when {
                string.startsWith("\"\"\"") -> string.substring(3, string.length - 3)
                string.startsWith("\"") -> string.substring(1, string.length - 1)
                string.startsWith("'") -> string.substring(1, string.length - 1)
                else -> throw IllegalStateException("Wrong string token '$data'.")
            }

        return if (rawMode) {
            rawString
        } else {
            rawString.unescape()
        }
    }
}
