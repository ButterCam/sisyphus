package com.bybutter.sisyphus.middleware.retrofit

import org.springframework.boot.context.properties.NestedConfigurationProperty
import retrofit2.Converter

data class RetrofitProperty(
    val name: String? = null,
    val url: String? = null,
    val connectTimeout: Long? = null,
    /**
     * Configuration classes name is required in configuration file.
     * example: [com.bybutter.sisyphus.middleware.retrofit.converter.JacksonConverterFactory].
     * */
    val converterFactory: List<Class<out Converter.Factory>>? = null,
    val builderInterceptors: List<Class<out RetrofitBuilderInterceptor>>? = null,
    val clientBuilderInterceptors: List<Class<out OkHttpClientBuilderInterceptor>>? = null,
    val enableCircuitBreaker: Boolean? = null,
    @NestedConfigurationProperty
    val circuitBreakerProperty: CircuitBreakerProperty? = null,
    val extensions: Map<String, Any> = mapOf(),
)

/**
 * Circuit breaker configuration.
 *
 * Please refer to https://resilience4j.readme.io/docs/circuitbreaker for parameter definition.
 * */
data class CircuitBreakerProperty(
    val failureRateThreshold: Float?,
    val slowCallRateThreshold: Float?,
    val slowCallDurationThreshold: Long?,
    val permittedNumberOfCallsInHalfOpenState: Int?,
    val minimumNumberOfCalls: Int?,
    val waitDurationInOpenState: Long?,
    val automaticTransitionFromOpenToHalfOpenEnabled: Boolean?,
)

data class RetrofitProperties(
    @NestedConfigurationProperty
    val retrofit: Map<String, RetrofitProperty>,
)
