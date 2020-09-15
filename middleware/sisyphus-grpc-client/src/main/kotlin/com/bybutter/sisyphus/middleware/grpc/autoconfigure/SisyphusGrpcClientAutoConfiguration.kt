package com.bybutter.sisyphus.middleware.grpc.autoconfigure

import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackageClasses = [SisyphusGrpcClientAutoConfiguration::class])
class SisyphusGrpcClientAutoConfiguration
