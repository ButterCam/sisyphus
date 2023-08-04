package com.bybutter.sisyphus.starter.grpc.openapi

import com.bybutter.sisyphus.starter.grpc.ServiceRegistrar
import com.bybutter.sisyphus.starter.grpc.transcoding.EnableHttpToGrpcTranscoding
import io.grpc.Server
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.env.Environment
import org.springframework.core.type.AnnotationMetadata
import org.springframework.http.HttpMethod
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.function.server.RouterFunction

@AutoConfiguration
class ApiDocConfig : ImportBeanDefinitionRegistrar, EnvironmentAware {
    private lateinit var environment: Environment

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    private val swaggerProperty by lazy {
        Binder.get(environment).bind("openapi", ApiDocProperty::class.java).orElse(null) ?: ApiDocProperty()
    }

    override fun registerBeanDefinitions(importingClassMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        // Find the [EnableHttpToGrpcTranscoding] annotation.
        val enableAnnotation =
            importingClassMetadata.getAnnotationAttributes(EnableHttpToGrpcTranscoding::class.java.name) ?: return
        // Get the enabled transcoding service in [EnableHttpToGrpcTranscoding] annotation.
        val enableServices =
            (enableAnnotation[EnableHttpToGrpcTranscoding::services.name] as? Array<String>)?.asList() ?: listOf()
        registerSwaggerRouterFunction(registry, enableServices)
        registerSwaggerCorsConfigSource(registry)
    }

    /**
     * Register swagger router function bean definition to spring context.
     */
    private fun registerSwaggerRouterFunction(registry: BeanDefinitionRegistry, enableServices: Collection<String>) {
        val definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RouterFunction::class.java) {
            val server =
                (registry as ConfigurableListableBeanFactory).getBean(ServiceRegistrar.QUALIFIER_AUTO_CONFIGURED_GRPC_SERVER) as Server
            ApiDocRouterFunction(
                server,
                enableServices,
                swaggerProperty,
                (registry as ConfigurableListableBeanFactory).getBeansOfType(ApiDocRequestInterceptor::class.java).values.toList(),
                (registry as ConfigurableListableBeanFactory).getBeansOfType(ApiDocInterceptor::class.java).values.toList()
            )
        }
        registry.registerBeanDefinition(
            QUALIFIER_AUTO_CONFIGURED_GRPC_OPENAPI_ROUTER_FUNCTION,
            definitionBuilder.beanDefinition
        )
    }

    /**
     * Register gRPC swagger CORS config source bean definition to spring context.
     */
    private fun registerSwaggerCorsConfigSource(registry: BeanDefinitionRegistry) {
        val definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(CorsConfigurationSource::class.java) {
            UrlBasedCorsConfigurationSource().apply {
                registerCorsConfiguration(
                    swaggerProperty.path,
                    CorsConfiguration().apply {
                        addAllowedHeader(CorsConfiguration.ALL)
                        addAllowedOrigin(CorsConfiguration.ALL)
                        addAllowedMethod(HttpMethod.OPTIONS)
                        addAllowedMethod(HttpMethod.HEAD)
                        addAllowedMethod(HttpMethod.GET)
                    }
                )
            }
        }
        registry.registerBeanDefinition(
            QUALIFIER_AUTO_CONFIGURED_GRPC_OPENAPI_CORS_CONFIG,
            definitionBuilder.beanDefinition
        )
    }

    companion object {
        /**
         * Bean name for registered swagger router function, you can use it to refer it.
         */
        const val QUALIFIER_AUTO_CONFIGURED_GRPC_OPENAPI_ROUTER_FUNCTION = "sisyphus:grpc:openapi-router"

        /**
         * Bean name for registered transcoding CORS filter, you can use it to refer it.
         */
        const val QUALIFIER_AUTO_CONFIGURED_GRPC_OPENAPI_CORS_CONFIG = "sisyphus:grpc:openapi-cors"
    }
}
