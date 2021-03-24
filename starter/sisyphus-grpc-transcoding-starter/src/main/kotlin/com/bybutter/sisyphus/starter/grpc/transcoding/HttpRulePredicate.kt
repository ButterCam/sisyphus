package com.bybutter.sisyphus.starter.grpc.transcoding

import com.bybutter.sisyphus.api.HttpRule
import org.springframework.web.reactive.function.server.RequestPredicate
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.ServerRequest

/**
 * [RequestPredicate] that check the request path match with [HttpRule].
 */
class HttpRulePredicate(private val rule: HttpRule) : RequestPredicate {
    private val patternPredicates = mutableListOf<HttpPatternPredicate>()

    init {
        rule.pattern?.let {
            patternPredicates.add(HttpPatternPredicate(it))
        }
        patternPredicates.addAll(
            rule.additionalBindings.mapNotNull {
                it.pattern?.let {
                    HttpPatternPredicate(it)
                }
            }
        )
    }

    override fun test(request: ServerRequest): Boolean {
        return patternPredicates.any {
            it.test(request)
        }
    }

    override fun accept(visitor: RequestPredicates.Visitor) {
        visitor.startOr()
        for ((index, patternPredicate) in patternPredicates.withIndex()) {
            if (index > 0) {
                visitor.or()
            }
            patternPredicate.accept(visitor)
        }
        visitor.endOr()
    }
}
