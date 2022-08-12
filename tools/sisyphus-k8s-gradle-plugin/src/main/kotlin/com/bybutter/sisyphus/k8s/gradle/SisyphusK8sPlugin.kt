package com.bybutter.sisyphus.k8s.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class SisyphusK8sPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create("k8s", KubernetesExtension::class.java, target)
    }
}
