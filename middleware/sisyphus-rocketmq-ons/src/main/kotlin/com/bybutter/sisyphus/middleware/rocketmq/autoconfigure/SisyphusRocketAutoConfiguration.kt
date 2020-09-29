package com.bybutter.sisyphus.middleware.rocketmq.autoconfigure

import com.bybutter.sisyphus.middleware.rocketmq.DefaultRocketTemplateFactory
import com.bybutter.sisyphus.middleware.rocketmq.RocketTemplateFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackageClasses = [SisyphusRocketAutoConfiguration::class])
class SisyphusRocketAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(value = [RocketTemplateFactory::class])
    fun defaultRocketTemplateFactory(): RocketTemplateFactory {
        return DefaultRocketTemplateFactory()
    }
}
