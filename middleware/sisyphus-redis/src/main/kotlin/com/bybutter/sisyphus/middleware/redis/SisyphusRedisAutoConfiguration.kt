package com.bybutter.sisyphus.middleware.redis

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [SisyphusRedisAutoConfiguration::class])
class SisyphusRedisAutoConfiguration
