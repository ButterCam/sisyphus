package com.bybutter.sisyphus.project.gradle.threepart

import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage
import com.bmuschko.gradle.docker.tasks.image.DockerSaveImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.bmuschko.gradle.docker.tasks.image.Dockerfile.Instruction
import com.bybutter.sisyphus.project.gradle.SisyphusExtension
import com.bybutter.sisyphus.project.gradle.getJavaMajorVersion
import com.bybutter.sisyphus.project.gradle.threepart.docker.ExtractBootJarLayer
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Sync
import org.gradle.util.internal.GUtil
import org.springframework.boot.gradle.tasks.bundling.BootJar

class SisyphusDockerPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.withPlugin("application") {
            target.pluginManager.apply("com.bmuschko.docker-remote-api")

            val sisyphus = target.extensions.getByType(SisyphusExtension::class.java)
            val docker = target.extensions.getByName("docker") as ExtensionAware
            val sisyphusDocker =
                docker.extensions.create("sisyphus", SisyphusDockerExtension::class.java, target.objects, sisyphus)

            target.afterEvaluate {
                val bootJar = target.tasks.findByName("bootJar") as? BootJar ?: return@afterEvaluate
                registerExtractLayerTask(target, bootJar)
                registerDockerfileTask(target, sisyphusDocker, bootJar)
                registerSyncDockerContent(target)
                registerDockerBuild(target, sisyphusDocker)
                registerDockerPush(target, sisyphusDocker, sisyphus)
                registerDockerSave(target)
            }
        }
    }

    private fun registerExtractLayerTask(target: Project, bootJar: BootJar) {
        target.tasks.register("extractBootJarLayer", ExtractBootJarLayer::class.java) {
            it.dependsOn(bootJar)
            it.bootJars = bootJar.outputs.files
            it.outputDirectory = target.buildDir.resolve("tmp/extractedBootJarLayer")
        }
    }

    private fun registerDockerfileTask(target: Project, sisyphusDocker: SisyphusDockerExtension, bootJar: BootJar) {
        target.tasks.register("dockerfile", Dockerfile::class.java) {
            it.arg("PROJECT_NAME")
            it.arg("PROJECT_VERSION")
            it.from(sisyphusDocker.baseImage.get())
            it.environmentVariable("PROJECT_NAME", "\$PROJECT_NAME")
            it.environmentVariable("PROJECT_VERSION", "\$PROJECT_VERSION")
            sisyphusDocker.env.get().forEach { (key, value) ->
                it.environmentVariable(key, value)
            }

            it.exposePort(sisyphusDocker.ports)

            sisyphusDocker.baseInstructions.get().forEach { instruction ->
                it.instructions.add(instruction)
            }

            bootJar.layered.layerOrder.forEach { layer ->
                it.copyFile("$layer/", "./")
                it.runCommand("true")
            }

            sisyphusDocker.instructions.get().forEach { instruction ->
                it.instructions.add(instruction)
            }

            it.entryPoint(
                "java",
                *sisyphusDocker.jvmArgs.get().toTypedArray(),
                "org.springframework.boot.loader.JarLauncher"
            )
        }
    }

    private fun registerSyncDockerContent(target: Project) {
        target.tasks.register("dockerSync", Sync::class.java) {
            val extractBootJar = target.tasks.named("extractBootJarLayer").get() as ExtractBootJarLayer
            it.dependsOn(extractBootJar)
            it.dependsOn(target.tasks.named("dockerfile"))
            it.group = "docker"

            it.destinationDir = target.buildDir.resolve("docker")
            it.from(extractBootJar.outputDirectory)
            var project: Project? = target
            while (project != null) {
                it.from(project.projectDir.resolve("docker"))
                project = project.parent
            }
            it.preserve {
                it.include("Dockerfile")
            }
        }
    }

    private fun registerDockerBuild(target: Project, sisyphusDocker: SisyphusDockerExtension) {
        target.tasks.register("dockerBuild", DockerBuildImage::class.java) {
            it.dependsOn(target.tasks.named("dockerSync"))
            it.group = "docker"
            it.buildArgs.put("PROJECT_NAME", target.name)
            it.buildArgs.put("PROJECT_VERSION", target.version.toString())
            it.platform.set(sisyphusDocker.platform)
            it.images.add("${target.name}:${target.version}")
            it.images.addAll(sisyphusDocker.images)
        }
    }

    private fun registerDockerPush(
        target: Project,
        sisyphusDocker: SisyphusDockerExtension,
        sisyphus: SisyphusExtension
    ) {
        val registries = sisyphus.dockerPublishRegistries.get().associate {
            it to sisyphus.repositories.getting(it).orNull
        }

        val baseName = "${target.name}:${target.version}"

        sisyphusDocker.images.get().forEach { image ->
            val repository = registries.entries.firstOrNull {
                val url = it.value?.url ?: return@firstOrNull false
                image == "$url/$baseName"
            } ?: return@forEach

            target.tasks.register("dockerPush" + GUtil.toCamelCase(repository.key), DockerPushImage::class.java) {
                it.dependsOn(target.tasks.named("dockerBuild"))
                it.group = "docker"
                it.images.set(listOf(image))
                it.registryCredentials {
                    it.url.set(repository.value?.url)
                    it.username.set(repository.value?.username)
                    it.password.set(repository.value?.password)
                }
            }
        }

        target.tasks.register("dockerPushAll", DefaultTask::class.java) {
            it.dependsOn(target.tasks.withType(DockerPushImage::class.java))
            it.group = "docker"
        }
    }

    private fun registerDockerSave(target: Project) {
        target.tasks.register("dockerSave", DockerSaveImage::class.java) {
            it.dependsOn(target.tasks.named("dockerBuild"))
            it.group = "docker"
            it.images.add("${target.name}:${target.version}")
            it.useCompression.set(true)
            it.destFile.set {
                if (it.useCompression.orNull == true) {
                    target.buildDir.resolve("distributions/${target.name}-${target.version}-docker.tgz")
                } else {
                    target.buildDir.resolve("distributions/${target.name}-${target.version}-docker.tar")
                }
            }
        }
    }
}

