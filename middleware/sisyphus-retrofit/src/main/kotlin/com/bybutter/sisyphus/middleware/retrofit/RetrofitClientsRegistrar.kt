package com.bybutter.sisyphus.middleware.retrofit

import com.bybutter.sisyphus.reflect.instance
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retrofit.CircuitBreakerCallAdapter
import okhttp3.OkHttpClient
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.getBeansOfType
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.env.Environment
import org.springframework.core.type.AnnotationMetadata
import retrofit2.Retrofit
import java.time.Duration
import java.util.concurrent.TimeUnit

class RetrofitClientsRegistrar : ImportBeanDefinitionRegistrar, EnvironmentAware {
    private lateinit var environment: Environment

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun registerBeanDefinitions(importingClassMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        val enableAnnotation = importingClassMetadata.getAnnotationAttributes(EnableRetrofitClients::class.java.name)
            ?: return

        // Get the value of basePackageNames from the annotation.
        val basePackageNames =
            (enableAnnotation[EnableRetrofitClients::basePackageNames.name] as? Array<String>)?.asList()?.toMutableSet()
                ?: mutableSetOf()

        // Get the value of basePackageClasses from the annotation. And class as anchor, get class package.
        (enableAnnotation[EnableRetrofitClients::basePackageClasses.name] as? Array<Class<*>>)?.asList()?.map {
            basePackageNames.add(it.packageName)
        }

        basePackageNames.add(Class.forName(importingClassMetadata.className).packageName)

        // Obtain the class with RetrofitClient annotation under the specified path through reflection.
        val classes = basePackageNames.fold(mutableSetOf<Class<*>>()) { acc, s ->
            acc.addAll(Reflections(ConfigurationBuilder().forPackages(s)).getTypesAnnotatedWith(RetrofitClient::class.java))
            acc
        }

        // Get the value of clientClassNames from the annotation. Reflect class through className，add to classes.
        (enableAnnotation[EnableRetrofitClients::clientClassNames.name] as? Array<String>)?.asList()?.map {
            val clazz = Class.forName(it)
            if (clazz.getAnnotation(RetrofitClient::class.java) != null) {
                classes.add(clazz)
            } else {
                logger.warn("@RetrofitClient is not present on client class '${clazz.name}', skip register client for it.")
            }
        }

        // Get the value of clientClasses from the annotation. Add to classes.
        (enableAnnotation[EnableRetrofitClients::clientClasses.name] as? Array<Class<*>>)?.asList()?.map {
            if (it.getAnnotation(RetrofitClient::class.java) != null) {
                classes.add(it)
            } else {
                logger.warn("@RetrofitClient is not present on client class '${it.name}', skip register client for it.")
            }
        }

        if (classes.isEmpty()) return

        registerClients(registry, classes)
    }

    private fun registerClients(registry: BeanDefinitionRegistry, classes: Set<Class<*>>) {
        val beanFactory = registry as ConfigurableListableBeanFactory
        val properties = beanFactory.getBeansOfType<RetrofitProperty>().toMutableMap()
        val retrofitProperties = Binder.get(environment)
            .bind("sisyphus", RetrofitProperties::class.java)
            .orElse(null)?.retrofit ?: mapOf()

        properties += retrofitProperties

        for (clientClass in classes) {
            val annotation = clientClass.getAnnotation(RetrofitClient::class.java)
            val property = buildRetrofitProperty(annotation, properties[annotation.name])
            val propertyPrefix = "next.retrofit.${annotation.name}"

            val name = property.name ?: throw IllegalStateException("Retrofit client must have 'name'.")

            val okHttpClient = createOkHttpClient(property, propertyPrefix)
            val client = createRetrofit(property, propertyPrefix, clientClass, okHttpClient)

            beanFactory.registerSingleton(BEAN_NAME_PREFIX + name, client)
            logger.info("Retrofit client '$name(${clientClass.canonicalName})' -> '${property.url}' registered.")
        }
    }

