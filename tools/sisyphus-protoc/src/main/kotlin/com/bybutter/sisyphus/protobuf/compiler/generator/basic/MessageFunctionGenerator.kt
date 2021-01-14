package com.bybutter.sisyphus.protobuf.compiler.generator.basic

import com.bybutter.sisyphus.protobuf.compiler.RuntimeMethods
import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.WhenBranchBuilder
import com.bybutter.sisyphus.protobuf.compiler.annotation
import com.bybutter.sisyphus.protobuf.compiler.beginScope
import com.bybutter.sisyphus.protobuf.compiler.function
import com.bybutter.sisyphus.protobuf.compiler.generating.advance
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MessageClearFieldInCurrentFunctionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MessageComputeHashCodeFunctionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MessageEqualsFunctionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MessageFunctionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MessageGetFieldInCurrentFunctionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MessageGetPropertyFunctionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MessageHasFieldInCurrentFunctionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MessageReadFieldFunctionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MessageSetFieldInCurrentFunctionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MessageWriteFieldsFunctionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.className
import com.bybutter.sisyphus.protobuf.compiler.generating.compiler
import com.bybutter.sisyphus.protobuf.compiler.generating.fieldType
import com.bybutter.sisyphus.protobuf.compiler.generating.implementationClassName
import com.bybutter.sisyphus.protobuf.compiler.generating.implementationName
import com.bybutter.sisyphus.protobuf.compiler.generating.mutableClassName
import com.bybutter.sisyphus.protobuf.compiler.generator.UniqueGenerator
import com.bybutter.sisyphus.protobuf.compiler.plusAssign
import com.bybutter.sisyphus.protobuf.compiler.util.makeTag
import com.bybutter.sisyphus.string.toPascalCase
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.WireFormat
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock
import kotlin.reflect.KProperty

open class MessageSupportFunctionGenerator :
    UniqueGenerator<MessageFunctionGeneratingState> {
    override fun generate(state: MessageFunctionGeneratingState): Boolean {
        state.target.apply {
            function("support") {
                this += KModifier.OVERRIDE
                returns(
                    RuntimeTypes.MESSAGE_SUPPORT.parameterizedBy(
                        state.parent.className(),
                        state.parent.mutableClassName()
                    )
                )
                addStatement("return %T", state.parent.className())
            }
        }
        return false
    }
}

open class MessageMergeWithFunctionGenerator :
    UniqueGenerator<MessageFunctionGeneratingState> {
    override fun generate(state: MessageFunctionGeneratingState): Boolean {
        state.target.apply {
            function("mergeWith") {
                this += KModifier.OVERRIDE
                addParameter("other", state.parent.className().copy(true))
                addStatement("other ?: return")
                addStatement("readFrom(Reader(other.toProto().inputStream()))")
            }
        }
        return false
    }
}

open class MessageCloneMutableFunctionGenerator :
    UniqueGenerator<MessageFunctionGeneratingState> {
    override fun generate(state: MessageFunctionGeneratingState): Boolean {
        state.target.apply {
            function("cloneMutable") {
                annotation(RuntimeTypes.INTERNAL_PROTO_API)

                this += KModifier.OVERRIDE
                returns(state.parent.mutableClassName())
                beginControlFlow("return %T().apply", state.parent.implementationClassName())
                addStatement("mergeWith(this@%T)", state.parent.implementationClassName())
                endControlFlow()
            }
        }
        return false
    }
}

open class MessageClearFunctionGenerator :
    UniqueGenerator<MessageFunctionGeneratingState> {
    override fun generate(state: MessageFunctionGeneratingState): Boolean {
        state.target.apply {
            function("clear") {
                this += KModifier.OVERRIDE
                for (fieldDescriptor in state.descriptor.fieldList) {
                    if (fieldDescriptor.label != DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED) {
                        addStatement("this.clear${fieldDescriptor.jsonName.toPascalCase()}()")
                    }
                }
            }
        }
        return false
    }
}

