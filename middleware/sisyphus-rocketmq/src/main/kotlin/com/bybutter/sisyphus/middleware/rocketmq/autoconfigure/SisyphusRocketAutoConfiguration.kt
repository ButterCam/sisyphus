package com.bybutter.sisyphus.middleware.rocketmq.autoconfigure

import com.bybutter.sisyphus.middleware.rocketmq.DefaultRocketMqResourceFactory
import com.bybutter.sisyphus.middleware.rocketmq.RocketMqResourceFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@AutoConfiguration
@ComponentScan(basePackageClasses = [SisyphusRocketAutoConfiguration::class])
class SisyphusRocketAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(value = [RocketMqResourceFactory::class])
    fun defaultRocketTemplateFactory(): RocketMqResourceFactory {
        return DefaultRocketMqResourceFactory()
    }
}
