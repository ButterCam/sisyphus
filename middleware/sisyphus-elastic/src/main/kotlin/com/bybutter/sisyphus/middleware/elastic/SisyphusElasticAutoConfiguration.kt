package com.bybutter.sisyphus.middleware.elastic

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [SisyphusElasticAutoConfiguration::class])
class SisyphusElasticAutoConfiguration