open class MessageClearFieldInCurrentFunctionGenerator :
    UniqueGenerator<MessageFunctionGeneratingState> {
    override fun generate(state: MessageFunctionGeneratingState): Boolean {
        state.target.apply {
            function("clearFieldInCurrent") {
                this += KModifier.OVERRIDE
                addParameter("fieldName", String::class)
                returns(ANY.copy(true))
                addCode(buildCodeBlock {
                    beginScope("return when(fieldName)") {
                        for (fieldDescriptor in state.descriptor.fieldList) {
                            if (fieldDescriptor.name != fieldDescriptor.jsonName) {
                                MessageClearFieldInCurrentFunctionGeneratingState(
                                    state, fieldDescriptor, WhenBranchBuilder(
                                        buildCodeBlock {
                                            add(
                                                "%S, %S ->",
                                                fieldDescriptor.name,
                                                fieldDescriptor.jsonName
                                            )
                                        }, this
                                    )
                                ).advance()
                            } else {
                                MessageClearFieldInCurrentFunctionGeneratingState(
                                    state, fieldDescriptor, WhenBranchBuilder(
                                        buildCodeBlock {
                                            add("%S ->", fieldDescriptor.name)
                                        }, this
                                    )
                                ).advance()
                            }
                        }
                        addStatement("else -> clearFieldInExtensions(fieldName)")
                    }
                })
            }

            function("clearFieldInCurrent") {
                this += KModifier.OVERRIDE
                addParameter("fieldNumber", Int::class)
                returns(ANY.copy(true))
                addCode(buildCodeBlock {
                    beginScope("return when(fieldNumber)") {
                        for (fieldDescriptor in state.descriptor.fieldList) {
                            MessageClearFieldInCurrentFunctionGeneratingState(
                                state, fieldDescriptor, WhenBranchBuilder(
                                    buildCodeBlock {
                                        add("${fieldDescriptor.number} ->")
                                    }, this
                                )
                            ).advance()
                        }
                        addStatement("else -> clearFieldInExtensions(fieldNumber)")
                    }
                })
            }
        }
        return false
    }
}

open class MessageFieldClearFieldInCurrentFunctionGenerator :
    UniqueGenerator<MessageClearFieldInCurrentFunctionGeneratingState> {
    override fun generate(state: MessageClearFieldInCurrentFunctionGeneratingState): Boolean {
        state.target.codeBlock.apply {
            if (state.descriptor.label != DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED) {
                addStatement("%L this.clear${state.descriptor.jsonName.toPascalCase()}()", state.target.branch)
            } else {
                addStatement(
                    "%L throw %T(%S)",
                    state.target.branch,
                    IllegalArgumentException::class,
                    "Field '${state.descriptor.name}' is required field can't be clear."
                )
            }
        }
        return true
    }
}

open class MessageGetFieldInCurrentFunctionGenerator :
    UniqueGenerator<MessageFunctionGeneratingState> {
    override fun generate(state: MessageFunctionGeneratingState): Boolean {
        state.target.apply {
            function("getFieldInCurrent") {
                this += KModifier.OVERRIDE
                addTypeVariable(TypeVariableName("T", ANY.copy(true)))
                addParameter("fieldName", String::class)
                returns(TypeVariableName("T"))
                addCode(buildCodeBlock {
                    beginControlFlow("return when(fieldName)")
                    for (fieldDescriptor in state.descriptor.fieldList) {
                        if (fieldDescriptor.name != fieldDescriptor.jsonName) {
                            MessageGetFieldInCurrentFunctionGeneratingState(
                                state, fieldDescriptor, WhenBranchBuilder(
                                    buildCodeBlock {
                                        add(
                                            "%S, %S ->",
                                            fieldDescriptor.name,
                                            fieldDescriptor.jsonName
                                        )
                                    }, this
                                )
                            ).advance()
                        } else {
                            MessageGetFieldInCurrentFunctionGeneratingState(
                                state, fieldDescriptor, WhenBranchBuilder(
                                    buildCodeBlock {
                                        add("%S ->", fieldDescriptor.name)
                                    }, this
                                )
                            ).advance()
                        }
                    }
                    addStatement("else -> getFieldInExtensions<T>(fieldName)")
                    unindent()
                    add("}.%M()\n", RuntimeMethods.UNCHECK_CAST)
                })
            }

            function("getFieldInCurrent") {
                this += KModifier.OVERRIDE
                addTypeVariable(TypeVariableName("T", ANY.copy(true)))
                addParameter("fieldNumber", Int::class)
                returns(TypeVariableName("T"))
                addCode(buildCodeBlock {
                    beginControlFlow("return when(fieldNumber)")
                    for (fieldDescriptor in state.descriptor.fieldList) {
                        MessageGetFieldInCurrentFunctionGeneratingState(
                            state, fieldDescriptor, WhenBranchBuilder(
                                buildCodeBlock {
                                    add("${fieldDescriptor.number} ->")
                                }, this
                            )
                        ).advance()
                    }
                    addStatement("else -> getFieldInExtensions<T>(fieldNumber)")
                    unindent()
                    add("}.%M()\n", RuntimeMethods.UNCHECK_CAST)
                })
            }
        }
        return false
    }
}

