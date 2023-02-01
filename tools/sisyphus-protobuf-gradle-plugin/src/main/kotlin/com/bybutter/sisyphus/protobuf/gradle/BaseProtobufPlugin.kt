package com.bybutter.sisyphus.protobuf.gradle

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.file.FileCollection
import java.io.File

abstract class BaseProtobufPlugin : Plugin<Project> {
    protected lateinit var project: Project
    protected lateinit var extension: ProtobufExtension
    private var applied = false

    final override fun apply(target: Project) {
        if (applied) return

        this.project = target
        applied = true
        extension = protoExtension()
        doApply()
        project.afterEvaluate {
            doAfterApply()
            if (!applied) {
                throw GradleException("The 'com.bybutter.sisyphus.protobuf' plugin requires 'java' or 'android' plugin.")
            }
        }
    }

    protected abstract fun protoExtension(): ProtobufExtension

    protected open fun protoConfiguration(sourceSetName: String): Configuration {
        return project.configurations.maybeCreate(protoConfigurationName(sourceSetName)).apply {
            description = "Proto files to compile for source set '$sourceSetName'"
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
                project.objects.named(Usage::class.java, Usage.JAVA_API)
            )
        }
    }

    protected open fun protoApiConfiguration(sourceSetName: String): Configuration {
        return project.configurations.maybeCreate(protoApiConfigurationName(sourceSetName)).apply {
            description = "Proto dependency files for compiling of source set '$sourceSetName'"
            isCanBeConsumed = false
            isCanBeResolved = true
            attributes.attribute(
                Usage.USAGE_ATTRIBUTE,
                project.objects.named(Usage::class.java, Usage.JAVA_API)
            )
            attributes.attribute(
                LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
                project.objects.named(LibraryElements::class.java, LibraryElements.RESOURCES)
            )
        }
    }

    protected open fun generateProtoTask(): Task {
        val name = "generateProtos"
        return project.tasks.findByName(name) ?: project.tasks.register(name) {
            it.group = "proto"
            it.description = "Generate protos for all source set."
        }.get()
    }

    protected abstract fun protoCompileFiles(sourceSetName: String): FileCollection

    protected abstract fun protoApiFiles(sourceSetName: String): FileCollection

    protected open fun extractProtoTask(sourceSetName: String): ExtractProtoTask {
        val name = extractProtoTaskName(sourceSetName)
        return project.tasks.findByName(name) as? ExtractProtoTask ?: project.tasks.register(
            name,
            ExtractProtoTask::class.java
        ) {
            it.resourceOutput.set(metadataDir(sourceSetName))
            it.protoPath.set(workDir(sourceSetName))
            it.protoApiFiles.from(protoApiFiles(sourceSetName))
            it.protoCompileFiles.from(protoCompileFiles(sourceSetName))
            it.group = "proto"
            it.description = "Extract protos for '$sourceSetName' source set."
            it.protobuf = extension

            it.dependsOn(protoConfiguration(sourceSetName))
            it.dependsOn(protoApiConfiguration(sourceSetName))
        }.get()
    }

    protected open fun generateProtoTask(sourceSetName: String): GenerateProtoTask {
        val name = generateProtoTaskName(sourceSetName)
        return project.tasks.findByName(name) as? GenerateProtoTask ?: project.tasks.register(
            name,
            GenerateProtoTask::class.java
        ) {
            it.protoPath.set(workDir(sourceSetName))
            it.output.set(outDir(sourceSetName))
            it.resourceOutput.set(metadataDir(sourceSetName))
            it.group = "proto"
            it.description = "Generate protos for '$sourceSetName' source set."
            it.protobuf = extension

            it.dependsOn(extractProtoTask(sourceSetName))
            generateProtoTask().dependsOn(it)
        }.get()
    }

    protected open fun protoSrc(sourceSetName: String): File {
        return extension.sourceSet(sourceSetName).srcDir?.let {
            project.file(it)
        } ?: project.file("src/$sourceSetName/proto")
    }

    protected open fun metadataDir(sourceSetName: String): File {
        return extension.sourceSet(sourceSetName).metadataDir?.let {
            project.file(it)
        } ?: project.buildDir.resolve("generated/proto/metadata/$sourceSetName")
    }

    protected open fun outDir(sourceSetName: String): File {
        return extension.sourceSet(sourceSetName).metadataDir?.let {
            project.file(it)
        } ?: project.buildDir.resolve("generated/proto/source/$sourceSetName")
    }

    protected open fun workDir(sourceSetName: String): File {
        return project.buildDir.resolve("tmp/proto/$sourceSetName")
    }

    abstract fun doApply()

    abstract fun doAfterApply()
}
