package com.bybutter.sisyphus.dsl.cel

import com.bybutter.sisyphus.dsl.cel.grammar.CelLexer
import com.bybutter.sisyphus.dsl.cel.grammar.CelParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

class CelEngine(global: Map<String, Any?> = mapOf(), val runtime: CelRuntime = CelRuntime()) {
    val context = CelContext(this, global)

    fun eval(cel: String): Any? {
        return context.visit(parse(cel))
    }

    fun eval(cel: String, global: Map<String, Any?>): Any? {
        val context = context.fork()
        context.global += global
        return context.visit(parse(cel))
    }

    fun fork(global: Map<String, Any?>): CelEngine {
        return CelEngine(context.global + global, runtime)
    }

    companion object {
        fun parse(cel: String): CelParser.StartContext {
            val lexer = CelLexer(CharStreams.fromString(cel))
            lexer.addErrorListener(CelErrorListener)
            val parser = CelParser(CommonTokenStream(lexer))
            parser.addErrorListener(CelErrorListener)
            return parser.start()
        }
    }
}
