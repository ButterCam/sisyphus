package com.bybutter.sisyphus.protobuf.gradle

import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.gradle.AbstractAppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.bybutter.sisyphus.string.toCamelCase
import java.nio.file.Files
import org.gradle.api.DomainObjectSet
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.Usage
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceTask
import org.gradle.plugins.ide.idea.model.IdeaModel

class ProtobufAndroidPlugin : Plugin<Project> {
    private lateinit var project: Project

    override fun apply(target: Project) {
        project = target

        val proto = target.extensions.create("protobuf", ProtobufExtension::class.java)

        project.android.sourceSets.forEach { androidSourceSet ->
            registerProtoConfig(target, proto, androidSourceSet)
            registerProtoApiConfig(target, proto, androidSourceSet)
        }

        configTest(target)
        applyKotlin(target, proto)
    }

    private fun applyKotlin(target: Project, extension: ProtobufExtension) {
        target.afterEvaluate {
            nonTestVariants.forEach { variant ->
                val generateProtos = target.tasks.register("generateVariant${variant.name.capitalize()}Protos") {
                    it.group = "proto"
                    it.description = "Generate protos for all source set."
                }.get()
                val kotlinSourceSet =
                    target.objects.sourceDirectorySet(variant.name, "${variant.name} kotlin source")

                variant.sourceSets.forEach {
                    val sourceSet = it as AndroidSourceSet
                    val extractTask = registerExtractProto(target, extension, sourceSet)
                    val generateTask =
                        registerGenerateProto(
                            target, extension, sourceSet,
                            extractTask, kotlinSourceSet
                        )
                    generateProtos.dependsOn(generateTask)
                }

                if (extension.autoGenerating) {
                    target.compileKotlinTask(variant.name) {
                        this.dependsOn(generateProtos)
                        this.source(kotlinSourceSet)
                    }
                }
            }
        }
    }

