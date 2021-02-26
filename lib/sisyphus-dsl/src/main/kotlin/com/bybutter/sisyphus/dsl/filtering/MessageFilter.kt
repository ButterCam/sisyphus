package com.bybutter.sisyphus.dsl.filtering

import com.bybutter.sisyphus.dsl.filtering.grammar.FilterParser
import com.bybutter.sisyphus.protobuf.Message

class MessageFilter(filter: String) : (Message<*, *>) -> Boolean {
    val filter = FilterEngine.parse(filter)

    fun filter(message: Message<*, *>): Boolean {
        return invoke(message)
    }

    override fun invoke(p1: Message<*, *>): Boolean {
        return engine.eval(filter, mapOf("." to p1)) as Boolean
    }

    private class MessageFilterRuntime() : FilterRuntime(FilterStandardLibrary()) {
        override fun access(member: FilterParser.MemberContext, global: Map<String, Any?>): Any? {
            return member.names.map { it.text }.fold<String, Any?>(global["."]) { result, it ->
                invoke("access", listOf(result, it))
            }
        }
    }

    companion object {
        private val engine = FilterEngine(runtime = MessageFilterRuntime())
    }
}
