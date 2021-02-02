package com.bybutter.sisyphus.starter.grpc.transcoding

import com.bybutter.sisyphus.api.HttpRule
import com.google.api.pathtemplate.PathTemplate
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.server.RequestPredicate
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest

/**
 * [RequestPredicate] that check the request path match with [HttpRule.Pattern].
 */
class HttpPatternPredicate(private val pattern: HttpRule.Pattern<*>) : RequestPredicate {
    private val pathTemplate: PathTemplate
    private val method: HttpMethod

    init {
        when (pattern) {
            is HttpRule.Pattern.Get -> {
                method = HttpMethod.GET
                pathTemplate = PathTemplate.create(pattern.value)
            }
            is HttpRule.Pattern.Post -> {
                method = HttpMethod.POST
                pathTemplate = PathTemplate.create(pattern.value)
            }
            is HttpRule.Pattern.Put -> {
                method = HttpMethod.PUT
                pathTemplate = PathTemplate.create(pattern.value)
            }
            is HttpRule.Pattern.Patch -> {
                method = HttpMethod.PATCH
                pathTemplate = PathTemplate.create(pattern.value)
            }
            is HttpRule.Pattern.Delete -> {
                method = HttpMethod.DELETE
                pathTemplate = PathTemplate.create(pattern.value)
            }
            is HttpRule.Pattern.Custom -> {
                method = HttpMethod.valueOf(pattern.value.kind)
                pathTemplate = PathTemplate.create(pattern.value.path)
            }
            else -> throw UnsupportedOperationException("Unknown http rule pattern")
        }
    }

    override fun accept(visitor: RequestPredicates.Visitor) {
        visitor.startAnd()
        visitor.method(setOf(method))
        visitor.and()
        visitor.pathExtension(pathTemplate.toString())
        visitor.endAnd()
        super.accept(visitor)
    }

    override fun test(request: ServerRequest): Boolean {
        if (request.method() != method) {
            return false
        }

        val result = pathTemplate.match(request.path().trim('/')) ?: return false
        request.attributes()[RouterFunctions.URI_TEMPLATE_VARIABLES_ATTRIBUTE] = result
        request.attributes()[TranscodingFunctions.MATCHING_PATH_TEMPLATE_ATTRIBUTE] = pathTemplate
        return true
    }
}