open class MessageFieldGetFieldInCurrentFunctionGenerator :
    UniqueGenerator<MessageGetFieldInCurrentFunctionGeneratingState> {
    override fun generate(state: MessageGetFieldInCurrentFunctionGeneratingState): Boolean {
        state.target.codeBlock.apply {
            addStatement("%L this.%N", state.target.branch, state.descriptor.jsonName)
        }
        return true
    }
}

open class MessageGetPropertyFunctionGenerator :
    UniqueGenerator<MessageFunctionGeneratingState> {
    override fun generate(state: MessageFunctionGeneratingState): Boolean {
        state.target.apply {
            function("getProperty") {
                this += KModifier.OVERRIDE
                addParameter("fieldName", String::class)
                returns(KProperty::class.asClassName().parameterizedBy(TypeVariableName("*")).copy(true))
                addCode(buildCodeBlock {
                    beginScope("return when(fieldName)") {
                        for (fieldDescriptor in state.descriptor.fieldList) {
                            if (fieldDescriptor.name != fieldDescriptor.jsonName) {
                                MessageGetPropertyFunctionGeneratingState(
                                    state, fieldDescriptor, WhenBranchBuilder(
                                        buildCodeBlock {
                                            add(
                                                "%S, %S ->",
                                                fieldDescriptor.name,
                                                fieldDescriptor.jsonName
                                            )
                                        }, this
                                    )
                                ).advance()
                            } else {
                                MessageGetPropertyFunctionGeneratingState(
                                    state, fieldDescriptor, WhenBranchBuilder(
                                        buildCodeBlock {
                                            add("%S ->", fieldDescriptor.name)
                                        }, this
                                    )
                                ).advance()
                            }
                        }
                        addStatement("else -> getPropertyInExtensions(fieldName)")
                    }
                })
            }

            function("getProperty") {
                this += KModifier.OVERRIDE
                addParameter("fieldNumber", Int::class)
                returns(KProperty::class.asClassName().parameterizedBy(TypeVariableName("*")).copy(true))
                addCode(buildCodeBlock {
                    beginScope("return when(fieldNumber)") {
                        for (fieldDescriptor in state.descriptor.fieldList) {
                            MessageGetPropertyFunctionGeneratingState(
                                state, fieldDescriptor, WhenBranchBuilder(
                                    buildCodeBlock {
                                        add("${fieldDescriptor.number} ->")
                                    }, this
                                )
                            ).advance()
                        }
                        addStatement("else -> getPropertyInExtensions(fieldNumber)")
                    }
                })
            }
        }
        return false
    }
}

open class MessageFieldGetPropertyFunctionGenerator :
    UniqueGenerator<MessageGetPropertyFunctionGeneratingState> {
    override fun generate(state: MessageGetPropertyFunctionGeneratingState): Boolean {
        state.target.codeBlock.apply {
            addStatement("%L %T::%N", state.target.branch, state.parent.parent.className(), state.descriptor.jsonName)
        }
        return true
    }
}

