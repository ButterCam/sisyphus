package com.bybutter.sisyphus.starter.test

import com.bybutter.sisyphus.test.SisyphusTestEngineContext
import com.bybutter.sisyphus.test.descriptor.SisyphusTestDescriptor
import com.bybutter.sisyphus.test.extension.AfterTest
import com.bybutter.sisyphus.test.extension.BeforeTest
import com.bybutter.sisyphus.test.extension.SelectorResolver
import org.junit.platform.engine.discovery.ClassSelector
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ApplicationContext

class SpringExtension : BeforeTest, AfterTest, SelectorResolver {
    private var application: Class<*>? = null
    private var context: ApplicationContext? = null

    override fun beforeTest(
        context: SisyphusTestEngineContext,
        descriptor: SisyphusTestDescriptor,
    ) {
        this.context =
            application?.let {
                SpringApplication.run(it)
            }
    }

    override fun afterTest(
        context: SisyphusTestEngineContext,
        descriptor: SisyphusTestDescriptor,
    ) {
        this.context?.let {
            SpringApplication.exit(it)
        }
    }

    override fun resolve(
        selector: ClassSelector,
        context: org.junit.platform.engine.support.discovery.SelectorResolver.Context,
    ): org.junit.platform.engine.support.discovery.SelectorResolver.Resolution {
        val clazz = Class.forName(selector.className)
        if (clazz.getAnnotationsByType(SpringBootApplication::class.java).isNotEmpty()) {
            application = clazz
        }
        return super.resolve(selector, context)
    }
}
