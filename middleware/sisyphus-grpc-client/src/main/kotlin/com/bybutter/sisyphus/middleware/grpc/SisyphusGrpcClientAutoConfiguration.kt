package com.bybutter.sisyphus.middleware.grpc

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [SisyphusGrpcClientAutoConfiguration::class])
class SisyphusGrpcClientAutoConfiguration
