package com.bybutter.sisyphus.starter.grpc

import com.bybutter.sisyphus.middleware.grpc.ChannelBuilderInterceptor
import com.bybutter.sisyphus.middleware.grpc.ClientRegistrar
import com.bybutter.sisyphus.middleware.grpc.ManagedChannelLifecycle
import com.bybutter.sisyphus.middleware.grpc.SisyphusGrpcClientAutoConfiguration
import io.grpc.ManagedChannelBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
@AutoConfigureBefore(SisyphusGrpcClientAutoConfiguration::class)
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
