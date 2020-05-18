package com.bybutter.sisyphus.cel

import com.bybutter.sisyphus.cel.grammar.CelLexer
import com.bybutter.sisyphus.cel.grammar.CelParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

class CelEngine(global: Map<String, Any?>, val runtime: CelRuntime = CelRuntime()) {
    val context = CelContext(this, global)

    fun eval(cel: String): Any? {
        return context.visit(parse(cel))
    }

    fun eval(cel: String, global: Map<String, Any?>): Any? {
        val context = context.fork()
        context.global += global
        return context.visit(parse(cel))
    }

    companion object {
        fun parse(cel: String): CelParser.StartContext {
            val lexer = CelLexer(CharStreams.fromString(cel))
            val parser = CelParser(CommonTokenStream(lexer))
            return parser.start()
        }
    }
}
