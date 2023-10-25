package com.bybutter.sisyphus.k8s.gradle

import org.gradle.api.Project

internal fun Project.ensurePlugin(
    vararg ids: String,
    block: (Project) -> Unit,
): Boolean {
    for (id in ids) {
        if (!pluginManager.hasPlugin(id)) {
            pluginManager.withPlugin(id) {
                block(this)
            }
            return false
        }
    }

    return true
}

internal inline fun Project.ensurePlugin(
    id: String,
    noinline block: (Project) -> Unit,
    returnBlock: () -> Unit,
) {
    if (!pluginManager.hasPlugin(id)) {
        pluginManager.withPlugin(id) {
            block(this)
        }
        returnBlock()
    }
}

internal fun Project.tryApplyPluginClass(
    className: String,
    action: () -> Unit = {},
): Boolean {
    return try {
        val plugin = Class.forName(className)
        action()
        this.pluginManager.apply(plugin)
        true
    } catch (ex: ClassNotFoundException) {
        false
    }
}

internal fun isClassExist(className: String): Boolean {
    return try {
        Class.forName(className)
        true
    } catch (ex: ClassNotFoundException) {
        false
    }
}