open class MessageSetFieldInCurrentFunctionGenerator :
    UniqueGenerator<MessageFunctionGeneratingState> {
    override fun generate(state: MessageFunctionGeneratingState): Boolean {
        state.target.apply {
            function("setFieldInCurrent") {
                this += KModifier.OVERRIDE
                addTypeVariable(TypeVariableName("T", ANY.copy(true)))
                addParameter("fieldName", String::class)
                addParameter("value", TypeVariableName("T"))
                addCode(buildCodeBlock {
                    beginScope("when(fieldName)") {
                        for (fieldDescriptor in state.descriptor.fieldList) {
                            if (fieldDescriptor.name != fieldDescriptor.jsonName) {
                                MessageSetFieldInCurrentFunctionGeneratingState(
                                    state, fieldDescriptor, WhenBranchBuilder(
                                        buildCodeBlock {
                                            add(
                                                "%S, %S ->",
                                                fieldDescriptor.name,
                                                fieldDescriptor.jsonName
                                            )
                                        }, this
                                    )
                                ).advance()
                            } else {
                                MessageSetFieldInCurrentFunctionGeneratingState(
                                    state, fieldDescriptor, WhenBranchBuilder(
                                        buildCodeBlock {
                                            add("%S ->", fieldDescriptor.name)
                                        }, this
                                    )
                                ).advance()
                            }
                        }
                        addStatement("else -> setFieldInExtensions(fieldName, value)")
                    }
                })
            }

            function("setFieldInCurrent") {
                this += KModifier.OVERRIDE
                addTypeVariable(TypeVariableName("T", ANY.copy(true)))
                addParameter("fieldNumber", Int::class)
                addParameter("value", TypeVariableName("T"))
                addCode(buildCodeBlock {
                    beginScope("when(fieldNumber)") {
                        for (fieldDescriptor in state.descriptor.fieldList) {
                            MessageSetFieldInCurrentFunctionGeneratingState(
                                state, fieldDescriptor, WhenBranchBuilder(
                                    buildCodeBlock {
                                        add("${fieldDescriptor.number} ->")
                                    }, this
                                )
                            ).advance()
                        }
                        addStatement("else -> setFieldInExtensions(fieldNumber, value)")
                    }
                })
            }
        }
        return false
    }
}

open class MessageFieldSetFieldInCurrentFunctionGenerator :
    UniqueGenerator<MessageSetFieldInCurrentFunctionGeneratingState> {
    override fun generate(state: MessageSetFieldInCurrentFunctionGeneratingState): Boolean {
        state.target.codeBlock.apply {
            if (state.descriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED) {
                beginScope("%L", state.target.branch) {
                    addStatement("this.%N.clear()", state.descriptor.jsonName)
                    addStatement(
                        "this.%N·+=·value.%M<%T>()", state.descriptor.jsonName,
                        RuntimeMethods.UNCHECK_CAST,
                        state.fieldType()
                    )
                }
            } else {
                addStatement(
                    "%L this.%N·=·value.%M()",
                    state.target.branch,
                    state.descriptor.jsonName,
                    RuntimeMethods.UNCHECK_CAST
                )
            }

        }
        return true
    }
}

open class MessageHasFieldInCurrentFunctionGenerator :
    UniqueGenerator<MessageFunctionGeneratingState> {
    override fun generate(state: MessageFunctionGeneratingState): Boolean {
        state.target.apply {
            function("hasFieldInCurrent") {
                this += KModifier.OVERRIDE
                addParameter("fieldName", String::class)
                returns(Boolean::class)
                addCode(buildCodeBlock {
                    beginScope("return when(fieldName)") {
                        for (fieldDescriptor in state.descriptor.fieldList) {
                            if (fieldDescriptor.name != fieldDescriptor.jsonName) {
                                MessageHasFieldInCurrentFunctionGeneratingState(
                                    state, fieldDescriptor, WhenBranchBuilder(
                                        buildCodeBlock {
                                            add(
                                                "%S, %S ->",
                                                fieldDescriptor.name,
                                                fieldDescriptor.jsonName
                                            )
                                        }, this
                                    )
                                ).advance()
                            } else {
                                MessageHasFieldInCurrentFunctionGeneratingState(
                                    state, fieldDescriptor, WhenBranchBuilder(
                                        buildCodeBlock {
                                            add("%S ->", fieldDescriptor.name)
                                        }, this
                                    )
                                ).advance()
                            }
                        }
                        addStatement("else -> hasFieldInExtensions(fieldName)")
                    }
                })
            }

            function("hasFieldInCurrent") {
                this += KModifier.OVERRIDE
                addParameter("fieldNumber", Int::class)
                returns(Boolean::class)
                addCode(buildCodeBlock {
                    beginScope("return when(fieldNumber)") {
                        for (fieldDescriptor in state.descriptor.fieldList) {
                            MessageHasFieldInCurrentFunctionGeneratingState(
                                state, fieldDescriptor, WhenBranchBuilder(
                                    buildCodeBlock {
                                        add("${fieldDescriptor.number} ->")
                                    }, this
                                )
                            ).advance()
                        }
                        addStatement("else -> hasFieldInExtensions(fieldNumber)")
                    }
                })
            }
        }
        return false
    }
}

open class MessageFieldHasFieldInCurrentFunctionGenerator :
    UniqueGenerator<MessageHasFieldInCurrentFunctionGeneratingState> {
    override fun generate(state: MessageHasFieldInCurrentFunctionGeneratingState): Boolean {
        state.target.codeBlock.apply {
            if (state.descriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED) {
                addStatement("%L true", state.target.branch)

            } else {
                addStatement("%L this.has${state.descriptor.jsonName.toPascalCase()}()", state.target.branch)
            }
        }
        return true
    }
}

