package com.bybutter.sisyphus.dsl.ordering

import com.bybutter.sisyphus.dsl.ordering.grammar.OrderLexer
import com.bybutter.sisyphus.dsl.ordering.grammar.OrderParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

object OrderDsl {
    fun parse(filter: String): OrderParser.StartContext {
        val lexer = OrderLexer(CharStreams.fromString(filter))
        val parser = OrderParser(CommonTokenStream(lexer))
        return parser.start()
    }
}