    private fun registerExtractProto(
        target: Project,
        extension: ProtobufExtension,
        sourceSet: AndroidSourceSet
    ): ExtractProtoTask {
        val extractTask: Task? =
            target.tasks.findByName("extract ${sourceSet.name} protos".toCamelCase())
        if (extractTask != null) {
            return extractTask as ExtractProtoTask
        }

        val inputDir = target.file(
            extension.sourceSet(sourceSet.name).inputDir
                ?: sourceSet.protoSourcePath
        )
        val resourceOutputDir = target.file(
            extension.sourceSet(sourceSet.name).resourceOutputDir
                ?: sourceSet.protoResourceCompileOutputPath
        )
        val protoDir = target.file(
            extension.sourceSet(sourceSet.name).resourceOutputDir
                ?: sourceSet.protoTempCompileOutputPath
        )

        Files.createDirectories(resourceOutputDir.toPath())
        Files.createDirectories(protoDir.toPath())

        sourceSet.resources.apply {
            srcDir(inputDir)
            srcDir(resourceOutputDir)
        }

        return target.tasks.register(
            "extract ${sourceSet.name} protos".toCamelCase(),
            ExtractProtoTask::class.java
        ) {
            if (sourceSet.isTestSourceSet) {
                val mainInputDir = target.file(
                    extension.sourceSet("main").inputDir
                        ?: target.android.sourceSets.findByName("main")!!.protoSourcePath
                )
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

    private fun registerGenerateProto(
        target: Project,
        extension: ProtobufExtension,
        sourceSet: AndroidSourceSet,
        extractTask: ExtractProtoTask,
        kotlinSourceSet: SourceDirectorySet
    ): ProtoGenerateTask {
        var generateTask: ProtoGenerateTask? =
            target.tasks.findByName("generate ${sourceSet.name} protos".toCamelCase()) as ProtoGenerateTask?
        if (generateTask == null) {
            val outputDir = target.file(
                extension.sourceSet(sourceSet.name).outputDir
                    ?: sourceSet.protoCompileOutputPath
            )
            val implOutputDir = target.file(
                extension.sourceSet(sourceSet.name).implDir
                    ?: sourceSet.protoInternalCompileOutputPath
            )
            val resourceOutputDir = target.file(
                extension.sourceSet(sourceSet.name).resourceOutputDir
                    ?: sourceSet.protoResourceCompileOutputPath
            )

            Files.createDirectories(outputDir.toPath())
            Files.createDirectories(implOutputDir.toPath())
            Files.createDirectories(resourceOutputDir.toPath())

            sourceSet.resources.apply {
                srcDir(resourceOutputDir)
            }

            sourceSet.java.apply {
                srcDir(outputDir)
                srcDir(implOutputDir)
            }

            generateTask = target.tasks.register(
                "generate ${sourceSet.name} protos".toCamelCase(),
                ProtoGenerateTask::class.java
            ) {
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
        }

        kotlinSourceSet.srcDir(generateTask!!.output)
        kotlinSourceSet.srcDir(generateTask.implOutput)

        return generateTask
    }

    private fun registerProtoConfig(
        target: Project,
        extension: ProtobufExtension,
        sourceSet: AndroidSourceSet
    ): Configuration {
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
            attributes.attribute(
                Usage.USAGE_ATTRIBUTE,
                target.objects.named(Usage::class.java, Usage.JAVA_API)
            )
        }
    }

    private fun registerProtoApiConfig(
        target: Project,
        extension: ProtobufExtension,
        sourceSet: AndroidSourceSet
    ): Configuration {
        return target.configurations.maybeCreate(sourceSet.getProtoApiConfigurationName()).apply {
            description = "Proto dependency files for compiling of source set '${sourceSet.name}'"
            isCanBeConsumed = false
            isCanBeResolved = true
            extendsFrom(target.configurations.getByName(sourceSet.implementationConfigurationName))
            attributes.attribute(
                Usage.USAGE_ATTRIBUTE,
                target.objects.named(Usage::class.java, Usage.JAVA_API)
            )
        }
    }

    private fun configTest(target: Project) {
        val testSourceSet = target.android.sourceSets.test ?: return
        val mainSourceSet = target.android.sourceSets.main ?: return

        target.protoApi(testSourceSet).extendsFrom(target.proto(mainSourceSet))
    }

    private val NamedDomainObjectContainer<com.android.build.gradle.api.AndroidSourceSet>.main
        get() = findByName("main")

    private val NamedDomainObjectContainer<com.android.build.gradle.api.AndroidSourceSet>.test
        get() = findByName("test")

    private val AndroidSourceSet.protoSourcePath: String get() = "src/$name/proto"

    private val AndroidSourceSet.protoCompileOutputPath: String
        get() = "build/generated/source/proto/$name"

    private val AndroidSourceSet.protoInternalCompileOutputPath: String
        get() = "build/generated/source/proto-internal/$name"

    private val AndroidSourceSet.protoResourceCompileOutputPath: String
        get() = "build/generated/resources/proto-meta/$name"

    private val AndroidSourceSet.protoTempCompileOutputPath: String get() = "build/tmp/proto/$name"

    private val AndroidSourceSet.isTestSourceSet: Boolean
        get() = name == "test" ||
                name.toLowerCase().contains("androidtest") ||
                name.toLowerCase().contains("unittest")

    private fun Project.protoApi(sourceSet: AndroidSourceSet): Configuration {
        return this.configurations.getByName(sourceSet.getProtoApiConfigurationName())
    }

    private fun Project.proto(sourceSet: AndroidSourceSet): Configuration {
        return this.configurations.getByName(sourceSet.getProtoConfigurationName())
    }

    private fun AndroidSourceSet.getProtoApiConfigurationName(): String {
        return buildString {
            if (!this@getProtoApiConfigurationName.isMainSourceSet) {
                append(this@getProtoApiConfigurationName.name)
                append(' ')
            }
            append("proto api")
        }.toCamelCase()
    }

    private fun AndroidSourceSet.getProtoConfigurationName(): String {
        return buildString {
            if (!isMainSourceSet) {
                append(name)
                append(' ')
            }
            append("proto")
        }.toCamelCase()
    }

    private fun Project.compileKotlinTask(
        name: String,
        block: SourceTask.() -> Unit
    ) {
        val compileTaskName = "compile${name.capitalize()}Kotlin"
        val compileTask = tasks.getByName(compileTaskName) as SourceTask?
        if (compileTask != null) {
            compileTask.block()
        } else {
            tasks.whenTaskAdded {
                if (name == compileTaskName) {
                    (it as SourceTask).block()
                }
            }
        }
    }

    private val nonTestVariants: DomainObjectSet<out BaseVariant>
        get() {
            val extension = project.android
            if (extension is AbstractAppExtension) {
                return extension.applicationVariants
            } else if (extension is LibraryExtension) {
                return extension.libraryVariants
            }
            throw IllegalStateException()
        }

    private val Project.android get() = extensions.getByName("android") as BaseExtension

    private val AndroidSourceSet.isMainSourceSet: Boolean get() = this.name == SourceSet.MAIN_SOURCE_SET_NAME
}
