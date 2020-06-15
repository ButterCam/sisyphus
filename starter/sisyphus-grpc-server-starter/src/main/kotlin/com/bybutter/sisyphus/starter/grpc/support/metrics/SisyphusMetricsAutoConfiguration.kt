package com.bybutter.sisyphus.starter.grpc.support.metrics

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.metrics.web.reactive.WebFluxMetricsAutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(MetricsAutoConfiguration::class, SimpleMetricsExportAutoConfiguration::class)
@AutoConfigureBefore(WebFluxMetricsAutoConfiguration::class)
@ConditionalOnBean(MeterRegistry::class)
class SisyphusMetricsAutoConfiguration {
    @Bean
    fun micrometerInterceptor(registry: MeterRegistry) : MicrometerTimerInterceptor {
        return MicrometerTimerInterceptor(registry)
    }
}