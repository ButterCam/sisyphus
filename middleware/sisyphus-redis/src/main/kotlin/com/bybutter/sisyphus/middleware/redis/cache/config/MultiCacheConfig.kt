package com.bybutter.sisyphus.middleware.redis.cache.config

import com.bybutter.sisyphus.middleware.redis.cache.MultiRedisCacheManager
import com.bybutter.sisyphus.middleware.redis.cache.interceptor.EvaluationContextInterceptor
import com.bybutter.sisyphus.middleware.redis.cache.interceptor.MultiCacheEvictInterceptor
import com.bybutter.sisyphus.middleware.redis.cache.interceptor.MultiCachePutInterceptor
import com.bybutter.sisyphus.middleware.redis.cache.interceptor.MultiCacheableInterceptor
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Role
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheWriter
import org.springframework.data.redis.connection.RedisConnectionFactory

@Configuration
@ConditionalOnClass(RedisConnectionFactory::class)
class MultiCacheConfig {
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    fun multiCacheableAdvisor(redisCacheManager: MultiRedisCacheManager, evaluationContextInterceptorList: List<EvaluationContextInterceptor>): MultiCacheableBeanFactorySourceAdvisor {
        val advisor = MultiCacheableBeanFactorySourceAdvisor()
        advisor.advice = MultiCacheableInterceptor(redisCacheManager, evaluationContextInterceptorList)
        return advisor
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    fun multiCachePutAdvisor(redisCacheManager: MultiRedisCacheManager, evaluationContextInterceptorList: List<EvaluationContextInterceptor>): MultiCachePutBeanFactorySourceAdvisor {
        val advisor = MultiCachePutBeanFactorySourceAdvisor()
        advisor.advice = MultiCachePutInterceptor(redisCacheManager, evaluationContextInterceptorList)
        return advisor
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    fun multiCacheEvictAdvisor(redisCacheManager: MultiRedisCacheManager, evaluationContextInterceptorList: List<EvaluationContextInterceptor>): MultiCacheEvictBeanFactorySourceAdvisor {
        val advisor = MultiCacheEvictBeanFactorySourceAdvisor()
        advisor.advice = MultiCacheEvictInterceptor(redisCacheManager, evaluationContextInterceptorList)
        return advisor
    }

    @Bean("defaultRedisCacheConfiguration")
    @ConditionalOnMissingBean
    fun defaultRedisCacheConfiguration(): RedisCacheConfiguration? {
        return RedisCacheConfiguration.defaultCacheConfig()
    }

    @Bean
    @ConditionalOnMissingBean
    fun redisCacheWriter(redisConnectionFactory: RedisConnectionFactory?): RedisCacheWriter? {
        return RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory)
    }

    @Bean("multiRedisCacheManager")
    @ConditionalOnMissingBean
    fun multiRedisCacheManager(
        redisCacheWriter: RedisCacheWriter,
        defaultRedisCacheConfiguration: RedisCacheConfiguration
    ): MultiRedisCacheManager {
        return MultiRedisCacheManager(
                redisCacheWriter,
                defaultRedisCacheConfiguration)
    }
}
