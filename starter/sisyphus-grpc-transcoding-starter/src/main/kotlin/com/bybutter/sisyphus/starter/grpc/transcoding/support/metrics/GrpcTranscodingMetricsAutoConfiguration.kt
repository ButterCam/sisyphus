package com.bybutter.sisyphus.starter.grpc.transcoding.support.metrics

import org.springframework.boot.actuate.metrics.web.reactive.server.WebFluxTagsContributor
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean

@AutoConfigureAfter(
    name = [
        "org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration"
    ]
)
@AutoConfigureBefore(name = ["org.springframework.boot.actuate.autoconfigure.metrics.web.reactive.WebFluxMetricsAutoConfiguration"])
@ConditionalOnClass(
    name = [
        "org.springframework.boot.actuate.metrics.web.reactive.server.WebFluxTagsContributor"
    ]
)
class GrpcTranscodingMetricsAutoConfiguration {
    @Bean
    fun grpcTranscodingWebFluxTagsContributor(): WebFluxTagsContributor {
        return GrpcTranscodingWebFluxTagsContributor()
    }
}
