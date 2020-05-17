package com.bybutter.sisyphus.middleware.hbase

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [SisyphusHBaseAutoConfiguration::class])
class SisyphusHBaseAutoConfiguration
