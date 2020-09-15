package com.bybutter.sisyphus.middleware.amqp.autoconfigure

import com.bybutter.sisyphus.middleware.amqp.AmqpTemplateFactory
import com.bybutter.sisyphus.middleware.amqp.DefaultAmqpTemplateFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackageClasses = [SisyphusAmqpAutoConfiguration::class])
class SisyphusAmqpAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(value = [AmqpTemplateFactory::class])
    fun defaultAmqpTemplateFactory(): AmqpTemplateFactory {
        return DefaultAmqpTemplateFactory()
    }
}
