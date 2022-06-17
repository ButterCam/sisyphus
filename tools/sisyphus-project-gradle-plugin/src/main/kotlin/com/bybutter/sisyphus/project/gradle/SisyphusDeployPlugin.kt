package com.bybutter.sisyphus.project.gradle

import com.bybutter.sisyphus.project.gradle.deploy.DeployingExtension
import com.bybutter.sisyphus.project.gradle.deploy.k8s.KubernetesDeployPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class SisyphusDeployPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        applyDeploying(target)
        if (isClassExist("com.palantir.gradle.docker.PalantirDockerPlugin")) {
            target.pluginManager.apply(KubernetesDeployPlugin::class.java)
        }
    }

    private fun applyDeploying(target: Project) {
        target.extensions.create("deploying", DeployingExtension::class.java, target)
    }
}
