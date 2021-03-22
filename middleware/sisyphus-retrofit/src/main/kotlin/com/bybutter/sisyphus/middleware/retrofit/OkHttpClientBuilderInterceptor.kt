package com.bybutter.sisyphus.middleware.retrofit

import okhttp3.OkHttpClient
import org.springframework.core.env.Environment

interface OkHttpClientBuilderInterceptor {
    fun intercept(
        builder: OkHttpClient.Builder,
        property: RetrofitProperty,
        environment: Environment,
        propertyPrefix: String
    ): OkHttpClient.Builder
}
