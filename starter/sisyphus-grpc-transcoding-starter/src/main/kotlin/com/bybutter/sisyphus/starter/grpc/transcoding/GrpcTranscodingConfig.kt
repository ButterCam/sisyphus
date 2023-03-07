package com.bybutter.sisyphus.starter.grpc.transcoding

import com.bybutter.sisyphus.spring.BeanUtils
import com.bybutter.sisyphus.starter.grpc.LocalClientRepository
import com.bybutter.sisyphus.starter.grpc.ServiceRegistrar
import com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.SwaggerAuthorizer
import com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.SwaggerProperty
import com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.SwaggerRouterFunction
import io.grpc.Channel
import io.grpc.Server
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.env.Environment
import org.springframework.core.type.AnnotationMetadata
import org.springframework.http.HttpMethod
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.function.server.RouterFunction

/**
 * Config of gRPC transcoding, it will register webflux router and CORS filter based on the gRPC server.
 *
 * It imported by [EnableHttpToGrpcTranscoding] annotation, and it will found all services registered by
 * [ServiceRegistrar], create [TranscodingRouterFunction] and register into spring context for handling
 * HTTP requests.
 *
 * Also, CORS requests have been supported too, it will analyze service and register [CorsWebFilter] based
 * on [TranscodingCorsConfigurationSource] into spring context.
 */
@EnableConfigurationProperties(SwaggerProperty::class)
class GrpcTranscodingConfig : ImportBeanDefinitionRegistrar, EnvironmentAware {
    private lateinit var environment: Environment

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    private val swaggerProperty by lazy {
        Binder.get(environment)
            .bind("swagger", SwaggerProperty::class.java)
            .orElse(null) ?: SwaggerProperty()
    }

    override fun registerBeanDefinitions(importingClassMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        // Find the [EnableHttpToGrpcTranscoding] annotation.
        val enableAnnotation =
            importingClassMetadata.getAnnotationAttributes(EnableHttpToGrpcTranscoding::class.java.name)
                ?: return
        // Get the enabled transcoding service in [EnableHttpToGrpcTranscoding] annotation.
        val enableServices = (enableAnnotation[EnableHttpToGrpcTranscoding::services.name] as? Array<String>)?.asList()
            ?: listOf()
        registerSwaggerRouterFunction(registry, enableServices)
        registerRouterFunction(registry, enableServices)
        registerTranscodingCorsConfigSource(registry, enableServices)
        registerSwaggerCorsConfigSource(registry)
    }

