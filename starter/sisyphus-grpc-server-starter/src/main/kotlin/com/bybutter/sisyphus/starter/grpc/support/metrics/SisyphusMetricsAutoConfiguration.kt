package com.bybutter.sisyphus.starter.grpc.support.metrics

import com.bybutter.sisyphus.starter.grpc.support.RequestLogger
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean

@AutoConfigureAfter(name = [
    "org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration",
    "org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration"
])
@AutoConfigureBefore(name = ["org.springframework.boot.actuate.autoconfigure.metrics.web.reactive.WebFluxMetricsAutoConfiguration"])
@ConditionalOnClass(name = ["io.micrometer.core.instrument.MeterRegistry"])
class SisyphusMetricsAutoConfiguration {
    @Bean
    fun micrometerRequestLogger(registry: MeterRegistry): RequestLogger {
        return MicrometerRequestLogger(registry)
    }
}
