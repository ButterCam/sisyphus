package com.bybutter.sisyphus.middleware.kafka.autoconfigure

import com.bybutter.sisyphus.middleware.kafka.DefaultKafkaResourceFactory
import com.bybutter.sisyphus.middleware.kafka.KafkaResourceFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackageClasses = [SisyphusKafkaAutoConfiguration::class])
class SisyphusKafkaAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(value = [KafkaResourceFactory::class])
    fun defaultKafkaResourceFactory(): KafkaResourceFactory {
        return DefaultKafkaResourceFactory()
    }
}
