package com.bybutter.sisyphus.middleware.amqp

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [SisyphusAmqpAutoConfiguration::class])
class SisyphusAmqpAutoConfiguration
