package com.bybutter.sisyphus.protobuf.compiler.resourcename

import com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator
import com.bybutter.sisyphus.protobuf.compiler.SortableGenerator
import com.bybutter.sisyphus.protobuf.compiler.annotation
import com.bybutter.sisyphus.protobuf.compiler.beginScope
import com.bybutter.sisyphus.protobuf.compiler.clearFunction
import com.bybutter.sisyphus.protobuf.compiler.constructor
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageFieldReadFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageFieldWriteFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageImplementationFieldBasicGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageInterfaceFieldBasicGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MutableMessageInterfaceBasicFieldGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.OneofKindTypeBasicGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.state.FieldImplementationGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.FieldInterfaceGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.FieldMutableInterafaceGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageReadFieldFunctionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageWriteFieldsFunctionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.OneofKindTypeGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.function
import com.bybutter.sisyphus.protobuf.compiler.getter
import com.bybutter.sisyphus.protobuf.compiler.hasFunction
import com.bybutter.sisyphus.protobuf.compiler.implements
import com.bybutter.sisyphus.protobuf.compiler.name
import com.bybutter.sisyphus.protobuf.compiler.plusAssign
import com.bybutter.sisyphus.protobuf.compiler.property
import com.bybutter.sisyphus.protobuf.compiler.setter
import com.bybutter.sisyphus.protobuf.compiler.type
import com.bybutter.sisyphus.protobuf.compiler.util.makeTag
import com.bybutter.sisyphus.string.toPascalCase
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.WireFormat
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MUTABLE_LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock

class ResourceNameInterfaceFieldGenerator : GroupedGenerator<FieldInterfaceGeneratingState>,
    SortableGenerator<FieldInterfaceGeneratingState> {
    override val order: Int get() = -2000

    override val group: String get() = MessageInterfaceFieldBasicGenerator::class.java.canonicalName

    override fun generate(state: FieldInterfaceGeneratingState): Boolean {
        ResourceFields.resource(state.descriptor) ?: return false

        state.target.property(state.descriptor.name(), ResourceFields.fieldType(state.descriptor)) {
            addKdoc(state.descriptor.document())
            if (state.descriptor.descriptor.options?.deprecated == true) {
                annotation(Deprecated::class.asClassName()) {
                    addMember("message = %S", "${state.descriptor.name()} has been marked as deprecated")
                }
            }
        }

        if (state.descriptor.descriptor.label != DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED) {
            state.target.function(state.descriptor.hasFunction()) {
                this += KModifier.ABSTRACT
                returns(Boolean::class)
            }
        }

        return true
    }
}

class ResourceNameMutableInterfaceFieldGenerator : GroupedGenerator<FieldMutableInterafaceGeneratingState>,
    SortableGenerator<FieldMutableInterafaceGeneratingState> {
    override val order: Int get() = -2000

    override val group: String get() = MutableMessageInterfaceBasicFieldGenerator::class.java.canonicalName

    override fun generate(state: FieldMutableInterafaceGeneratingState): Boolean {
        ResourceFields.resource(state.descriptor) ?: return false
        state.target.property(state.descriptor.name(), ResourceFields.mutableFieldType(state.descriptor)) {
            this += KModifier.OVERRIDE
            if (state.descriptor.descriptor.label != DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED) {
                mutable()
            }
        }

        if (state.descriptor.descriptor.label != DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED) {
            state.target.function(state.descriptor.clearFunction()) {
                this += KModifier.ABSTRACT
                returns(
                    ResourceFields.fieldType(state.descriptor)
                        .copy(state.descriptor.descriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL)
                )
            }
        }
        return true
    }
}

class ResourceNameImplementationFieldGenerator : GroupedGenerator<FieldImplementationGeneratingState>,
    SortableGenerator<FieldImplementationGeneratingState> {
    override val order: Int get() = -2000

    override val group: String get() = MessageImplementationFieldBasicGenerator::class.java.canonicalName

    override fun generate(state: FieldImplementationGeneratingState): Boolean {
        val resource = ResourceFields.resource(state.descriptor) ?: return false

        when (state.descriptor.descriptor.label) {
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL -> {
                state.target.property(state.descriptor.name(), resource.className().copy(true)) {
                    this += KModifier.OVERRIDE
                    mutable()
                    initializer("null")
                }

                state.target.function(state.descriptor.hasFunction()) {
                    this += KModifier.OVERRIDE
                    returns(Boolean::class.java)
                    addStatement("return %N != null", state.descriptor.name())
                }

                state.target.function(state.descriptor.clearFunction()) {
                    this += KModifier.OVERRIDE
                    returns(resource.className().copy(true))
                    beginControlFlow("return %N.also", state.descriptor.name())
                    addStatement("%N = null", state.descriptor.name())
                    endControlFlow()
                }
            }
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED -> {
                state.target.property(state.descriptor.name(), resource.className()) {
                    this += KModifier.OVERRIDE
                    this += KModifier.LATEINIT
                    mutable()
                }
            }
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED -> {
                state.target.property(state.descriptor.name(), MUTABLE_LIST.parameterizedBy(resource.className())) {
                    this += KModifier.OVERRIDE
                    initializer("mutableListOf()")
                }

                state.target.function(state.descriptor.hasFunction()) {
                    this += KModifier.OVERRIDE
                    returns(Boolean::class.java)
                    addStatement("return %N.isNotEmpty()", state.descriptor.name())
                }

                state.target.function(state.descriptor.clearFunction()) {
                    this += KModifier.OVERRIDE
                    returns(LIST.parameterizedBy(resource.className()))
                    beginControlFlow("return %N.toList().also", state.descriptor.name())
                    addStatement("%N.clear()", state.descriptor.name())
                    endControlFlow()
                }
            }
        }
        return true
    }
}

