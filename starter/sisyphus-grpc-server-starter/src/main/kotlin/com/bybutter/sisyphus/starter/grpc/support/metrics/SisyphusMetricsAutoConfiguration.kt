package com.bybutter.sisyphus.starter.grpc.support.metrics

import com.bybutter.sisyphus.starter.grpc.support.RequestLogger
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean

@AutoConfiguration(
    afterName = [
        "org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration"
    ],
    beforeName = [
        "org.springframework.boot.actuate.autoconfigure.metrics.web.reactive.WebFluxMetricsAutoConfiguration"
    ]
)
@ConditionalOnClass(name = ["io.micrometer.core.instrument.MeterRegistry"])
class SisyphusMetricsAutoConfiguration {
    @Bean
    fun micrometerRequestLogger(registry: MeterRegistry): RequestLogger {
        return MicrometerRequestLogger(registry)
    }
}
