package com.bybutter.sisyphus.starter.webflux

import com.bybutter.sisyphus.spring.BeanUtils
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.server.WebFilter

class CorsConfigurationSourceRegistrar : BeanDefinitionRegistryPostProcessor {
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        val definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(WebFilter::class.java) {
            val corsConfigSource = BeanUtils.getSortedBeans(registry as ConfigurableListableBeanFactory, CorsConfigurationSource::class.java).values
            CorsWebFilter(DelegatingCorsConfigurationSource(corsConfigSource))
        }
        registry.registerBeanDefinition("corsConfiguration", definitionBuilder.beanDefinition)
    }
}
