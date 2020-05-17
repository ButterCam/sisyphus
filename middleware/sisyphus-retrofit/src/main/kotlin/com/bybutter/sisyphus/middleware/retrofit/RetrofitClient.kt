package com.bybutter.sisyphus.middleware.retrofit

import com.bybutter.sisyphus.middleware.retrofit.converter.JacksonConverterFactory
import kotlin.reflect.KClass
import retrofit2.Converter

/**
 * Declare that this is a retrofit client.
 * When the service starts, will scan the class decorated by this annotation and inject it into the spring container.
 * @param name Client name.
 * @param url Client request address.
 * @param connectTimeout Request connection timeout, default 5000 millisecond.
 * @param converterFactory Retrofit used by converter factory KClass, default used [JacksonConverterFactory].
 * @param builderInterceptors Retrofit client builder interceptor. Through builder interceptor, builder can be changed externally in a low coupling way.
 * Interceptor must Implementation [RetrofitBuilderInterceptor].
 * @param enableCircuitBreaker Retrofit client use or not circuit breaker, default true.
 * */
@Target(AnnotationTarget.CLASS)
annotation class RetrofitClient(
    val name: String,
    val url: String,
    val connectTimeout: Long = 5000L,
    val converterFactory: Array<KClass<out Converter.Factory>> = [JacksonConverterFactory::class],
    val builderInterceptors: Array<KClass<out RetrofitBuilderInterceptor>> = [],
    val clientBuilderInterceptors: Array<KClass<out OkHttpClientBuilderInterceptor>> = [],
    val enableCircuitBreaker: Boolean = true
)
