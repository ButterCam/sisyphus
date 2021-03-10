package com.bybutter.sisyphus.protobuf.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class ProtobufPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.withPlugin("com.android.base") {
            target.pluginManager.apply(ProtobufAndroidPlugin::class.java)
        }
        target.pluginManager.withPlugin("java") {
            target.pluginManager.apply(ProtobufJvmPlugin::class.java)
        }
    }
}
