package com.bybutter.sisyphus.k8s.gradle

import org.gradle.api.Project

open class KubernetesExtension(val project: Project) {
    val clusters = mutableMapOf<String, KubernetesCluster>()

    fun cluster(
        name: String,
        block: KubernetesCluster.() -> Unit,
    ) {
        val current =
            clusters.getOrPut(name) {
                KubernetesCluster(name, this)
            }
        block(current)
    }
}
