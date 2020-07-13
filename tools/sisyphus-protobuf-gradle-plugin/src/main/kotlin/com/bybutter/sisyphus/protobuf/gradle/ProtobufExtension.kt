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

    val linter = ApiLinterConfig()

    var autoGenerating = true

    fun sourceSet(name: String, block: ProtoGeneratingConfig.() -> Unit = {}): ProtoGeneratingConfig {
        val config = configs.getOrPut(name) { ProtoGeneratingConfig() }
        config.block()
        return config
    }

    fun service(block: MutableService.() -> Unit) {
        serviceConfig = serviceConfig?.invoke(block) ?: Service(block)
    }

    fun linter(block: ApiLinterConfig.() -> Unit) {
        linter.block()
    }

    fun packageMapping(proto: String, kotlin: String) {
        packageMapping[proto] = kotlin
    }

    fun packageMapping(vararg mapping: Pair<String, String>) {
        packageMapping.putAll(mapping)
    }
}

data class ApiLinterConfig(var version: String? = null, val enableRules: MutableSet<String> = mutableSetOf(), val disableRules: MutableSet<String> = mutableSetOf(), val excludeFiles: MutableSet<String> = mutableSetOf())

data class ProtoGeneratingConfig(var inputDir: String? = null, var outputDir: String? = null, var implDir: String? = null, var resourceOutputDir: String? = null)