    private fun createOkHttpClient(property: RetrofitProperty, propertyPrefix: String): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()
        if (property.connectTimeout != null) {
            clientBuilder.connectTimeout(property.connectTimeout, TimeUnit.MILLISECONDS)
        }
        return property.clientBuilderInterceptors?.fold(clientBuilder) { builder, it ->
            it.instance().intercept(builder, property, environment, propertyPrefix)
        }?.build() ?: clientBuilder.build()
    }

    private fun createRetrofit(
        property: RetrofitProperty,
        propertyPrefix: String,
        clientClass: Class<*>,
        client: OkHttpClient
    ): Any {
        val baseUrl = property.url ?: throw IllegalStateException("Retrofit client must have 'url'.")
        val retrofitBuilder = Retrofit.Builder().baseUrl(baseUrl).client(client)
        property.converterFactory?.forEach {
            retrofitBuilder.addConverterFactory(it.instance())
        }

        if (property.enableCircuitBreaker != false) {
            retrofitBuilder.addCallAdapterFactory(CircuitBreakerCallAdapter.of(buildCircuitBreaker(property)))
        }

        val retrofit = property.builderInterceptors?.fold(retrofitBuilder) { builder, it ->
            it.instance().intercept(builder, property, environment, propertyPrefix)
        }?.build() ?: retrofitBuilder.build()

        return retrofit.create(clientClass)
    }

    private fun buildRetrofitProperty(retrofitClient: RetrofitClient, property: RetrofitProperty?): RetrofitProperty {
        return RetrofitProperty(
            property?.name
                ?: retrofitClient.name,
            property?.url ?: retrofitClient.url,
            property?.connectTimeout ?: retrofitClient.connectTimeout,
            property?.converterFactory ?: retrofitClient.converterFactory.map { it.java },
            property?.builderInterceptors ?: retrofitClient.builderInterceptors.map { it.java },
            property?.clientBuilderInterceptors ?: retrofitClient.clientBuilderInterceptors.map { it.java },
            property?.enableCircuitBreaker ?: retrofitClient.enableCircuitBreaker,
            property?.circuitBreakerProperty
        )
    }

    private fun buildCircuitBreaker(property: RetrofitProperty): CircuitBreaker {
        val configBuilder = CircuitBreakerConfig.custom()
            .enableAutomaticTransitionFromOpenToHalfOpen()

        if (property.circuitBreakerProperty != null) {
            property.circuitBreakerProperty.failureRateThreshold?.let {
                configBuilder.failureRateThreshold(it)
            }
            property.circuitBreakerProperty.slowCallRateThreshold?.let {
                configBuilder.slowCallRateThreshold(it)
            }
            property.circuitBreakerProperty.slowCallDurationThreshold?.let {
                configBuilder.slowCallDurationThreshold(Duration.ofSeconds(it))
            }
            property.circuitBreakerProperty.permittedNumberOfCallsInHalfOpenState?.let {
                configBuilder.permittedNumberOfCallsInHalfOpenState(it)
            }
            property.circuitBreakerProperty.minimumNumberOfCalls?.let {
                configBuilder.minimumNumberOfCalls(it)
            }
            property.circuitBreakerProperty.waitDurationInOpenState?.let {
                configBuilder.waitDurationInOpenState(Duration.ofSeconds(it))
            }
            property.circuitBreakerProperty.automaticTransitionFromOpenToHalfOpenEnabled?.let {
                configBuilder.automaticTransitionFromOpenToHalfOpenEnabled(it)
            }
        }

        return CircuitBreakerRegistry.of(configBuilder.build())
            .circuitBreaker(CIRCUIT_BREAKER_NAME_PREFIX + property.name)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RetrofitClientsRegistrar::class.java)
        const val BEAN_NAME_PREFIX = "sisyphus:retrofit:"
        const val CIRCUIT_BREAKER_NAME_PREFIX = "retrofit:circuit-breaker:"
    }
}