open class MessageEqualsMessageFunctionGenerator :
    UniqueGenerator<MessageFunctionGeneratingState> {
    override fun generate(state: MessageFunctionGeneratingState): Boolean {
        state.target.apply {
            function("equalsMessage") {
                this += KModifier.OVERRIDE
                addParameter("other", state.parent.className())
                returns(Boolean::class)
                addCode(buildCodeBlock {
                    for (fieldDescriptor in state.descriptor.fieldList) {
                        MessageEqualsFunctionGeneratingState(state, fieldDescriptor, this)
                    }
                    addStatement("return true")
                })
            }
        }
        return false
    }
}

open class MessageFieldEqualsFunctionGenerator :
    UniqueGenerator<MessageEqualsFunctionGeneratingState> {

    override fun generate(state: MessageEqualsFunctionGeneratingState): Boolean {
        state.target.apply {
            when (state.descriptor.label) {
                DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED, DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL -> {
                    if (state.descriptor.type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES) {
                        addStatement(
                            "if (%N.contentEquals(other.%N)) return false",
                            state.descriptor.jsonName,
                            state.descriptor.jsonName
                        )
                    } else {
                        addStatement(
                            "if (%N != other.%N) return false",
                            state.descriptor.jsonName,
                            state.descriptor.jsonName
                        )
                    }
                }
                DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED -> {
                    addStatement(
                        "if (%N.%M(other.%N)) return false", state.descriptor.jsonName,
                        RuntimeMethods.CONTENT_EQUALS, state.descriptor.jsonName
                    )
                }
            }
        }
        return true
    }
}

open class MessageComputeHashCodeFunctionGenerator :
    UniqueGenerator<MessageFunctionGeneratingState> {
    override fun generate(state: MessageFunctionGeneratingState): Boolean {
        state.target.apply {
            function("computeHashCode") {
                this += KModifier.OVERRIDE
                returns(Int::class)
                addStatement("var result = this.javaClass.hashCode()")
                addCode(buildCodeBlock {
                    for (fieldDescriptor in state.descriptor.fieldList) {
                        MessageComputeHashCodeFunctionGeneratingState(state, fieldDescriptor, this).advance()
                    }
                    addStatement("return result")
                })
            }
        }
        return false
    }
}

open class MessageFieldComputeHashCodeFunctionGenerator :
    UniqueGenerator<MessageComputeHashCodeFunctionGeneratingState> {

    override fun generate(state: MessageComputeHashCodeFunctionGeneratingState): Boolean {
        state.target.apply {
            when (state.descriptor.label) {
                DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL -> {
                    beginScope("if (has${state.descriptor.jsonName.toPascalCase()}())") {
                        addStatement("result·=·result·*·37·+·${state.descriptor.number}")
                        if (state.descriptor.type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE) {
                            addStatement("result·=·result·*·31·+·this.%N!!.hashCode()", state.descriptor.jsonName)
                        } else {
                            addStatement("result·=·result·*·31·+·this.%N.hashCode()", state.descriptor.jsonName)
                        }
                    }
                }
                DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED -> {
                    addStatement("result·=·result·*·37·+·${state.descriptor.number}")
                    addStatement("result·=·result·*·31·+·this.%N.hashCode()", state.descriptor.jsonName)
                }
                DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED -> {
                    if (state.descriptor.type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE) {
                        val type = state.compiler().protoDescriptor(state.descriptor.typeName)
                        if (type.options?.mapEntry == true) {
                            beginScope("for ((key, value) in %N)", state.descriptor.jsonName) {
                                addStatement("result·=·result·*·37·+·${state.descriptor.number}")
                                addStatement("result·=·result·*·31·+·key.hashCode()")
                                addStatement("result·=·result·*·31·+·value.hashCode()")
                            }
                        }
                    } else {
                        beginScope("for (value in %N)", state.descriptor.jsonName) {
                            addStatement("result·=·result·*·37·+·${state.descriptor.number}")
                            addStatement("result·=·result·*·31·+·value.hashCode()")
                        }
                    }
                }
            }
        }
        return true
    }
}