class ResourceNameOneofImplementationFieldGenerator : GroupedGenerator<FieldImplementationGeneratingState>,
    SortableGenerator<FieldImplementationGeneratingState> {
    override val order: Int get() = -3000

    override val group: String get() = MessageImplementationFieldBasicGenerator::class.java.canonicalName

    override fun generate(state: FieldImplementationGeneratingState): Boolean {
        val resource = ResourceFields.resource(state.descriptor) ?: return false
        val oneOf = state.descriptor.oneof() ?: return false

        state.target.property(state.descriptor.name(), resource.className().copy(true)) {
            this += KModifier.OVERRIDE
            mutable()
            getter {
                addStatement(
                    "return (%N as? %T)?.value",
                    oneOf.fieldName(),
                    oneOf.oneOfClassName().nestedClass(state.descriptor.descriptor.name.toPascalCase())
                )
            }
            setter {
                addParameter("value", resource.className().copy(true))
                addCode(buildCodeBlock {
                    addStatement(
                        "%N = value?.let { %T(it) }",
                        oneOf.fieldName(),
                        oneOf.oneOfClassName().nestedClass(state.descriptor.descriptor.name.toPascalCase())
                    )
                })
            }
        }

        state.target.function(state.descriptor.hasFunction()) {
            this += KModifier.OVERRIDE
            returns(Boolean::class.java)
            addStatement(
                "return %N is %T",
                oneOf.fieldName(),
                oneOf.oneOfClassName().nestedClass(state.descriptor.descriptor.name.toPascalCase())
            )
        }

        state.target.function(state.descriptor.clearFunction()) {
            this += KModifier.OVERRIDE
            returns(resource.className().copy(true))
            beginControlFlow(
                "return (%N as? %T)?.value?.also",
                oneOf.fieldName(),
                oneOf.oneOfClassName().nestedClass(state.descriptor.descriptor.name.toPascalCase())
            )
            addStatement("%N = null", oneOf.fieldName())
            endControlFlow()
        }

        return true
    }
}

class ResourceNameOneofKindTypeBasicGenerator : GroupedGenerator<OneofKindTypeGeneratingState>,
    SortableGenerator<OneofKindTypeGeneratingState> {
    override val order: Int get() = -1000

    override val group: String get() = OneofKindTypeBasicGenerator::class.java.canonicalName

    override fun generate(state: OneofKindTypeGeneratingState): Boolean {
        val oneof = state.descriptor.oneof() ?: return false
        val resource = ResourceFields.resource(state.descriptor) ?: return false
        state.target.type(state.descriptor.descriptor.name.toPascalCase()) {
            this += KModifier.DATA
            this implements oneof.oneOfClassName().parameterizedBy(resource.className())
            constructor {
                addParameter("value", resource.className())
            }
            property("value", resource.className()) {
                this += KModifier.OVERRIDE
                initializer("value")
            }
        }
        return true
    }
}

class ResourceNameMessageFieldWriteFunctionGenerator : GroupedGenerator<MessageWriteFieldsFunctionGeneratingState>,
    SortableGenerator<MessageWriteFieldsFunctionGeneratingState> {
    override val group: String get() = MessageFieldWriteFunctionGenerator::class.java.canonicalName

    override val order: Int get() = -1000

    override fun generate(state: MessageWriteFieldsFunctionGeneratingState): Boolean {
        ResourceFields.resource(state.descriptor) ?: return false

        state.target.apply {
            addStatement(
                "this.%N?.let{ writer.tag(${
                    makeTag(
                        state.descriptor.descriptor.number,
                        WireFormat.WIRETYPE_LENGTH_DELIMITED
                    )
                }).string(it.value()) }", state.descriptor.name()
            )
        }
        return true
    }
}

class ResourceNameMessageFieldReadFunctionGenerator : GroupedGenerator<MessageReadFieldFunctionGeneratingState>,
    SortableGenerator<MessageReadFieldFunctionGeneratingState> {
    override val group: String get() = MessageFieldReadFunctionGenerator::class.java.canonicalName

    override val order: Int get() = -1000

    override fun generate(state: MessageReadFieldFunctionGeneratingState): Boolean {
        val resource = ResourceFields.resource(state.descriptor) ?: return false

        state.target.apply {
            addStatement(
                "${state.descriptor.descriptor.number} -> this.%N = %T(reader.string())",
                state.descriptor.name(), resource.className()
            )
        }
        return true
    }
}