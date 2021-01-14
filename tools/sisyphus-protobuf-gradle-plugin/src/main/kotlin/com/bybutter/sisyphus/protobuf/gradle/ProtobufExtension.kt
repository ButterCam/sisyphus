package com.bybutter.sisyphus.protobuf.gradle

import com.bybutter.sisyphus.protobuf.compiler.generator.CodeGenerator
import com.bybutter.sisyphus.reflect.instance
import kotlin.reflect.KClass

open class ProtobufExtension {
    private val _configs: MutableMap<String, ProtoGeneratingConfig> = mutableMapOf()
    private val _packageMapping = mutableMapOf<String, String>()
    private var _plugins = ProtoCompilerPlugins.default

    val mapping: Map<String, String> get() = _packageMapping

    val plugins: ProtoCompilerPlugins get() = _plugins

    val linter = ApiLinterConfig()

    var autoGenerating = true

    fun sourceSet(name: String, block: ProtoGeneratingConfig.() -> Unit = {}): ProtoGeneratingConfig {
        val config = _configs.getOrPut(name) { ProtoGeneratingConfig() }
        config.block()
        return config
    }

    fun linter(block: ApiLinterConfig.() -> Unit) {
        linter.block()
    }

    fun packageMapping(proto: String, kotlin: String) {
        _packageMapping[proto] = kotlin
    }

    fun packageMapping(vararg mapping: Pair<String, String>) {
        _packageMapping.putAll(mapping)
    }

    fun plugins(block: ProtoCompilerPlugins.() -> Unit) {
        _plugins = ProtoCompilerPlugins().apply(block)
    }
}

data class ProtoCompilerPlugins(
    val buildInPlugins: MutableSet<BuildInPlugin> = mutableSetOf(),
    val plugins: MutableList<CodeGenerator<*>> = mutableListOf()
) {
    fun spi(): ProtoCompilerPlugins {
        buildInPlugins += BuildInPlugin.GENERATORS_FROM_SPI
        return this
    }

    fun basic(): ProtoCompilerPlugins {
        buildInPlugins += BuildInPlugin.BASIC_GENERATOR
        return this
    }

    fun coroutine(): ProtoCompilerPlugins {
        buildInPlugins += BuildInPlugin.COROUTINE_SERVICE_GENERATOR
        return this
    }

    fun resourceName(): ProtoCompilerPlugins {
        buildInPlugins += BuildInPlugin.RESOURCE_NAME_GENERATOR
        return this
    }

    fun plugin(plugin: BuildInPlugin) {
        buildInPlugins += plugin
    }

    fun plugin(className: String) {
        plugin(Class.forName(className) as Class<CodeGenerator<*>>)
    }

    fun plugin(clazz: KClass<out CodeGenerator<*>>) {
        plugin(clazz.java)
    }

    fun plugin(clazz: Class<out CodeGenerator<*>>) {
        plugin(clazz.instance())
    }

    fun plugin(generator: CodeGenerator<*>) {
        plugins += generator
    }

    companion object {
        val default = ProtoCompilerPlugins().basic().coroutine().resourceName()
    }
}

data class ApiLinterConfig(
    var version: String? = null,
    val enableRules: MutableSet<String> = mutableSetOf(),
    val disableRules: MutableSet<String> = mutableSetOf(),
    val excludeFiles: MutableSet<String> = mutableSetOf()
)

data class ProtoGeneratingConfig(
    var inputDir: String? = null,
    var outputDir: String? = null,
    var implDir: String? = null,
    var resourceOutputDir: String? = null
)

enum class BuildInPlugin {
    BASIC_GENERATOR,
    COROUTINE_SERVICE_GENERATOR,
    RXJAVA_SERVICE_GENERATOR,
    RESOURCE_NAME_GENERATOR,
    GENERATORS_FROM_SPI
}
