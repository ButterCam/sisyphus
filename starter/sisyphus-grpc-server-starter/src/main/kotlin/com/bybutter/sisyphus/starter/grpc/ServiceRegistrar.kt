package com.bybutter.sisyphus.starter.grpc

import com.bybutter.sisyphus.middleware.grpc.RpcServiceImpl
import com.bybutter.sisyphus.spring.BeanUtils
import com.bybutter.sisyphus.starter.grpc.support.ReflectionService
import com.bybutter.sisyphus.starter.grpc.support.ReflectionServiceAlpha
import com.bybutter.sisyphus.starter.grpc.support.RpcBinaryLog
import io.grpc.BindableService
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.ServerInterceptor
import io.grpc.ServerServiceDefinition
import io.grpc.ServerStreamTracer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class ServiceRegistrar : BeanDefinitionRegistryPostProcessor, EnvironmentAware {
    companion object {
        private val logger = LoggerFactory.getLogger(ServiceRegistrar::class.java)

        const val GRPC_PORT_PROPERTY = "grpc.port"
        const val DEFAULT_GRPC_PORT = "9090"

        const val QUALIFIER_AUTO_CONFIGURED_GRPC_SERVER = "sisyphus:grpc:server"
    }

    private lateinit var environment: Environment

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(Server::class.java) {
            var builder = ServerBuilder.forPort(environment.getProperty(GRPC_PORT_PROPERTY, DEFAULT_GRPC_PORT).toInt())

            val builderInterceptors = beanFactory.getBeansOfType(ServerBuilderInterceptor::class.java)
            for ((_, builderInterceptor) in builderInterceptors) {
                builder = builderInterceptor.intercept(builder)
            }

            val services = beanFactory.getBeansWithAnnotation(RpcServiceImpl::class.java)
            logger.info("${services.size} grpc services registered: ${services.keys.joinToString(", ")}")
            for ((_, service) in services) {
                builder = when (service) {
                    is BindableService -> {
                        builder.addService(service)
                    }
                    is ServerServiceDefinition -> {
                        builder.addService(service)
                    }
                    else -> {
                        throw IllegalArgumentException("Grpc service implement must inherit from 'BindableService' or 'ServerServiceDefinition'.")
                    }
                }
            }

            val interceptors = BeanUtils.getBeans<ServerInterceptor>(beanFactory)
            for ((_, interceptor) in interceptors) {
                builder = builder.intercept(interceptor)
            }

            val tracerFactories = BeanUtils.getBeans<ServerStreamTracer.Factory>(beanFactory)
            for ((_, factory) in tracerFactories) {
                builder = builder.addStreamTracerFactory(factory)
            }

            builder.setBinaryLog(RpcBinaryLog())
            builder.addService(ReflectionServiceAlpha())
            builder.addService(ReflectionService())

            builder.build()
        }

        (beanFactory as BeanDefinitionRegistry).registerBeanDefinition(QUALIFIER_AUTO_CONFIGURED_GRPC_SERVER, definitionBuilder.beanDefinition)
    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
    }
}
