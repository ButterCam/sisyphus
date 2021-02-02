package com.bybutter.sisyphus.protobuf.gradle

import com.bybutter.sisyphus.string.toCamelCase
import java.nio.file.Files
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.Usage
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Zip
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class ProtobufJvmPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.apply()
    }

    private fun Project.apply(): Project {
        pluginManager.apply(JavaPlugin::class.java)
        pluginManager.apply(KotlinPluginWrapper::class.java)

        val proto = extensions.create("protobuf", ProtobufExtension::class.java)

        for (sourceSet in sourceSets) {
            sourceSet.resources {
                it.srcDir("src/${sourceSet.name}/proto")
            }
            registerProtoConfig(this, proto, sourceSet)
            registerProtoApiConfig(this, proto, sourceSet)
        }

        configTest(this)
        applyKotlin(this, proto)
        return this
    }

    private fun applyKotlin(target: Project, extension: ProtobufExtension) {
        val generateProtos = target.tasks.register("generateProtos") {
            it.group = "proto"
            it.description = "Generate protos for all source set."
        }.get()

        for (sourceSet in target.sourceSets) {
            val extractTask = registerExtractProto(target, extension, sourceSet)
            val generateTask = registerGenerateProto(target, extension, sourceSet, extractTask)
            registerPackageProto(target, extension, sourceSet, extractTask)
            registerApiLintProto(target, extension, sourceSet, extractTask)
            generateProtos.dependsOn(generateTask)

            target.afterEvaluate {
                if (extension.autoGenerating) {
                    target.tasks.getByName(sourceSet.getCompileTaskName("kotlin")) {
                        val kotlinCompileTask = it as KotlinCompile
                        kotlinCompileTask.dependsOn(generateTask)
                    }
                }
            }
        }
    }

    private fun registerProtoConfig(target: Project, extension: ProtobufExtension, sourceSet: SourceSet): Configuration {
        return target.configurations.maybeCreate(sourceSet.getProtoConfigurationName()).apply {
            description = "Proto files to compile for source set '${sourceSet.name}'"
            isCanBeConsumed = false
            isCanBeResolved = true
            this.withDependencies {
                for (dependency in it) {
                    val moduleDependency = dependency as? ModuleDependency ?: continue
                    when (moduleDependency.attributes.getAttribute(Category.CATEGORY_ATTRIBUTE)?.name) {
                        Category.ENFORCED_PLATFORM, Category.REGULAR_PLATFORM, Category.LIBRARY -> {
                            dependency.isTransitive = true
                        }
                        else -> {
                            dependency.isTransitive = false
                        }
                    }
                }
            }
            attributes.attribute(Usage.USAGE_ATTRIBUTE, target.objects.named(Usage::class.java, Usage.JAVA_API))
        }
    }

    private fun registerProtoApiConfig(target: Project, extension: ProtobufExtension, sourceSet: SourceSet): Configuration {
        return target.configurations.maybeCreate(sourceSet.getProtoApiConfigurationName()).apply {
            description = "Proto dependency files for compiling of source set '${sourceSet.name}'"
            isCanBeConsumed = false
            isCanBeResolved = true
            extendsFrom(target.configurations.getByName(sourceSet.implementationConfigurationName))
            attributes.attribute(Usage.USAGE_ATTRIBUTE, target.objects.named(Usage::class.java, Usage.JAVA_API))
        }
    }

    private fun registerExtractProto(target: Project, extension: ProtobufExtension, sourceSet: SourceSet): ExtractProtoTask {
        val inputDir = target.file(extension.sourceSet(sourceSet.name).inputDir
            ?: sourceSet.protoSourcePath)
        val resourceOutputDir = target.file(extension.sourceSet(sourceSet.name).resourceOutputDir
            ?: sourceSet.protoResourceCompileOutputPath)
        val protoDir = target.file(extension.sourceSet(sourceSet.name).resourceOutputDir
            ?: sourceSet.protoTempCompileOutputPath)

        Files.createDirectories(resourceOutputDir.toPath())
        Files.createDirectories(protoDir.toPath())

        sourceSet.resources {
            it.srcDir(inputDir)
            it.srcDir(resourceOutputDir)
        }

        return target.tasks.register("extract ${sourceSet.name} protos".toCamelCase(), ExtractProtoTask::class.java) {
            if (sourceSet.isTestSourceSet) {
                val mainInputDir = target.file(extension.sourceSet("main").inputDir
                    ?: target.sourceSets.main!!.protoSourcePath)
                it.input = target.layout.files(inputDir, mainInputDir)
            } else {
                it.input = target.layout.files(inputDir)
            }
            it.resourceOutput = resourceOutputDir
            it.protoPath = protoDir
            it.protoApiConfig = target.protoApi(sourceSet)
            it.protoConfig = target.proto(sourceSet)
            it.group = "proto"
            it.description = "Extract protos for '${sourceSet.name}' source set."
            it.protobuf = extension

            it.dependsOn(target.proto(sourceSet))
            it.dependsOn(target.protoApi(sourceSet))
        }.get()
    }

    private fun registerGenerateProto(target: Project, extension: ProtobufExtension, sourceSet: SourceSet, extractTask: ExtractProtoTask): ProtoGenerateTask {
        val outputDir = target.file(extension.sourceSet(sourceSet.name).outputDir
            ?: sourceSet.protoCompileOutputPath)
        val implOutputDir = target.file(extension.sourceSet(sourceSet.name).implDir
            ?: sourceSet.protoInternalCompileOutputPath)
        val resourceOutputDir = target.file(extension.sourceSet(sourceSet.name).resourceOutputDir
            ?: sourceSet.protoResourceCompileOutputPath)

        Files.createDirectories(outputDir.toPath())
        Files.createDirectories(implOutputDir.toPath())
        Files.createDirectories(resourceOutputDir.toPath())

        sourceSet.resources {
            it.srcDir(resourceOutputDir)
        }

        sourceSet.java {
            it.srcDir(outputDir)
            it.srcDir(implOutputDir)
        }

        val generateTask = target.tasks.register("generate ${sourceSet.name} protos".toCamelCase(), ProtoGenerateTask::class.java) {
            it.protoPath = extractTask.protoPath
            it.output = outputDir
            it.implOutput = implOutputDir
            it.resourceOutput = resourceOutputDir
            it.group = "proto"
            it.description = "Generate protos for '${sourceSet.name}' source set."
            it.protobuf = extension

            it.dependsOn(extractTask)
        }.get()

        target.extensions.findByType(IdeaModel::class.java)?.apply {
            module.generatedSourceDirs.add(outputDir)
            module.generatedSourceDirs.add(implOutputDir)
        }

        target.tasks.getByName(sourceSet.getCompileTaskName("kotlin")) {
            val kotlinCompileTask = it as KotlinCompile

            kotlinCompileTask.source(outputDir)
            kotlinCompileTask.source(implOutputDir)
        }

        return generateTask
    }

    private fun registerPackageProto(target: Project, extension: ProtobufExtension, sourceSet: SourceSet, extractTask: ExtractProtoTask): Zip? {
        if (!sourceSet.isMainSourceSet) return null

        val protoDir = target.file(extension.sourceSet(sourceSet.name).resourceOutputDir
            ?: sourceSet.protoTempCompileOutputPath)

        return target.tasks.register("protoZip", Zip::class.java) {
            it.from(protoDir)
            it.group = "build"
            it.description = "Package all protos and proto meta files of 'main' source set into a zip."
            it.dependsOn(extractTask)
        }.get()
    }

    private fun registerApiLintProto(target: Project, extension: ProtobufExtension, sourceSet: SourceSet, extractTask: ExtractProtoTask): ProtobufApiLintTask {
        return target.tasks.register("${sourceSet.name} api lint".toCamelCase(), ProtobufApiLintTask::class.java) {
            it.protoPath = extractTask.protoPath
            it.protobuf = extension
            it.group = "api"
            it.description = "Apilint for '${sourceSet.name}' source set."

            it.source(extractTask.protoPath)
            it.dependsOn(extractTask)
        }.get()
    }

    private fun configTest(target: Project) {
        val testSourceSet = target.sourceSets.test ?: return
        val mainSourceSet = target.sourceSets.main ?: return

        target.protoApi(testSourceSet).extendsFrom(target.proto(mainSourceSet))
    }

    internal val Project.sourceSets get() = project.extensions.getByType(SourceSetContainer::class.java)

    internal val SourceSetContainer.main get() = findByName("main")

    internal val SourceSetContainer.test get() = findByName("test")

    internal val SourceSet.isMainSourceSet: Boolean get() = this.name == "main"

    internal val SourceSet.isTestSourceSet: Boolean get() = this.name == "test"

    internal val SourceSet.protoSourcePath: String get() = "src/$name/proto"

    internal val SourceSet.protoCompileOutputPath: String get() = "build/generated/source/proto/$name"

    internal val SourceSet.protoInternalCompileOutputPath: String get() = "build/generated/source/proto-internal/$name"

    internal val SourceSet.protoResourceCompileOutputPath: String get() = "build/generated/resources/proto-meta/$name"

    internal val SourceSet.protoTempCompileOutputPath: String get() = "build/tmp/proto/$name"

    internal fun SourceSet.getProtoApiConfigurationName(): String {
        return buildString {
            if (!this@getProtoApiConfigurationName.isMainSourceSet) {
                append(this@getProtoApiConfigurationName.name)
                append(' ')
            }
            append("proto api")
        }.toCamelCase()
    }

    internal fun SourceSet.getProtoConfigurationName(): String {
        return buildString {
            if (!this@getProtoConfigurationName.isMainSourceSet) {
                append(this@getProtoConfigurationName.name)
                append(' ')
            }
            append("proto")
        }.toCamelCase()
    }

    internal fun Project.protoApi(sourceSet: SourceSet): Configuration {
        return this.configurations.getByName(sourceSet.getProtoApiConfigurationName())
    }

    internal fun Project.proto(sourceSet: SourceSet): Configuration {
        return this.configurations.getByName(sourceSet.getProtoConfigurationName())
    }
}
