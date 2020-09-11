package com.bybutter.sisyphus.middleware.rocketmq

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [SisyphusRocketAutoConfiguration::class])
class SisyphusRocketAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(value = [RocketTemplateFactory::class])
    fun defaultAmqpTemplateFactory(): RocketTemplateFactory {
        return DefaultRocketTemplateFactory()
    }
}
