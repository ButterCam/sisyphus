package com.bybutter.sisyphus.middleware.rocketmq.autoconfigure

import com.bybutter.sisyphus.middleware.rocketmq.RocketMqLogger
import com.bybutter.sisyphus.middleware.rocketmq.metrics.MicrometerRocketMqLogger
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
class RocketMqMetricsAutoConfiguration {
    @Bean
    fun micrometerRocketMqLogger(registry: MeterRegistry): RocketMqLogger {
        return MicrometerRocketMqLogger(registry)
    }
}