open class MessageWriteFieldsFunctionGenerator :
    UniqueGenerator<MessageFunctionGeneratingState> {
    override fun generate(state: MessageFunctionGeneratingState): Boolean {
        state.target.apply {
            function("writeFields") {
                this += KModifier.OVERRIDE
                addParameter("writer", RuntimeTypes.WRITER)

                addCode(buildCodeBlock {
                    for (fieldDescriptor in state.descriptor.fieldList) {
                        MessageWriteFieldsFunctionGeneratingState(state, fieldDescriptor, this).advance()
                    }
                })
            }
        }
        return false
    }
}

open class MessageFieldWriteFunctionGenerator :
    UniqueGenerator<MessageWriteFieldsFunctionGeneratingState> {
    override fun generate(state: MessageWriteFieldsFunctionGeneratingState): Boolean {
        state.target.apply {
            val message = state.descriptor.type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE
            val enum = state.descriptor.type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM
            val repeated = state.descriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED
            val optional = state.descriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL
            val type = WireFormat.FieldType.values()[state.descriptor.type.ordinal]
            val packed = repeated && type.isPackable
            val writeMethod = type.name.toLowerCase()
            val any = state.descriptor.typeName == ".google.protobuf.Any"

            if (optional) {
                beginControlFlow("if (has${state.descriptor.jsonName.toPascalCase()}())")
            }
            when {
                packed -> addStatement(
                    "writer.tag(${
                        makeTag(
                            state.descriptor.number,
                            WireFormat.WIRETYPE_LENGTH_DELIMITED
                        )
                    }).beginLd().apply{ this@${state.parent.parent.implementationName()}.%N.forEach { $writeMethod(it) } }.endLd()",
                    state.descriptor.jsonName
                )
                repeated && message -> {
                    val typeDescriptor = state.compiler().protoDescriptor(state.descriptor.typeName)
                    if (typeDescriptor.options?.mapEntry == true) {
                        val keyType =
                            WireFormat.FieldType.values()[typeDescriptor.fieldList.first { it.number == 1 }.type.ordinal]
                        val valueType =
                            WireFormat.FieldType.values()[typeDescriptor.fieldList.first { it.number == 2 }.type.ordinal]

                        addStatement(
                            "this.%N.forEach { k, v -> writer.tag(${
                                makeTag(
                                    state.descriptor.number,
                                    WireFormat.WIRETYPE_LENGTH_DELIMITED
                                )
                            }).beginLd().tag(${
                                makeTag(
                                    1,
                                    keyType.wireType
                                )
                            }).${keyType.name.toLowerCase()}(k).tag(${
                                makeTag(
                                    2,
                                    valueType.wireType
                                )
                            }).${valueType.name.toLowerCase()}(v).endLd() }", state.descriptor.jsonName
                        )
                    } else {
                        addStatement(
                            "this.%N.forEach { writer.tag(${
                                makeTag(
                                    state.descriptor.number,
                                    WireFormat.WIRETYPE_LENGTH_DELIMITED
                                )
                            }).${if (any) "any" else "message"}(it) }", state.descriptor.jsonName
                        )
                    }
                }
                repeated -> addStatement(
                    "this.%N.forEach { writer.tag(${
                        makeTag(
                            state.descriptor.number,
                            type.wireType
                        )
                    }).$writeMethod(it) }", state.descriptor.jsonName
                )
                message -> addStatement(
                    "writer.tag(${
                        makeTag(
                            state.descriptor.number,
                            WireFormat.WIRETYPE_LENGTH_DELIMITED
                        )
                    }).${if (any) "any" else "message"}(this.%N)", state.descriptor.jsonName
                )
                else -> addStatement(
                    "writer.tag(${
                        makeTag(
                            state.descriptor.number,
                            type.wireType
                        )
                    }).$writeMethod(this.%N)", state.descriptor.jsonName
                )
            }
            if (optional) {
                endControlFlow()
            }
        }
        return true
    }
}

open class MessageReadFieldFunctionGenerator :
    UniqueGenerator<MessageFunctionGeneratingState> {
    override fun generate(state: MessageFunctionGeneratingState): Boolean {
        state.target.apply {
            function("readField") {
                annotation(RuntimeTypes.INTERNAL_PROTO_API)
                this += KModifier.OVERRIDE
                addParameter("reader", RuntimeTypes.READER)
                addParameter("field", Int::class)
                addParameter("wire", Int::class)
                returns(Boolean::class)

                addCode(buildCodeBlock {
                    beginScope("when(field)") {
                        for (fieldDescriptor in state.descriptor.fieldList) {
                            MessageReadFieldFunctionGeneratingState(state, fieldDescriptor, this).advance()
                        }
                        addStatement("else -> return false")
                    }
                    addStatement("return true")
                })
            }
        }
        return false
    }
}

