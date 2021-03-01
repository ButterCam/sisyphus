package com.bybutter.sisyphus.dsl.filtering

import com.bybutter.sisyphus.dsl.filtering.grammar.FilterLexer
import com.bybutter.sisyphus.dsl.filtering.grammar.FilterParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

class FilterEngine(global: Map<String, Any> = mapOf(), val runtime: FilterRuntime = FilterRuntime()) {
    val context = FilterContext(this, global)
    val memberContext = MemberContext(this, global)

    fun eval(filter: String): Any? {
        return eval(parse(filter))
    }

    fun eval(filter: String, global: Map<String, Any?>): Any? {
        return eval(parse(filter), global)
    }

    fun eval(filter: FilterParser.FilterContext): Any? {
        return context.visit(filter)
    }

    fun eval(filter: FilterParser.FilterContext, global: Map<String, Any?>): Any? {
        val context = context.fork()
        context.global += global
        return context.visit(filter)
    }

    fun memberEval(filter: String): List<Any> {
        return memberEval(parse(filter))
    }

    fun memberEval(filter: String, global: Map<String, Any?>): List<Any> {
        return memberEval(parse(filter), global)
    }

    fun memberEval(filter: FilterParser.FilterContext): List<Any> {
        return memberContext.visit(filter)
    }

    fun memberEval(filter: FilterParser.FilterContext, global: Map<String, Any?>): List<Any> {
        val context = memberContext.fork()
        context.global += global
        return context.visit(filter)
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
