package com.bybutter.sisyphus.middleware.grpc.client.kubernetes

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [SisyphusGrpcKubernetesClientAutoConfiguration::class])
class SisyphusGrpcKubernetesClientAutoConfiguration