open class MessageFieldReadFunctionGenerator :
    UniqueGenerator<MessageReadFieldFunctionGeneratingState> {
    override fun generate(state: MessageReadFieldFunctionGeneratingState): Boolean {
        state.target.apply {
            val message = state.descriptor.type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE
            val enum = state.descriptor.type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM
            val repeated = state.descriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED
            val type = WireFormat.FieldType.values()[state.descriptor.type.ordinal]
            val packed = repeated && type.isPackable
            val readMethod = type.name.toLowerCase()
            val any = state.descriptor.typeName == ".google.protobuf.Any"

            when {
                packed && enum -> addStatement(
                    "${state.descriptor.number} -> reader.packed(wire) { this.%N·+=·%T(it.int32()) }",
                    state.descriptor.jsonName,
                    state.compiler().protoClassName(state.descriptor.typeName)
                )
                packed -> addStatement(
                    "${state.descriptor.number} -> reader.packed(wire) { this.%N·+=·it.${readMethod}() }",
                    state.descriptor.jsonName
                )
                repeated && message -> {
                    val typeDescriptor = state.compiler().protoDescriptor(state.descriptor.typeName)
                    if (typeDescriptor.options?.mapEntry == true) {
                        val keyDescriptor = typeDescriptor.fieldList.first { it.number == 1 }
                        val keyType = WireFormat.FieldType.values()[keyDescriptor.type.ordinal]
                        val valueDescriptor = typeDescriptor.fieldList.first { it.number == 2 }
                        val valueType = WireFormat.FieldType.values()[valueDescriptor.type.ordinal]

                        when (valueType) {
                            WireFormat.FieldType.MESSAGE -> {
                                addStatement(
                                    "${state.descriptor.number} -> reader.mapEntry({ it.${keyType.name.toLowerCase()}() }, { %T.newMutable().apply { readFrom(reader) } }) { k,·v·-> this.%N[k]·=·v }",
                                    state.compiler().protoClassName(valueDescriptor.typeName), state.descriptor.jsonName
                                )
                            }
                            WireFormat.FieldType.ENUM -> {
                                addStatement(
                                    "${state.descriptor.number} -> reader.mapEntry({ it.${keyType.name.toLowerCase()}() }, { %T(it.int32()) }) { k,·v·-> this.%N[k]·=·v }",
                                    state.compiler().protoClassName(valueDescriptor.typeName), state.descriptor.jsonName
                                )
                            }
                            else -> {
                                addStatement(
                                    "${state.descriptor.number} -> reader.mapEntry({ it.${keyType.name.toLowerCase()}() }, { it.${valueType.name.toLowerCase()}() }) { k,·v·-> this.%N[k]·=·v }",
                                    state.descriptor.jsonName
                                )
                            }
                        }
                    } else if (any) {
                        addStatement("${state.descriptor.number} -> this.%N += reader.any()", state.descriptor.jsonName)
                    } else {
                        addStatement(
                            "${state.descriptor.number} -> this.%N += %T.newMutable().apply { readFrom(reader) }",
                            state.descriptor.jsonName,
                            state.compiler().protoClassName(state.descriptor.typeName)
                        )
                    }
                }
                repeated -> addStatement(
                    "${state.descriptor.number} -> this.%N += reader.${readMethod}()",
                    state.descriptor.jsonName
                )
                message -> if (any) {
                    addStatement("${state.descriptor.number} -> this.%N = reader.any()", state.descriptor.jsonName)
                } else {
                    addStatement(
                        "${state.descriptor.number} -> this.%N = %T.newMutable().apply { readFrom(reader) }",
                        state.descriptor.jsonName,
                        state.compiler().protoClassName(state.descriptor.typeName)
                    )
                }
                enum -> addStatement(
                    "${state.descriptor.number} -> this.%N = %T(reader.int32())", state.descriptor.jsonName,
                    state.compiler().protoClassName(state.descriptor.typeName)
                )
                else -> addStatement(
                    "${state.descriptor.number} -> this.%N = reader.${readMethod}()",
                    state.descriptor.jsonName
                )
            }
        }
        return true
    }
}