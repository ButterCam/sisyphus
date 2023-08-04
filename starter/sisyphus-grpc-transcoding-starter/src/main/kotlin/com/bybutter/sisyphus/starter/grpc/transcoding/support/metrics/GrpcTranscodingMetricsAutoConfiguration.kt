package com.bybutter.sisyphus.starter.grpc.transcoding.support.metrics

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.http.server.reactive.observation.ServerRequestObservationConvention

@AutoConfiguration(
    afterName = ["org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration", "org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration"],
    beforeName = ["org.springframework.boot.actuate.autoconfigure.metrics.web.reactive.WebFluxMetricsAutoConfiguration"]
)
@ConditionalOnClass(
    name = ["org.springframework.boot.actuate.metrics.web.reactive.server.WebFluxTagsContributor"]
)
class GrpcTranscodingMetricsAutoConfiguration {
    @Bean
    fun grpcTranscodingRequestObservationConvention(): ServerRequestObservationConvention {
        return GrpcTranscodingRequestObservationConvention()
    }
}
