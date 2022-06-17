package com.bybutter.sisyphus.project.gradle.deploy.k8s

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class KubernetesDeployTask : DefaultTask() {
    @get:Input
    lateinit var environment: KubernetesEnvironment

    @get:Input
    lateinit var tag: String

    @TaskAction
    fun run() {
        environment.resources().forEach {
            when (it.kind.lowercase()) {
                "deployment", "deployments", "deploy", "deploys" -> {
                    DeploymentDeployer(environment, tag, it, logger).deploy()
                }
                "pod", "pods" -> {
                    PodDeployer(environment, tag, it, logger).deploy()
                }
                "statefulset", "statefulsets" -> {
                    StatefulSetDeployer(environment, tag, it, logger).deploy()
                }
                "job", "jobs" -> {
                    JobDeployer(environment, tag, it, logger).deploy()
                }
                "cornjob", "cornjobs" -> {
                    CronJobDeployer(environment, tag, it, logger).deploy()
                }
                "deamonset", "deamonsets" -> {
                    DaemonSetDeployer(environment, tag, it, logger).deploy()
                }
                else -> throw IllegalArgumentException("Unsupported kind: ${it.kind}")
            }
        }
    }
}
