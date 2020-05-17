package com.bybutter.sisyphus.spi

import com.bybutter.sisyphus.reflect.instance
import com.bybutter.sisyphus.reflect.uncheckedCast

/**
 * Java SPI(Service Provider Interface) like [ServiceLoader],
 * but more lightweight and support provider and Kotlin Object.
 */
object ServiceLoader {
    /**
     * Load services which provided specified interface.
     */
    fun <T> load(clazz: Class<T>): List<T> {
        return load(clazz, clazz.classLoader)
    }

    /**
     * Load services which provided specified interface with [ClassLoader].
     */
    fun <T> load(clazz: Class<T>, loader: ClassLoader): List<T> {
        val result = loader.getResources("META-INF/services/${clazz.name}")
                .asSequence().flatMap {
                    it.openStream().use {
                        it.reader().readLines().asSequence()
                    }
                }.map {
                    it.trim()
                }.filter {
                    it.isNotBlank()
                }.map {
                    Class.forName(it).instance().uncheckedCast<T>()
                }.toMutableList()

        if (Ordered::class.java.isAssignableFrom(clazz)) {
            result.sortBy {
                it as Ordered
            }
        }
        return result
    }
}
