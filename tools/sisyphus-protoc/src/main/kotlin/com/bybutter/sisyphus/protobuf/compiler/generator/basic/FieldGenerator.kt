package com.bybutter.sisyphus.protobuf.compiler.generator.basic

import com.bybutter.sisyphus.protobuf.compiler.annotation
import com.bybutter.sisyphus.protobuf.compiler.function
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.FieldGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.FieldImplementationGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MutableFieldGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.clearFunction
import com.bybutter.sisyphus.protobuf.compiler.generating.defaultValue
import com.bybutter.sisyphus.protobuf.compiler.generating.document
import com.bybutter.sisyphus.protobuf.compiler.generating.fieldType
import com.bybutter.sisyphus.protobuf.compiler.generating.hasFunction
import com.bybutter.sisyphus.protobuf.compiler.generating.mapEntry
import com.bybutter.sisyphus.protobuf.compiler.generating.mutableFieldType
import com.bybutter.sisyphus.protobuf.compiler.generating.name
import com.bybutter.sisyphus.protobuf.compiler.generator.UniqueGenerator
import com.bybutter.sisyphus.protobuf.compiler.getter
import com.bybutter.sisyphus.protobuf.compiler.plusAssign
import com.bybutter.sisyphus.protobuf.compiler.property
import com.bybutter.sisyphus.protobuf.compiler.setter
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName

open class FieldGenerator : UniqueGenerator<FieldGeneratingState> {
    override fun generate(state: FieldGeneratingState): Boolean {
        state.target.property(state.name(), state.fieldType()) {
            addKdoc(state.document())
            if (state.descriptor.options?.deprecated == true) {
                annotation(Deprecated::class.asClassName()) {
                    addMember("message = %S", "${state.name()} has been marked as deprecated")
                }
            }
        }

        if (state.descriptor.label != DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED) {
            state.target.function(state.hasFunction()) {
                this += KModifier.ABSTRACT
                returns(Boolean::class)
            }
        }

        return true
    }
}

open class MutableFieldGenerator : UniqueGenerator<MutableFieldGeneratingState> {
    override fun generate(state: MutableFieldGeneratingState): Boolean {
        state.target.property(state.name(), state.mutableFieldType()) {
            this += KModifier.OVERRIDE
            if (state.descriptor.label != DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED) {
                mutable()
            }
        }

        if (state.descriptor.label != DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED) {
            state.target.function(state.clearFunction()) {
                this += KModifier.ABSTRACT
                returns(
                    state.fieldType()
                        .copy(state.descriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL)
                )
            }
        }

        return true
    }
}

open class FieldImplementationGenerator : UniqueGenerator<FieldImplementationGeneratingState> {
    override fun generate(state: FieldImplementationGeneratingState): Boolean {
        val isPrimitive = when(state.descriptor.type) {
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES -> false
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING -> false
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE -> false
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM -> false
            else -> true
        }

        when (state.descriptor.label) {
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL -> {
                state.target.property("_${state.hasFunction()}", Boolean::class.java.asTypeName()) {
                    this += KModifier.PRIVATE
                    mutable()
                    initializer("false")
                }

                state.target.property(state.name(), state.mutableFieldType()) {
                    this += KModifier.OVERRIDE
                    mutable()
                    initializer(state.defaultValue())
                    getter {
                        addStatement("return if(_${state.hasFunction()}) field else %L", state.defaultValue())
                    }
                    setter {
                        addParameter("value", state.mutableFieldType())
                        addStatement("field = value")
                        if (state.mutableFieldType().isNullable) {
                            addStatement("_${state.hasFunction()} = value != null")
                        } else {
                            addStatement("_${state.hasFunction()} = true")
                        }
                    }
                }

                state.target.function(state.hasFunction()) {
                    this += KModifier.OVERRIDE
                    returns(Boolean::class.java)
                    addStatement("return _${state.hasFunction()}")
                }

                state.target.function(state.clearFunction()) {
                    this += KModifier.OVERRIDE
                    returns(state.fieldType().copy(true))
                    addStatement("if (!${state.hasFunction()}()) return null")
                    beginControlFlow("return %N.also", state.name())
                    addStatement("%N = %L", state.name(), state.defaultValue())
                    addStatement("_${state.hasFunction()} = false")
                    endControlFlow()
                }
            }
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED -> {
                state.target.property(state.name(), state.mutableFieldType()) {
                    this += KModifier.OVERRIDE
                    if(isPrimitive) {
                        this.initializer(state.defaultValue())
                    } else {
                        this += KModifier.LATEINIT
                    }
                    mutable()
                }
            }
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED -> {
                state.target.property(state.name(), state.mutableFieldType()) {
                    this += KModifier.OVERRIDE
                    initializer(state.defaultValue())
                }

                state.target.function(state.hasFunction()) {
                    this += KModifier.OVERRIDE
                    returns(Boolean::class.java)
                    addStatement("return %N.isNotEmpty()", state.name())
                }

                state.target.function(state.clearFunction()) {
                    this += KModifier.OVERRIDE
                    returns(state.fieldType())
                    if (state.mapEntry() != null) {
                        beginControlFlow("return %N.toMap().also", state.name())
                        addStatement("%N.clear()", state.name())
                        endControlFlow()
                    } else {
                        beginControlFlow("return %N.toList().also", state.name())
                        addStatement("%N.clear()", state.name())
                        endControlFlow()
                    }
                }
            }
        }

        return true
    }
}
