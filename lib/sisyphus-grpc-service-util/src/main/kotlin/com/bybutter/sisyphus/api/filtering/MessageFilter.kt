package com.bybutter.sisyphus.api.filtering

import com.bybutter.sisyphus.api.filtering.grammar.FilterLexer
import com.bybutter.sisyphus.api.filtering.grammar.FilterParser
import com.bybutter.sisyphus.protobuf.CustomProtoType
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoEnum
import com.bybutter.sisyphus.protobuf.primitives.BoolValue
import com.bybutter.sisyphus.protobuf.primitives.BytesValue
import com.bybutter.sisyphus.protobuf.primitives.DoubleValue
import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.FloatValue
import com.bybutter.sisyphus.protobuf.primitives.Int32Value
import com.bybutter.sisyphus.protobuf.primitives.Int64Value
import com.bybutter.sisyphus.protobuf.primitives.ListValue
import com.bybutter.sisyphus.protobuf.primitives.NullValue
import com.bybutter.sisyphus.protobuf.primitives.StringValue
import com.bybutter.sisyphus.protobuf.primitives.Struct
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.UInt32Value
import com.bybutter.sisyphus.protobuf.primitives.UInt64Value
import com.bybutter.sisyphus.protobuf.primitives.Value
import com.bybutter.sisyphus.protobuf.primitives.string
import com.bybutter.sisyphus.security.base64
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

class MessageFilter(filter: String, val runtime: FilterRuntime = FilterRuntime()) : (Message<*, *>) -> Boolean {
    private val filter: FilterParser.FilterContext = parse(filter)

    fun filter(message: Message<*, *>): Boolean {
        return invoke(message)
    }

    override fun invoke(message: Message<*, *>): Boolean {
        val expr = filter.e ?: return true
        return visit(message, expr)
    }

    private fun visit(message: Message<*, *>, expr: FilterParser.ExpressionContext): Boolean {
        for (seq in expr.seq) {
            if (!visit(message, seq)) {
                return false
            }
        }
        return true
    }

    private fun visit(message: Message<*, *>, seq: FilterParser.SequenceContext): Boolean {
        for (e in seq.e) {
            if (!visit(message, e)) {
                return false
            }
        }
        return true
    }

    private fun visit(message: Message<*, *>, fac: FilterParser.FactorContext): Boolean {
        for (e in fac.e) {
            if (visit(message, e)) {
                return true
            }
        }
        return false
    }

    private fun visit(message: Message<*, *>, term: FilterParser.TermContext): Boolean {
        var value = visit(message, term.simple())
        value = when (term.op?.text) {
            "NOT" -> {
                when (value) {
                    is Boolean -> !value
                    is String -> !value.toBoolean()
                    null -> true
                    else -> false
                }
            }
            "-" -> {
                when (value) {
                    is Boolean -> !value
                    is Int -> -value
                    is UInt -> (-value.toLong()).toInt()
                    is Long -> -value
                    is ULong -> -value.toLong()
                    is String -> value.toDoubleOrNull()?.let { -it } ?: true
                    null -> true
                    else -> 0
                }
            }
            null -> value
            else -> throw IllegalArgumentException("Unsupported term operator '${term.op?.text}'.")
        }

        return when (value) {
            is Boolean -> value
            is Number -> value.toDouble() != 0.0
            is String -> value.toBoolean()
            null -> false
            else -> true
        }
    }

    private fun visit(message: Message<*, *>, simple: FilterParser.SimpleContext): Any? {
        return when (simple) {
            is FilterParser.RestrictionExprContext -> {
                visit(message, simple.restriction())
            }
            is FilterParser.CompositeExprContext -> {
                visit(message, simple.composite())
            }
            else -> throw UnsupportedOperationException("Unsupported simple expression '${simple.text}'.")
        }
    }

    private fun visit(message: Message<*, *>, rest: FilterParser.RestrictionContext): Any? {
        val left = visit(message, rest.left)
        return if (rest.op != null) {
            when (rest.op.text) {
                "<=", "<", ">", ">=" -> {
                    val right = visit(message, rest.right)
                    val result = DynamicOperator.compare(left?.toString(), right?.toString())
                    when (rest.op.text) {
                        "<=" -> result <= 0
                        "<" -> result < 0
                        ">=" -> result >= 0
                        ">" -> result > 0
                        else -> TODO()
                    }
                }
                "=" -> {
                    val right = visit(message, rest.right)
                    DynamicOperator.equals(left?.toString(), right?.toString())
                }
                "!=" -> {
                    val right = visit(message, rest.right)
                    !DynamicOperator.equals(left?.toString(), right?.toString())
                }
                ":" -> {
                    val right = visit(message, rest.right)?.toString()
                    if (right == "*") return left != null

                    when (left) {
                        is Message<*, *> -> {
                            right ?: return false
                            left.has(right)
                        }
                        is List<*> -> left.any {
                            DynamicOperator.equals(it?.toString(), right)
                        }
                        is Map<*, *> -> {
                            right ?: return false
                            left.containsKey(right)
                        }
                        else -> DynamicOperator.equals(left?.toString(), right)
                    }
                }
                else -> TODO()
            }
        } else {
            return left
        }
    }

