package com.bybutter.sisyphus.starter.grpc

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [SisyphusGrpcServerAutoConfiguration::class])
class SisyphusGrpcServerAutoConfiguration
