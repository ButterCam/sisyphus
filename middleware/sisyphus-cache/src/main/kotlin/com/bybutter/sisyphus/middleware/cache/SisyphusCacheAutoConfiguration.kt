package com.bybutter.sisyphus.middleware.cache

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [SisyphusCacheAutoConfiguration::class])
class SisyphusCacheAutoConfiguration
