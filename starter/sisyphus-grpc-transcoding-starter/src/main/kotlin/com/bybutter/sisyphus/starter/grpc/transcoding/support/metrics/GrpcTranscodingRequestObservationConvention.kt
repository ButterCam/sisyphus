package com.bybutter.sisyphus.starter.grpc.transcoding.support.metrics

import com.bybutter.sisyphus.starter.grpc.transcoding.TranscodingFunctions
import com.bybutter.sisyphus.starter.grpc.transcoding.TranscodingRouterRule
import com.google.api.pathtemplate.PathTemplate
import io.micrometer.common.KeyValues
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext
import org.springframework.http.server.reactive.observation.ServerRequestObservationConvention

class GrpcTranscodingRequestObservationConvention : ServerRequestObservationConvention {
    override fun getLowCardinalityKeyValues(context: ServerRequestObservationContext): KeyValues {
        val rule = context.attributes[TranscodingFunctions.TRANSCODING_RULE_ATTRIBUTE] as? TranscodingRouterRule
        val pathTemplate = context.attributes[TranscodingFunctions.MATCHING_PATH_TEMPLATE_ATTRIBUTE] as? PathTemplate

        if (rule == null || pathTemplate == null) {
            return KeyValues.of(
                "grpc_method",
                "None",
                "grpc_service",
                "None"
            )
        }

        return KeyValues.of(
            "grpc_method",
            rule.method.methodDescriptor.fullMethodName,
            "grpc_service",
            rule.service.serviceDescriptor.name,
            "uri",
            pathTemplate.toString()
        )
    }
}