open class SisyphusDockerExtension(factory: ObjectFactory, sisyphus: SisyphusExtension) {
    val platform: Property<String> = factory.property(String::class.java)
    val baseImage: Property<String> = factory.property(String::class.java)
    val ports: ListProperty<Int> = factory.listProperty(Int::class.java).empty()
    val images: SetProperty<String> = factory.setProperty(String::class.java).empty()
    val jvmArgs: ListProperty<String> = factory.listProperty(String::class.java).empty()
    val env: MapProperty<String, String> = factory.mapProperty(String::class.java, String::class.java).empty()
    val baseInstructions: ListProperty<Instruction> = factory.listProperty(Instruction::class.java).empty()
    val instructions: ListProperty<Instruction> = factory.listProperty(Instruction::class.java).empty()

    init {
        when (val version = getJavaMajorVersion()) {
            in 1..7,
            in 9..10,
            in 12..16,
            null -> {
            }

            else -> {
                baseImage.set("amazoncorretto:$version")
            }
        }

        val baseName = "${sisyphus.project.name}:${sisyphus.project.version}"

        images.set(
            sisyphus.dockerPublishRegistries.map {
                it.mapNotNull {
                    sisyphus.repositories.getting(it).orNull
                }.map {
                    "${it.url}/$baseName"
                }
            }
        )
    }

    fun baseInstructions(configure: ListProperty<Instruction>.() -> Unit) {
        configure(baseInstructions)
    }

    fun instructions(configure: ListProperty<Instruction>.() -> Unit) {
        configure(instructions)
    }

    fun ListProperty<Instruction>.copyFile(source: String, destination: String) {
        add(Dockerfile.CopyFileInstruction(Dockerfile.CopyFile(source, destination)))
    }

    fun ListProperty<Instruction>.addFile(source: String, destination: String) {
        add(Dockerfile.AddFileInstruction(Dockerfile.File(source, destination)))
    }

    fun ListProperty<Instruction>.runCommand(command: String) {
        add(Dockerfile.RunCommandInstruction(command))
    }
}
