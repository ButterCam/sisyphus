package com.bybutter.sisyphus.protobuf.gradle

import com.bybutter.sisyphus.api.Service
import com.bybutter.sisyphus.protobuf.invoke
import proto.internal.com.bybutter.sisyphus.api.MutableService

open class ProtobufExtension {
    private val configs: MutableMap<String, ProtoGeneratingConfig> = mutableMapOf()
    private val packageMapping = mutableMapOf<String, String>()
    private var serviceConfig: Service? = null

    val mapping: Map<String, String> get() = packageMapping

    val service get() = serviceConfig

    var autoGenerating = true

    fun sourceSet(name: String, block: ProtoGeneratingConfig.() -> Unit = {}): ProtoGeneratingConfig {
        val config = configs.getOrPut(name) { ProtoGeneratingConfig() }
        config.block()
        return config
    }

    fun service(block: MutableService.() -> Unit) {
        serviceConfig = serviceConfig?.invoke(block) ?: Service(block)
    }

    fun packageMapping(proto: String, kotlin: String) {
        packageMapping[proto] = kotlin
    }

    fun packageMapping(vararg mapping: Pair<String, String>) {
        packageMapping.putAll(mapping)
    }
}

data class ProtoGeneratingConfig(var inputDir: String? = null, var outputDir: String? = null, var implDir: String? = null, var resourceOutputDir: String? = null)
