package com.bybutter.sisyphus.starter.grpc

import com.bybutter.sisyphus.middleware.grpc.autoconfigure.SisyphusGrpcClientAutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.context.annotation.ComponentScan

@AutoConfigureBefore(SisyphusGrpcClientAutoConfiguration::class)
@ComponentScan(basePackageClasses = [SisyphusGrpcServerAutoConfiguration::class])
class SisyphusGrpcServerAutoConfiguration
