package com.bybutter.sisyphus.protobuf.gradle

open class ProtobufExtension {
    private val _configs: MutableMap<String, ProtoGeneratingConfig> = mutableMapOf()
    private val _packageMapping = mutableMapOf<String, String>()
    private var _plugins = ProtoCompilerPlugins.default

    val mapping: Map<String, String> get() = _packageMapping

    val plugins: ProtoCompilerPlugins get() = _plugins

    var source = true

    var autoGenerating = true

    fun sourceSet(name: String, block: ProtoGeneratingConfig.() -> Unit = {}): ProtoGeneratingConfig {
        val config = _configs.getOrPut(name) { ProtoGeneratingConfig() }
        config.block()
        return config
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
