package com.bybutter.sisyphus.middleware.redis.cache.interceptor

import org.springframework.expression.EvaluationContext

interface EvaluationContextInterceptor {
    fun intercept(context: EvaluationContext): EvaluationContext
}
