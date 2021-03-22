package com.bybutter.sisyphus.test

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.logging.LoggerFactory

class TestReporter {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private fun getName(context: ExtensionContext): String {
        val annotations = context.requiredTestMethod.declaredAnnotations
        var value = context.requiredTestMethod.name
        annotations.filterIsInstance<DisplayName>()
                .forEach { value = it.value }
        return value
    }
}