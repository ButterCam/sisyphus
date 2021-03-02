package com.bybutter.sisyphus.protobuf.compiler.resourcename

import com.bybutter.sisyphus.protobuf.compiler.DescriptorNode
import com.bybutter.sisyphus.protobuf.compiler.FileDescriptor
import com.bybutter.sisyphus.protobuf.compiler.MessageDescriptor
import com.bybutter.sisyphus.protobuf.compiler.fileSet
import com.bybutter.sisyphus.string.plural
import com.bybutter.sisyphus.string.singular
import com.bybutter.sisyphus.string.toPascalCase
import com.google.api.ResourceDescriptor
import com.google.api.pathtemplate.PathTemplate
import com.squareup.kotlinpoet.ClassName

class ResourceDescriptor(
    override val parent: DescriptorNode<*>,
    override val descriptor: ResourceDescriptor
) : DescriptorNode<ResourceDescriptor>() {
    init {
        fileSet().registerLookup(descriptor.type, this)
    }

    val templates = descriptor.patternList.map {
        PathTemplate.create(it)
    }

    val commonFields = run {
        val commonFields = templates.firstOrNull()?.vars()?.toMutableSet() ?: mutableSetOf()
        for (template in templates) {
            commonFields.removeIf {
                !template.vars().contains(it)
            }
        }
        commonFields
    }

    val singular = run {
        if (this.descriptor.singular.isNotEmpty()) {
            return@run this.descriptor.singular
        }

        if (this.descriptor.plural.isNotEmpty()) {
            return@run this.descriptor.plural.singular()
        }

        resource().singular()
    }

    val plural = run {
        if (this.descriptor.plural.isNotEmpty()) {
            return@run this.descriptor.plural
        }

        if (this.descriptor.singular.isNotEmpty()) {
            return@run this.descriptor.singular.plural()
        }

        resource().plural()
    }

    fun templateName(template: PathTemplate): String {
        val uniqueFields = template.vars() - commonFields
        return if (uniqueFields.isEmpty()) {
            "Base"
        } else {
            template.vars().joinToString("And") { it.toPascalCase() }
        }
    }

    fun tempalteClassName(template: PathTemplate): ClassName {
        return className().nestedClass(templateName(template))
    }

    fun resource(): String {
        return descriptor.type.substringAfterLast("/")
    }

    fun name(): String {
        return when (parent) {
            is FileDescriptor -> "${resource()}Name"
            is MessageDescriptor -> "Name"
            else -> TODO()
        }
    }

    fun className(): ClassName {
        return when (parent) {
            is FileDescriptor -> ClassName(parent.packageName(), name())
            is MessageDescriptor -> parent.className().nestedClass(name())
            else -> TODO()
        }
    }
}
