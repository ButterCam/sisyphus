package com.bybutter.sisyphus.starter.grpc

import com.bybutter.sisyphus.middleware.grpc.SisyphusGrpcClientAutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@AutoConfigureBefore(SisyphusGrpcClientAutoConfiguration::class)
@ComponentScan(basePackageClasses = [SisyphusGrpcServerAutoConfiguration::class])
class SisyphusGrpcServerAutoConfiguration
