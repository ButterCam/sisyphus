package com.bybutter.sisyphus.middleware.rocketmq.autoconfigure

import com.bybutter.sisyphus.middleware.rocketmq.RocketMqLogger
import com.bybutter.sisyphus.middleware.rocketmq.metrics.MicrometerRocketMqLogger
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
class RocketMqMetricsAutoConfiguration {
    @Bean
    fun micrometerRocketMqLogger(registry: MeterRegistry): RocketMqLogger {
        return MicrometerRocketMqLogger(registry)
    }
}
