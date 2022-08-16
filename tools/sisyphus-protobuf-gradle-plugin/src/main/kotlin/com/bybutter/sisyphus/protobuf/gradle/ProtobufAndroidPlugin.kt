package com.bybutter.sisyphus.protobuf.gradle

import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.attributes.Attribute
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceTask
import org.gradle.plugins.ide.idea.model.IdeaModel

class ProtobufAndroidPlugin : BaseProtobufPlugin() {
    override fun doApply() {
        for (sourceSet in project.android.sourceSets) {
            applySourceSet(sourceSet)
        }
        generateProtoTask()
    }

    override fun doAfterApply() {
        for (sourceSet in project.android.sourceSets) {
            afterApplySourceSet(sourceSet)
        }

        for (variant in project.android.variants) {
            afterApplyVariant(variant)
        }

        project.android.variants
    }

    override fun protoExtension(): ProtobufExtension {
        return project.extensions.create("protobuf", ProtobufExtension::class.java).apply {
            source = false
            plugins {
                basic().rxJava().resourceName().liteDescriptor().inlineDescriptor()
            }
        }
    }

    override fun protoApiFiles(sourceSetName: String): FileCollection {
        val variant = project.android.variants.first {
            it.name == sourceSetName
        }

        return project.files(
            variant.compileConfiguration.incoming.artifactView {
                it.attributes {
                    it.attribute(Attribute.of("artifactType", String::class.java), "jar")
                }
            }.files,
            variant.sourceSets.map { protoApiConfiguration(it.name) }
        )
    }

    override fun protoCompileFiles(sourceSetName: String): FileCollection {
        val variant = project.android.variants.first {
            it.name == sourceSetName
        }
        return project.files(
            variant.sourceSets.map { protoSrc(it.name) },
            variant.sourceSets.map { protoConfiguration(it.name) }
        )
    }

    private fun applySourceSet(sourceSet: AndroidSourceSet) {
        protoConfiguration(sourceSet.name)
        protoApiConfiguration(sourceSet.name)

        sourceSet.resources {
            srcDir(metadataDir(sourceSet.name))
        }

        sourceSet.java {
            srcDir(outDir(sourceSet.name))
        }
    }

    private fun afterApplySourceSet(sourceSet: AndroidSourceSet) {
        project.extensions.findByType(IdeaModel::class.java)?.apply {
            module.sourceDirs = module.sourceDirs + protoSrc(sourceSet.name)
            module.generatedSourceDirs.add(outDir(sourceSet.name))
            this.module.scopes["PROVIDED"]?.get("plus")?.add(protoApiConfiguration(sourceSet.name))
            this.module.scopes["COMPILE"]?.get("plus")?.add(protoConfiguration(sourceSet.name))
        }
    }

    private fun afterApplyVariant(variant: BaseVariant) {
        extractProtoTask(variant.name)
        generateProtoTask(variant.name)

        if (extension.autoGenerating) {
            val kotlinTask = project.tasks.findByName(compileKotlinTaskName(variant.name))
            kotlinTask?.dependsOn(generateProtoTask(variant.name))
            variant.processJavaResourcesProvider.configure {
                it.dependsOn(generateProtoTask(variant.name))
            }
        }
    }
}
