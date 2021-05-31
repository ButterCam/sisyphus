package com.bybutter.sisyphus.dsl.filtering

import com.bybutter.sisyphus.dsl.filtering.grammar.FilterLexer
import com.bybutter.sisyphus.dsl.filtering.grammar.FilterParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

object FilterDsl {
    fun parse(filter: String): FilterParser.FilterContext {
        val lexer = FilterLexer(CharStreams.fromString(filter))
        val parser = FilterParser(CommonTokenStream(lexer))
        return parser.filter()
    }
}
