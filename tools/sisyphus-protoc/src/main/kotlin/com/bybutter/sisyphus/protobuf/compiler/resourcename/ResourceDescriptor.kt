package com.bybutter.sisyphus.protobuf.compiler.resourcename

import com.bybutter.sisyphus.protobuf.compiler.DescriptorNode
import com.bybutter.sisyphus.protobuf.compiler.FileDescriptor
import com.bybutter.sisyphus.protobuf.compiler.MessageDescriptor
import com.bybutter.sisyphus.protobuf.compiler.defaultValue
import com.bybutter.sisyphus.protobuf.compiler.elementType
import com.bybutter.sisyphus.protobuf.compiler.fileSet
import com.bybutter.sisyphus.protobuf.compiler.mapEntry
import com.bybutter.sisyphus.string.plural
import com.bybutter.sisyphus.string.singular
import com.bybutter.sisyphus.string.toPascalCase
import com.google.api.ResourceDescriptor
import com.google.api.ResourceProto
import com.google.api.pathtemplate.PathTemplate
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.MUTABLE_LIST
import com.squareup.kotlinpoet.MUTABLE_MAP
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.buildCodeBlock

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
            "${name()}Base"
        } else {
            "${name()}With${uniqueFields.joinToString("And") { it.toPascalCase() }}"
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