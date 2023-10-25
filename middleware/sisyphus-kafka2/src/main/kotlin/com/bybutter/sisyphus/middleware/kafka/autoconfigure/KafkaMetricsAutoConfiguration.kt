package com.bybutter.sisyphus.middleware.kafka.autoconfigure

import com.bybutter.sisyphus.middleware.kafka.KafkaLogger
import com.bybutter.sisyphus.middleware.kafka.metrics.MicrometerKafkaLogger
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean

@AutoConfiguration(
    afterName = [
        "org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration",
    ],
    beforeName = ["org.springframework.boot.actuate.autoconfigure.metrics.web.reactive.WebFluxMetricsAutoConfiguration"],
)
@ConditionalOnClass(name = ["io.micrometer.core.instrument.MeterRegistry"])
class KafkaMetricsAutoConfiguration {
    @Bean
    fun micrometerKafkaMqLogger(registry: MeterRegistry): KafkaLogger {
        return MicrometerKafkaLogger(registry)
    }
}
