package com.bybutter.sisyphus.middleware.grpc

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.ComponentScan

@AutoConfiguration
@ComponentScan(basePackageClasses = [SisyphusGrpcClientAutoConfiguration::class])
class SisyphusGrpcClientAutoConfiguration
