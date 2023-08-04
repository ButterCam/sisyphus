package com.bybutter.sisyphus.spring

import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.ConfigurationClassPostProcessor
import org.springframework.core.Conventions
import org.springframework.core.Ordered
import org.springframework.core.annotation.AnnotationUtils

/**
 * BeanUtils for spring framework.
 */
object BeanUtils {

    inline fun <reified T> getBeans(beanFactory: ListableBeanFactory): Map<String, T> {
        return getBeans(beanFactory, T::class.java)
    }

    /**
     * Get beans of specified type, if [beanFactory] is [ConfigurableBeanFactory] or [ApplicationContext] this function will return sorted beans.
     */
    fun <T> getBeans(beanFactory: ListableBeanFactory, type: Class<T>): Map<String, T> {
        val factory = when (beanFactory) {
            is ApplicationContext -> beanFactory.autowireCapableBeanFactory
            else -> beanFactory
        }

        if (factory is ConfigurableListableBeanFactory) {
            return getSortedBeans(factory, type)
        }

        return getBeansWithCondition(beanFactory, type) { _, _ ->
            true
        }
    }

    fun <T> getSortedBeans(beanFactory: ConfigurableListableBeanFactory, type: Class<T>): Map<String, T> {
        return getSortedBeansWithCondition(beanFactory, type) { _, _, _ ->
            true
        }
    }

    inline fun <reified T> getBeansWithAnnotation(
        beanFactory: ListableBeanFactory,
        annotation: Class<out Annotation>
    ): Map<String, T> {
        return getBeansWithAnnotation(beanFactory, T::class.java, annotation)
    }

    /**
     * Get beans of specified type and annotation, if [beanFactory] is [ConfigurableBeanFactory] or [ApplicationContext] this function will return sorted beans.
     */
    fun <T> getBeansWithAnnotation(
        beanFactory: ListableBeanFactory,
        type: Class<T>,
        annotation: Class<out Annotation>
    ): Map<String, T> {
        val factory = when (beanFactory) {
            is ApplicationContext -> beanFactory.autowireCapableBeanFactory
            else -> beanFactory
        }

        if (factory is ConfigurableListableBeanFactory) {
            return getSortedBeansWithAnnotation(factory, type, annotation)
        }

        return getBeansWithCondition(beanFactory, type) { name, f ->
            val beanType = f.getType(name) ?: return@getBeansWithCondition false
            AnnotationUtils.getAnnotation(beanType, annotation) != null
        }
    }

    fun <T> getSortedBeansWithAnnotation(
        beanFactory: ConfigurableListableBeanFactory,
        type: Class<T>,
        annotation: Class<out Annotation>
    ): Map<String, T> {
        return getSortedBeansWithCondition(beanFactory, type) { _, _, definition ->
            val beanType = Class.forName(definition.beanClassName)
            AnnotationUtils.getAnnotation(beanType, annotation) != null
        }
    }

    /**
     * Get beans of specified type and annotation, if [beanFactory] is [ConfigurableBeanFactory] or [ApplicationContext] this function will return sorted beans.
     */
    fun <T> getBeansWithCondition(
        beanFactory: ListableBeanFactory,
        type: Class<T>,
        condition: (String, ListableBeanFactory) -> Boolean
    ): Map<String, T> {
        val factory = when (beanFactory) {
            is ApplicationContext -> beanFactory.autowireCapableBeanFactory
            else -> beanFactory
        }

        if (factory is ConfigurableListableBeanFactory) {
            return getSortedBeansWithCondition(factory, type) { name, _, _ ->
                condition(name, beanFactory)
            }
        }

        val names = beanFactory.getBeanNamesForType(type)
        val result = mutableMapOf<String, T>()

        for (name in names) {
            if (condition(name, beanFactory)) {
                result[name] = beanFactory.getBean(name, type)
            }
        }

        return result
    }

    fun <T> getSortedBeansWithCondition(
        beanFactory: ConfigurableListableBeanFactory,
        type: Class<T>,
        condition: (String, ConfigurableBeanFactory, BeanDefinition) -> Boolean
    ): Map<String, T> {
        val names = beanFactory.getBeanNamesForType(type)
        val beans = mutableMapOf<String, BeanDefinition>()

        for (name in names) {
            val beanDefinition = beanFactory.getMergedBeanDefinition(name)
            if (condition(name, beanFactory, beanDefinition)) {
                beans[name] = beanDefinition
            }
        }

        return beans.toSortedMap(BeanDefinitionOrderComparer(beans)).mapValues {
            @Suppress("UNCHECKED_CAST")
            beanFactory.getBean(it.key) as T
        }
    }
}

private class BeanDefinitionOrderComparer(private val map: Map<String, BeanDefinition>) : Comparator<String> {
    companion object {
        private val ORDER_ATTRIBUTE =
            Conventions.getQualifiedAttributeName(ConfigurationClassPostProcessor::class.java, "order")
    }

    override fun compare(o1: String, o2: String): Int {
        val order = getOrder(o1).compareTo(getOrder(o2))
        if (order != 0) return order
        return o1.compareTo(o2)
    }

    private fun getOrder(beanName: String): Int {
        val bean = map[beanName] ?: return Ordered.LOWEST_PRECEDENCE
        val order = bean.getAttribute(ORDER_ATTRIBUTE) as? Int
        return order ?: Ordered.LOWEST_PRECEDENCE
    }
}
