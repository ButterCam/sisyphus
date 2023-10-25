package com.bybutter.sisyphus.protobuf.compiler.resourcename

import com.bybutter.sisyphus.protobuf.compiler.DescriptorNode
import com.bybutter.sisyphus.protobuf.compiler.MessageFieldDescriptor
import com.bybutter.sisyphus.protobuf.compiler.fieldType
import com.bybutter.sisyphus.protobuf.compiler.fileSet
import com.bybutter.sisyphus.protobuf.compiler.mutableFieldType
import com.google.api.ResourceProto
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.ExtensionRegistry
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MUTABLE_LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName

object ResourceFields {
    fun resource(field: DescriptorNode<DescriptorProtos.FieldDescriptorProto>): ResourceDescriptor? {
        if (field is MessageFieldDescriptor) {
            val nameField = field.parent.resource?.descriptor?.nameField?.takeIf { it.isNotEmpty() } ?: "name"
            if (field.descriptor.name == nameField) {
                field.parent.resource?.let { return it }
            }
        }
        val options =
            DescriptorProtos.FieldOptions.parseFrom(
                field.descriptor.options.toByteArray(),
                extensionRegistry,
            )
        val reference = options.getExtension(ResourceProto.resourceReference) ?: return null

        if (reference.type.isNotEmpty()) {
            return field.fileSet().lookup(reference.type) as? ResourceDescriptor
        }
        return null
    }

    fun fieldType(field: DescriptorNode<DescriptorProtos.FieldDescriptorProto>): TypeName {
        val resource = resource(field) ?: return field.fieldType()
        when (field.descriptor.label) {
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED -> {
                return LIST.parameterizedBy(resource.className())
            }
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL -> {
                return resource.className().copy(true)
            }
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED -> {
                return resource.className()
            }
        }
    }

    fun mutableFieldType(field: DescriptorNode<DescriptorProtos.FieldDescriptorProto>): TypeName {
        val resource = resource(field) ?: return field.mutableFieldType()
        return if (field.descriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED) {
            MUTABLE_LIST.parameterizedBy(resource.className())
        } else {
            fieldType(field)
        }
    }

    val extensionRegistry =
        ExtensionRegistry.newInstance().apply {
            add(ResourceProto.resource)
            add(ResourceProto.resourceReference)
            add(ResourceProto.resourceDefinition)
        }
}
