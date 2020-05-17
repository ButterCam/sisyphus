package com.bybutter.sisyphus.middleware.retrofit

import org.springframework.core.env.Environment
import retrofit2.Retrofit

interface RetrofitBuilderInterceptor {
    fun intercept(builder: Retrofit.Builder, property: RetrofitProperty, environment: Environment, propertyPrefix: String): Retrofit.Builder
}