    private fun visit(message: Message<*, *>, com: FilterParser.ComparableContext): Any? {
        return when (com) {
            is FilterParser.FucntionExprContext -> visit(message, com.function())
            is FilterParser.MemberExprContext -> visit(message, com.member())
            else -> throw UnsupportedOperationException("Unsupported comparable expression '${com.text}'.")
        }
    }

    private fun visit(message: Message<*, *>, com: FilterParser.CompositeContext): Any? {
        return visit(message, com.expression())
    }

    private fun visit(message: Message<*, *>, com: FilterParser.FunctionContext): Any? {
        val function = com.n.joinToString(".") { it.text }
        return runtime.invoke(function, com.argList()?.e?.map { visit(message, it) } ?: listOf())
    }

    private fun visit(message: Message<*, *>, arg: FilterParser.ArgContext): Any? {
        return when (arg) {
            is FilterParser.ArgComparableExprContext -> visit(message, arg.comparable())
            is FilterParser.ArgCompositeExprContext -> visit(message, arg.composite())
            else -> throw UnsupportedOperationException("Unsupported arg expression '${arg.text}'.")
        }
    }

    private fun visit(message: Message<*, *>, arg: FilterParser.MemberContext): Any? {
        val memberStart = visit(message, arg.value())
        return if (memberStart != null && message.support().fieldInfo(memberStart) != null) {
            var value = message.get<Any?>(memberStart)

            for (fieldContext in arg.e) {
                val fieldName = visit(message, fieldContext)

                when (value) {
                    is Message<*, *> -> {
                        value = value.get<Any?>(fieldName).protoNormalizing() ?: return null
                    }
                    is List<*> -> {
                        val int = fieldName.toIntOrNull() ?: return null
                        if (int >= value.size) return null
                        value = value[int]
                    }
                    is Map<*, *> -> {
                        if (!value.containsKey(fieldName)) return null
                        value = value[fieldName]
                    }
                    else -> return null
                }
            }

            value
        } else {
            val value = mutableListOf(memberStart)
            value += arg.e.map { visit(message, it) }
            value.joinToString(".")
        }
    }

    private fun visit(message: Message<*, *>, field: FilterParser.FieldContext): String {
        return field.value()?.let { visit(message, it) } ?: field.text
    }

    private fun Any?.protoNormalizing(): Any? {
        return when (this) {
            is ByteArray -> this.base64()
            is ListValue -> this.values.map { it.protoNormalizing() }
            is DoubleValue -> this.value.toString()
            is FloatValue -> this.value.toString()
            is Int64Value -> this.value.toString()
            is UInt64Value -> this.value.toString()
            is Int32Value -> this.value.toString()
            is UInt32Value -> this.value.toString()
            is BoolValue -> this.value.toString()
            is StringValue -> this.value
            is BytesValue -> this.value.base64()
            is Duration -> string()
            is Timestamp -> string()
            is NullValue -> null
            is Struct -> this.fields.mapValues { it.value.protoNormalizing() }
            is Value -> when (val kind = this.kind) {
                is Value.Kind.BoolValue -> kind.value.toString()
                is Value.Kind.ListValue -> kind.value.protoNormalizing()
                is Value.Kind.NullValue -> null
                is Value.Kind.NumberValue -> kind.value.toString()
                is Value.Kind.StringValue -> kind.value
                is Value.Kind.StructValue -> kind.value.protoNormalizing()
                null -> null
                else -> throw IllegalStateException("Illegal proto value type '${kind.javaClass}'.")
            }
            is ProtoEnum -> this.proto
            is List<*> -> this.map { it.protoNormalizing() }
            is Map<*, *> -> this.mapValues { it.value.protoNormalizing() }
            is CustomProtoType<*> -> this.value().protoNormalizing()
            null -> null
            is Int, is UInt, is Long, is ULong, is Float, is Double, is Boolean -> this.toString()
            is String, is Message<*, *> -> this
            else -> throw IllegalStateException("Illegal proto data type '${this.javaClass}'.")
        }
    }

    private fun visit(message: Message<*, *>, value: FilterParser.ValueContext): String? {
        if (value.STRING() != null) {
            return string(value.text)
        }

        if (value.TEXT() != null) {
            if (value.text == "null") return null
            return value.text
        }

        TODO()
    }

    private fun string(data: String): String {
        return when {
            data.startsWith("\"\"\"") -> data.substring(3, data.length - 3)
            data.startsWith("\"") -> data.substring(1, data.length - 1)
            data.startsWith("'") -> data.substring(1, data.length - 1)
            else -> throw IllegalStateException("Wrong string token '$data'.")
        }
    }

    companion object {
        fun parse(filter: String): FilterParser.FilterContext {
            val lexer = FilterLexer(CharStreams.fromString(filter))
            lexer.addErrorListener(FilterSyntaxErrorListener)
            val parser = FilterParser(CommonTokenStream(lexer))
            parser.addErrorListener(FilterSyntaxErrorListener)
            return parser.filter()
        }
    }
}
