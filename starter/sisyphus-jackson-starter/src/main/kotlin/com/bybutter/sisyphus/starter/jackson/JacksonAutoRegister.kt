package com.bybutter.sisyphus.starter.jackson

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.type.AnnotationMetadata

class JacksonAutoRegister : ImportBeanDefinitionRegistrar {
    override fun registerBeanDefinitions(importingClassMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        for (module in ObjectMapper.findModules()) {
            val beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(Module::class.java) {
                module
            }

            registry.registerBeanDefinition(
                "jackson:spi:module:${module.moduleName}",
                beanDefinitionBuilder.beanDefinition
            )
        }
    }
}
