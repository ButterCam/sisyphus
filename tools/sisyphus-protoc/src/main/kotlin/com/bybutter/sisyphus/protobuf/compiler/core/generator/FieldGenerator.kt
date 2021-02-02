package com.bybutter.sisyphus.protobuf.compiler.core.generator

import com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator
import com.bybutter.sisyphus.protobuf.compiler.annotation
import com.bybutter.sisyphus.protobuf.compiler.clearFunction
import com.bybutter.sisyphus.protobuf.compiler.core.state.FieldImplementationGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.FieldInterfaceGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.FieldMutableInterafaceGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageCompanionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageImplementationGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageInterfaceGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MutableMessageInterfaceGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.advance
import com.bybutter.sisyphus.protobuf.compiler.defaultValue
import com.bybutter.sisyphus.protobuf.compiler.fieldType
import com.bybutter.sisyphus.protobuf.compiler.function
import com.bybutter.sisyphus.protobuf.compiler.getter
import com.bybutter.sisyphus.protobuf.compiler.hasFunction
import com.bybutter.sisyphus.protobuf.compiler.mapEntry
import com.bybutter.sisyphus.protobuf.compiler.mutableFieldType
import com.bybutter.sisyphus.protobuf.compiler.name
import com.bybutter.sisyphus.protobuf.compiler.plusAssign
import com.bybutter.sisyphus.protobuf.compiler.property
import com.bybutter.sisyphus.protobuf.compiler.setter
import com.bybutter.sisyphus.string.toScreamingSnakeCase
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName

class MessageInterfaceFieldGenerator : GroupedGenerator<MessageInterfaceGeneratingState> {
    override fun generate(state: MessageInterfaceGeneratingState): Boolean {
        for (field in state.descriptor.fields) {
            FieldInterfaceGeneratingState(state, field, state.target).advance()
        }
        return true
    }
}

class MessageInterfaceFieldBasicGenerator : GroupedGenerator<FieldInterfaceGeneratingState> {
    override fun generate(state: FieldInterfaceGeneratingState): Boolean {
        state.target.property(state.descriptor.name(), state.descriptor.fieldType()) {
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

class MutableMessageInterfaceFieldGenerator : GroupedGenerator<MutableMessageInterfaceGeneratingState> {
    override fun generate(state: MutableMessageInterfaceGeneratingState): Boolean {
        for (field in state.descriptor.fields) {
            FieldMutableInterafaceGeneratingState(state, field, state.target).advance()
        }
        return true
    }
}

class MutableMessageInterfaceBasicFieldGenerator : GroupedGenerator<FieldMutableInterafaceGeneratingState> {
    override fun generate(state: FieldMutableInterafaceGeneratingState): Boolean {
        state.target.property(state.descriptor.name(), state.descriptor.mutableFieldType()) {
            this += KModifier.OVERRIDE
            if (state.descriptor.descriptor.label != DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED) {
                mutable()
            }
        }

        if (state.descriptor.descriptor.label != DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED) {
            state.target.function(state.descriptor.clearFunction()) {
                this += KModifier.ABSTRACT
                returns(
                    state.descriptor.fieldType().copy(state.descriptor.descriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL)
                )
            }
        }
        return true
    }
}

class MessageImplementationFieldGenerator : GroupedGenerator<MessageImplementationGeneratingState> {
    override fun generate(state: MessageImplementationGeneratingState): Boolean {
        for (field in state.descriptor.fields) {
            FieldImplementationGeneratingState(state, field, state.target).advance()
        }
        return true
    }
}

class MessageImplementationFieldBasicGenerator : GroupedGenerator<FieldImplementationGeneratingState> {
    override fun generate(state: FieldImplementationGeneratingState): Boolean {
        val isPrimitive = when (state.descriptor.descriptor.type) {
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES -> false
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING -> false
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE -> false
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM -> false
            else -> true
        }

        when (state.descriptor.descriptor.label) {
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL -> {
                state.target.property("_${state.descriptor.hasFunction()}", Boolean::class.java.asTypeName()) {
                    this += KModifier.PRIVATE
                    mutable()
                    initializer("false")
                }

                state.target.property(state.descriptor.name(), state.descriptor.mutableFieldType()) {
                    this += KModifier.OVERRIDE
                    mutable()
                    initializer(state.descriptor.defaultValue())
                    getter {
                        addStatement(
                            "return if(_${state.descriptor.hasFunction()}) field else %L",
                            state.descriptor.defaultValue()
                        )
                    }
                    setter {
                        addParameter("value", state.descriptor.mutableFieldType())
                        addStatement("field = value")
                        if (state.descriptor.mutableFieldType().isNullable) {
                            addStatement("_${state.descriptor.hasFunction()} = value != null")
                        } else {
                            addStatement("_${state.descriptor.hasFunction()} = true")
                        }
                    }
                }

                state.target.function(state.descriptor.hasFunction()) {
                    this += KModifier.OVERRIDE
                    returns(Boolean::class.java)
                    addStatement("return _${state.descriptor.hasFunction()}")
                }

                state.target.function(state.descriptor.clearFunction()) {
                    this += KModifier.OVERRIDE
                    returns(state.descriptor.fieldType().copy(true))
                    addStatement("if (!${state.descriptor.hasFunction()}()) return null")
                    beginControlFlow("return %N.also", state.descriptor.name())
                    addStatement("%N = %L", state.descriptor.name(), state.descriptor.defaultValue())
                    addStatement("_${state.descriptor.hasFunction()} = false")
                    endControlFlow()
                }
            }
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED -> {
                state.target.property(state.descriptor.name(), state.descriptor.mutableFieldType()) {
                    this += KModifier.OVERRIDE
                    if (isPrimitive) {
                        this.initializer(state.descriptor.defaultValue())
                    } else {
                        this += KModifier.LATEINIT
                    }
                    mutable()
                }
            }
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED -> {
                state.target.property(state.descriptor.name(), state.descriptor.mutableFieldType()) {
                    this += KModifier.OVERRIDE
                    initializer(state.descriptor.defaultValue())
                }

                state.target.function(state.descriptor.hasFunction()) {
                    this += KModifier.OVERRIDE
                    returns(Boolean::class.java)
                    addStatement("return %N.isNotEmpty()", state.descriptor.name())
                }

                state.target.function(state.descriptor.clearFunction()) {
                    this += KModifier.OVERRIDE
                    returns(state.descriptor.fieldType())
                    if (state.descriptor.mapEntry() != null) {
                        beginControlFlow("return %N.toMap().also", state.descriptor.name())
                        addStatement("%N.clear()", state.descriptor.name())
                        endControlFlow()
                    } else {
                        beginControlFlow("return %N.toList().also", state.descriptor.name())
                        addStatement("%N.clear()", state.descriptor.name())
                        endControlFlow()
                    }
                }
            }
        }
        return true
    }
}

class MessageCompanionFieldNameConstGenerator : GroupedGenerator<MessageCompanionGeneratingState> {
    override fun generate(state: MessageCompanionGeneratingState): Boolean {
        for (field in state.descriptor.fields) {
            state.target.property("${field.descriptor.name}_field_name".toScreamingSnakeCase(), String::class) {
                this += KModifier.CONST
                initializer("%S", field.descriptor.name)
            }

            state.target.property("${field.descriptor.name}_field_number".toScreamingSnakeCase(), Int::class) {
                this += KModifier.CONST
                initializer(field.descriptor.number.toString())
            }
        }
        return true
    }
}
