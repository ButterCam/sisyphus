package org.gradle.kotlin.dsl

import org.gradle.api.artifacts.Dependency
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.HasConfigurableAttributes

enum class SisyphusDevelopmentLayer {
    /**
     * Just for implementation development, it will disable all internal module version replace.
     */
    IMPLEMENTATION,

    /**
     * Layer 1, Api schema and proto development
     */
    API,

    /**
     * Layer 2, Middleware and internal component development, include [API]
     */
    PLATFORM,

    /**
     * Layer 3, Framework development, include [API], [PLATFORM]
     */
    FRAMEWORK;

    companion object {
        val attribute = Attribute.of(SisyphusDevelopmentLayer::class.java)
    }
}

infix fun Dependency.layer(layer: SisyphusDevelopmentLayer): Dependency {
    if (this is HasConfigurableAttributes<*>) {
        this.attributes {
            it.attribute(SisyphusDevelopmentLayer.attribute, layer)
        }
    }

    return this
}
