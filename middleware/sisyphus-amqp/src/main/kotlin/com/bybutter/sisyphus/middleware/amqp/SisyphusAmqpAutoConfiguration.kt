package com.bybutter.sisyphus.middleware.amqp

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [SisyphusAmqpAutoConfiguration::class])
class SisyphusAmqpAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(value = [AmqpTemplateFactory::class])
    fun defaultAmqpTemplateFactory(): AmqpTemplateFactory {
        return DefaultAmqpTemplateFactory()
    }
}
