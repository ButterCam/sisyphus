package com.bybutter.sisyphus.middleware.mongo.autoconfigure

import com.bybutter.sisyphus.middleware.mongo.DefaultMongoClientFactory
import com.bybutter.sisyphus.middleware.mongo.MongoClientFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@AutoConfiguration
@ComponentScan(basePackageClasses = [SisyphusMongoAutoConfiguration::class])
class SisyphusMongoAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(value = [MongoClientFactory::class])
    fun defaultMongoClientFactory(): MongoClientFactory {
        return DefaultMongoClientFactory()
    }
}
