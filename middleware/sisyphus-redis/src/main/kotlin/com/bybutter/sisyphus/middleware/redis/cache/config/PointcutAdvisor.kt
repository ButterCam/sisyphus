package com.bybutter.sisyphus.middleware.redis.cache.config

import com.bybutter.sisyphus.middleware.redis.cache.annotation.MultiCacheEvict
import com.bybutter.sisyphus.middleware.redis.cache.annotation.MultiCachePut
import com.bybutter.sisyphus.middleware.redis.cache.annotation.MultiCacheable
import org.springframework.aop.Pointcut
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor
import org.springframework.aop.support.StaticMethodMatcherPointcut
import java.io.Serializable
import java.lang.reflect.Method

class MultiCacheableBeanFactorySourceAdvisor : AbstractBeanFactoryPointcutAdvisor() {
    override fun getPointcut(): Pointcut {
        return MultiCacheableSourcePointcut()
    }
}

class MultiCacheableSourcePointcut : StaticMethodMatcherPointcut(), Serializable {
    override fun matches(method: Method, targetClass: Class<*>): Boolean {
        return method.getAnnotation(MultiCacheable::class.java) != null
    }
}

class MultiCachePutBeanFactorySourceAdvisor : AbstractBeanFactoryPointcutAdvisor() {
    override fun getPointcut(): Pointcut {
        return MultiCachePutSourcePointcut()
    }
}

class MultiCachePutSourcePointcut : StaticMethodMatcherPointcut(), Serializable {
    override fun matches(method: Method, targetClass: Class<*>): Boolean {
        return method.getAnnotation(MultiCachePut::class.java) != null
    }
}

class MultiCacheEvictBeanFactorySourceAdvisor : AbstractBeanFactoryPointcutAdvisor() {
    override fun getPointcut(): Pointcut {
        return MultiCacheEvictSourcePointcut()
    }
}

class MultiCacheEvictSourcePointcut : StaticMethodMatcherPointcut(), Serializable {
    override fun matches(method: Method, targetClass: Class<*>): Boolean {
        return method.getAnnotation(MultiCacheEvict::class.java) != null
    }
}
