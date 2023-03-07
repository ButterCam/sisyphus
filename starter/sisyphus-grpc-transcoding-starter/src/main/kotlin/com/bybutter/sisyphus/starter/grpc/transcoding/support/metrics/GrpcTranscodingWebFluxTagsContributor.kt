package com.bybutter.sisyphus.starter.grpc.transcoding.support.metrics

import com.bybutter.sisyphus.starter.grpc.transcoding.TranscodingFunctions
import com.bybutter.sisyphus.starter.grpc.transcoding.TranscodingRouterRule
import com.google.api.pathtemplate.PathTemplate
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags
import org.springframework.boot.actuate.metrics.web.reactive.server.WebFluxTagsContributor
import org.springframework.web.server.ServerWebExchange

class GrpcTranscodingWebFluxTagsContributor : WebFluxTagsContributor {
    override fun httpRequestTags(exchange: ServerWebExchange, ex: Throwable?): MutableIterable<Tag> {
        val rule =
            exchange.getAttribute<TranscodingRouterRule>(TranscodingFunctions.TRANSCODING_RULE_ATTRIBUTE)
        val pathTemplate = exchange.getAttribute<PathTemplate>(TranscodingFunctions.MATCHING_PATH_TEMPLATE_ATTRIBUTE)

        if (rule == null || pathTemplate == null) {
            return Tags.of(
                "grpc_method",
                "None",
                "grpc_service",
                "None"
            )
        }

        return Tags.of(
            "grpc_method",
            rule.method.methodDescriptor.fullMethodName,
            "grpc_service",
            rule.service.serviceDescriptor.name,
            "uri",
            pathTemplate.toString()
        )
    }
}
