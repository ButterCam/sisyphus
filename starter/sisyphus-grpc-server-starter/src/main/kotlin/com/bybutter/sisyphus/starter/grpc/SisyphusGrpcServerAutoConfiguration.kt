package com.bybutter.sisyphus.starter.grpc

import com.bybutter.sisyphus.middleware.grpc.SisyphusGrpcClientAutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.env.Environment

@AutoConfiguration(
    before = [SisyphusGrpcClientAutoConfiguration::class],
)
@ComponentScan(basePackageClasses = [SisyphusGrpcServerAutoConfiguration::class])
class SisyphusGrpcServerAutoConfiguration : EnvironmentAware {
    private lateinit var environment: Environment

    @Bean
    fun serviceConfig(): ServiceConfig {
        return ServiceConfig(environment.getProperty("server.grpc.port", Int::class.java, 9090))
    }

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }
}
