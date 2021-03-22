package com.bybutter.sisyphus.starter.grpc

import com.bybutter.sisyphus.longrunning.OperationSupport
import com.bybutter.sisyphus.middleware.grpc.RpcServiceImpl
import com.bybutter.sisyphus.spring.BeanUtils
import com.bybutter.sisyphus.starter.grpc.support.operation.Operations
import com.bybutter.sisyphus.starter.grpc.support.reflection.ReflectionService
import com.bybutter.sisyphus.starter.grpc.support.reflection.ReflectionServiceAlpha
import io.grpc.BindableService
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.ServerInterceptor
import io.grpc.ServerServiceDefinition
import io.grpc.ServerStreamTracer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.boot.web.server.Shutdown
import org.springframework.context.EnvironmentAware
import org.springframework.context.Lifecycle
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class ServiceRegistrar : BeanDefinitionRegistryPostProcessor, EnvironmentAware {
    private lateinit var environment: Environment

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(Server::class.java) {
            val config = beanFactory.getBean<ServiceConfig>()
            var builder = ServerBuilder.forPort(config.serverPort)

            val builderInterceptors = beanFactory.getBeansOfType(ServerBuilderInterceptor::class.java)
            for ((_, builderInterceptor) in builderInterceptors) {
                builder = builderInterceptor.intercept(builder)
            }

            val services = beanFactory.getBeansWithAnnotation(RpcServiceImpl::class.java)
            logger.info("${services.size} gRPC services registered: ${services.keys.joinToString(", ")}")
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
            for (interceptor in interceptors.values.reversed()) {
                builder = builder.intercept(interceptor)
            }

            val tracerFactories = BeanUtils.getBeans<ServerStreamTracer.Factory>(beanFactory)
            for ((_, factory) in tracerFactories) {
                builder = builder.addStreamTracerFactory(factory)
            }

            val operationSupports = BeanUtils.getBeans<OperationSupport>(beanFactory)
            builder.addService(Operations(operationSupports.values.toList()))
            builder.addService(ReflectionServiceAlpha())
            builder.addService(ReflectionService())

            builder.build()
        }
        (beanFactory as BeanDefinitionRegistry).registerBeanDefinition(
            QUALIFIER_AUTO_CONFIGURED_GRPC_SERVER,
            definitionBuilder.beanDefinition
        )

        val lifecycleBuilder = BeanDefinitionBuilder.genericBeanDefinition(Lifecycle::class.java) {
            val server = beanFactory.getBean(QUALIFIER_AUTO_CONFIGURED_GRPC_SERVER) as Server
            val shutdown = environment.getProperty("server.shutdown", Shutdown::class.java)
            ServerLifecycle(server, shutdown ?: Shutdown.IMMEDIATE)
        }
        (beanFactory as BeanDefinitionRegistry).registerBeanDefinition(
            QUALIFIER_AUTO_CONFIGURED_GRPC_SERVER_LIFECYCLE,
            lifecycleBuilder.beanDefinition
        )
    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ServiceRegistrar::class.java)

        const val QUALIFIER_AUTO_CONFIGURED_GRPC_SERVER = "sisyphus:grpc:server"

        const val QUALIFIER_AUTO_CONFIGURED_GRPC_SERVER_LIFECYCLE = "sisyphus:grpc:server-lifecycle"
    }
}