    /**
     * Register router function bean definition to spring context.
     *
     * @param registry BeanDefinitionRegistry the [BeanDefinitionRegistry] for registering.
     * @param enableServices Collection<String> the name of services which need to enable gRPC transcoding.
     * Empty list for all supported services.
     */
    private fun registerRouterFunction(registry: BeanDefinitionRegistry, enableServices: Collection<String>) {
        val definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RouterFunction::class.java) {
            val server =
                (registry as ConfigurableListableBeanFactory).getBean(ServiceRegistrar.QUALIFIER_AUTO_CONFIGURED_GRPC_SERVER) as Server

            val channel =
                (registry as ConfigurableListableBeanFactory).getBean(LocalClientRepository.QUALIFIER_AUTO_CONFIGURED_GRPC_IN_PROCESS_CHANNEL) as Channel

            val rules = exportTranscodingRules(
                BeanUtils.getSortedBeans(registry, TranscodingRouterRuleExporter::class.java).values,
                server,
                enableServices
            )

            TranscodingRouterFunction(rules, channel)
        }
        definitionBuilder.addDependsOn(QUALIFIER_AUTO_CONFIGURED_GRPC_SWAGGER_ROUTER_FUNCTION)
        definitionBuilder.addDependsOn(LocalClientRepository.QUALIFIER_AUTO_CONFIGURED_GRPC_IN_PROCESS_CHANNEL)
        registry.registerBeanDefinition(
            QUALIFIER_AUTO_CONFIGURED_GRPC_TRANSCODING_ROUTER_FUNCTION,
            definitionBuilder.beanDefinition
        )
    }

    /**
     * Register swagger router function bean definition to spring context.
     */
    private fun registerSwaggerRouterFunction(registry: BeanDefinitionRegistry, enableServices: Collection<String>) {
        BeanDefinitionBuilder.genericBeanDefinition(SwaggerAuthorizer::class.java)
        val definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RouterFunction::class.java) {
            val server =
                (registry as ConfigurableListableBeanFactory).getBean(ServiceRegistrar.QUALIFIER_AUTO_CONFIGURED_GRPC_SERVER) as Server
            SwaggerRouterFunction(
                server,
                enableServices,
                swaggerProperty,
                (registry as ConfigurableListableBeanFactory).getBeansOfType(SwaggerAuthorizer::class.java).values.toList()
            )
        }
        registry.registerBeanDefinition(
            QUALIFIER_AUTO_CONFIGURED_GRPC_SWAGGER_ROUTER_FUNCTION,
            definitionBuilder.beanDefinition
        )
    }

    /**
     * Register gRPC transcoding CORS config source bean definition to spring context.
     *
     * @param enableServices Collection<String> the name of services which need to enable CORS in transcoding.
     * Empty list for all supported services.
     */
    private fun registerTranscodingCorsConfigSource(
        registry: BeanDefinitionRegistry,
        enableServices: Collection<String>
    ) {
        val definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(CorsConfigurationSource::class.java) {
            val server =
                (registry as ConfigurableListableBeanFactory).getBean(ServiceRegistrar.QUALIFIER_AUTO_CONFIGURED_GRPC_SERVER) as Server

            val rules = exportTranscodingRules(
                BeanUtils.getSortedBeans(registry, TranscodingRouterRuleExporter::class.java).values,
                server,
                enableServices
            )

            // Create CORS config source for transcoding.
            TranscodingCorsConfigurationSource(
                rules,
                // Try to get the default transcoding cors configuration factory form spring application context
                BeanUtils.getSortedBeans(registry, TranscodingCorsConfigurationInterceptor::class.java).values
            )
        }
        registry.registerBeanDefinition(
            QUALIFIER_AUTO_CONFIGURED_GRPC_TRANSCODING_CORS_CONFIG,
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
            QUALIFIER_AUTO_CONFIGURED_GRPC_SWAGGER_CORS_CONFIG,
            definitionBuilder.beanDefinition
        )
    }

    private fun exportTranscodingRules(
        exporters: Collection<TranscodingRouterRuleExporter>,
        server: Server,
        enableServices: Collection<String>
    ): List<TranscodingRouterRule> {
        val enableServices = enableServices.toSet()
        val rules = mutableListOf<TranscodingRouterRule>()
        for (exporter in exporters) {
            exporter.export(server, enableServices, rules)
        }
        return rules
    }

    companion object {
        /**
         * Bean name for registered transcoding router function, you can use it to refer it.
         */
        const val QUALIFIER_AUTO_CONFIGURED_GRPC_TRANSCODING_ROUTER_FUNCTION = "sisyphus:grpc:transcoding-router"

        /**
         * Bean name for registered swagger router function, you can use it to refer it.
         */
        const val QUALIFIER_AUTO_CONFIGURED_GRPC_SWAGGER_ROUTER_FUNCTION = "sisyphus:grpc:swagger-router"

        /**
         * Bean name for registered transcoding CORS filter, you can use it to refer it.
         */
        const val QUALIFIER_AUTO_CONFIGURED_GRPC_TRANSCODING_CORS_CONFIG = "sisyphus:grpc:transcoding-cors"

        /**
         * Bean name for registered transcoding CORS filter, you can use it to refer it.
         */
        const val QUALIFIER_AUTO_CONFIGURED_GRPC_SWAGGER_CORS_CONFIG = "sisyphus:grpc:swagger-cors"
    }
}
