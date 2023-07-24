package com.bybutter.sisyphus.middleware.kafka.autoconfigure

import com.bybutter.sisyphus.middleware.kafka.KafkaLogger
import com.bybutter.sisyphus.middleware.kafka.metrics.MicrometerKafkaLogger
import io.micrometer.core.instrument.MeterRegistry
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
@ConditionalOnClass(name = ["io.micrometer.core.instrument.MeterRegistry"])
class KafkaMetricsAutoConfiguration {
    @Bean
    fun micrometerRocketMqLogger(registry: MeterRegistry): KafkaLogger {
        return MicrometerKafkaLogger(registry)
    }
}
