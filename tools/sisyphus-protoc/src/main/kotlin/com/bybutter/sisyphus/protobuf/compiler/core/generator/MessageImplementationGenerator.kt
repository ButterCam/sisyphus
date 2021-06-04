package com.bybutter.sisyphus.protobuf.compiler.core.generator

import com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator
import com.bybutter.sisyphus.protobuf.compiler.RuntimeMethods
import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.WhenBranchBuilder
import com.bybutter.sisyphus.protobuf.compiler.annotation
import com.bybutter.sisyphus.protobuf.compiler.beginScope
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageClearInCurrentFunctionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageEqualsFunctionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageGetInCurrentFunctionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageHasFieldInCurrentFunctionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageHashCodeFunctionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageImplementationGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageReadFieldFunctionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageSetFieldInCurrentFunctionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageWriteFieldsFunctionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.advance
import com.bybutter.sisyphus.protobuf.compiler.elementType
import com.bybutter.sisyphus.protobuf.compiler.enumType
import com.bybutter.sisyphus.protobuf.compiler.function
import com.bybutter.sisyphus.protobuf.compiler.hasFunction
import com.bybutter.sisyphus.protobuf.compiler.mapEntry
import com.bybutter.sisyphus.protobuf.compiler.messageType
import com.bybutter.sisyphus.protobuf.compiler.name
import com.bybutter.sisyphus.protobuf.compiler.plusAssign
import com.bybutter.sisyphus.protobuf.compiler.util.makeTag
import com.bybutter.sisyphus.string.toPascalCase
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.WireFormat
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.buildCodeBlock

class MessageSupportFunctionGenerator : GroupedGenerator<MessageImplementationGeneratingState> {
    override fun generate(state: MessageImplementationGeneratingState): Boolean {
        state.target.apply {
            function("support") {
                this += KModifier.OVERRIDE
                returns(
                    RuntimeTypes.MESSAGE_SUPPORT.parameterizedBy(
                        state.descriptor.className(),
                        state.descriptor.mutableClassName()
                    )
                )
                addStatement("return %T", state.descriptor.className())
            }
        }
        return true
    }
}

class MessageMergeWithFunctionGenerator : GroupedGenerator<MessageImplementationGeneratingState> {
    override fun generate(state: MessageImplementationGeneratingState): Boolean {
        state.target.apply {
            function("mergeWith") {
                this += KModifier.OVERRIDE
                addParameter("other", state.descriptor.className().copy(true))
                addStatement("other ?: return")
                addStatement("val proto = other.toProto()")
                addStatement("readFrom(Reader(proto.inputStream()), proto.size)")
            }
        }
        return true
    }
}

class MessageCloneMutableFunctionGenerator : GroupedGenerator<MessageImplementationGeneratingState> {
    override fun generate(state: MessageImplementationGeneratingState): Boolean {
        state.target.apply {
            function("cloneMutable") {
                annotation(RuntimeTypes.INTERNAL_PROTO_API)

                this += KModifier.OVERRIDE
                returns(state.descriptor.mutableClassName())
                beginControlFlow("return %T().apply", state.descriptor.implementationClassName())
                addStatement("mergeWith(this@%T)", state.descriptor.implementationClassName())
                endControlFlow()
            }
        }
        return true
    }
}

class MessageClearFunctionGenerator : GroupedGenerator<MessageImplementationGeneratingState> {
    override fun generate(state: MessageImplementationGeneratingState): Boolean {
        state.target.apply {
            function("clear") {
                this += KModifier.OVERRIDE
                for (field in state.descriptor.fields) {
                    if (field.descriptor.label != DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED) {
                        addStatement("this.clear${field.descriptor.jsonName.toPascalCase()}()")
                    }
                }
            }
        }
        return true
    }
}

class MessageClearFieldInCurrentFunctionGenerator : GroupedGenerator<MessageImplementationGeneratingState> {
    override fun generate(state: MessageImplementationGeneratingState): Boolean {
        state.target.apply {
            function("clearFieldInCurrent") {
                this += KModifier.OVERRIDE
                addParameter("fieldName", String::class)
                returns(ANY.copy(true))
                addCode(
                    buildCodeBlock {
                        if (state.descriptor.fields.isEmpty()) {
                            addStatement("return clearFieldInExtensions(fieldName)")
                            return@buildCodeBlock
                        }
                        beginScope("return when(fieldName)") {
                            for (field in state.descriptor.fields) {
                                if (field.descriptor.name != field.descriptor.jsonName) {
                                    MessageClearInCurrentFunctionGeneratingState(
                                        state, field,
                                        WhenBranchBuilder(
                                            buildCodeBlock {
                                                add(
                                                    "%S, %S ->",
                                                    field.descriptor.name,
                                                    field.descriptor.jsonName
                                                )
                                            },
                                            this
                                        )
                                    ).advance()
                                } else {
                                    MessageClearInCurrentFunctionGeneratingState(
                                        state, field,
                                        WhenBranchBuilder(
                                            buildCodeBlock {
                                                add("%S ->", field.descriptor.name)
                                            },
                                            this
                                        )
                                    ).advance()
                                }
                            }
                            addStatement("else -> clearFieldInExtensions(fieldName)")
                        }
                    }
                )
            }

            function("clearFieldInCurrent") {
                this += KModifier.OVERRIDE
                addParameter("fieldNumber", Int::class)
                returns(ANY.copy(true))
                addCode(
                    buildCodeBlock {
                        if (state.descriptor.fields.isEmpty()) {
                            addStatement("return clearFieldInExtensions(fieldNumber)")
                            return@buildCodeBlock
                        }
                        beginScope("return when(fieldNumber)") {
                            for (field in state.descriptor.fields) {
                                MessageClearInCurrentFunctionGeneratingState(
                                    state, field,
                                    WhenBranchBuilder(
                                        buildCodeBlock {
                                            add("${field.descriptor.number} ->")
                                        },
                                        this
                                    )
                                ).advance()
                            }
                            addStatement("else -> clearFieldInExtensions(fieldNumber)")
                        }
                    }
                )
            }
        }
        return true
    }
}

class MessageFieldClearFieldInCurrentFunctionGenerator :
    GroupedGenerator<MessageClearInCurrentFunctionGeneratingState> {
    override fun generate(state: MessageClearInCurrentFunctionGeneratingState): Boolean {
        state.target.codeBlock.apply {
            if (state.descriptor.descriptor.label != DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED) {
                addStatement(
                    "%L this.clear${state.descriptor.name().toPascalCase()}()",
                    state.target.branch
                )
            } else {
                addStatement(
                    "%L throw %T(%S)",
                    state.target.branch,
                    IllegalArgumentException::class,
                    "Field '${state.descriptor.descriptor.name}' is required field can't be clear."
                )
            }
        }
        return true
    }
}

class MessageGetFieldInCurrentFunctionGenerator : GroupedGenerator<MessageImplementationGeneratingState> {
    override fun generate(state: MessageImplementationGeneratingState): Boolean {
        state.target.apply {
            function("getFieldInCurrent") {
                this += KModifier.OVERRIDE
                addTypeVariable(TypeVariableName("T", ANY.copy(true)))
                addParameter("fieldName", String::class)
                returns(TypeVariableName("T"))
                addCode(
                    buildCodeBlock {
                        if (state.descriptor.fields.isEmpty()) {
                            addStatement("return getFieldInExtensions(fieldName)")
                            return@buildCodeBlock
                        }
                        beginScope("return when(fieldName)") {
                            for (field in state.descriptor.fields) {
                                if (field.descriptor.name != field.descriptor.jsonName) {
                                    MessageGetInCurrentFunctionGeneratingState(
                                        state, field,
                                        WhenBranchBuilder(
                                            buildCodeBlock {
                                                add(
                                                    "%S, %S ->",
                                                    field.descriptor.name,
                                                    field.descriptor.jsonName
                                                )
                                            },
                                            this
                                        )
                                    ).advance()
                                } else {
                                    MessageGetInCurrentFunctionGeneratingState(
                                        state, field,
                                        WhenBranchBuilder(
                                            buildCodeBlock {
                                                add("%S ->", field.descriptor.name)
                                            },
                                            this
                                        )
                                    ).advance()
                                }
                            }
                            addStatement("else -> getFieldInExtensions(fieldName)")
                        }
                    }
                )
            }

            function("getFieldInCurrent") {
                this += KModifier.OVERRIDE
                addTypeVariable(TypeVariableName("T", ANY.copy(true)))
                addParameter("fieldNumber", Int::class)
                returns(TypeVariableName("T"))
                addCode(
                    buildCodeBlock {
                        if (state.descriptor.fields.isEmpty()) {
                            addStatement("return getFieldInExtensions(fieldNumber)")
                            return@buildCodeBlock
                        }
                        beginScope("return when(fieldNumber)") {
                            for (field in state.descriptor.fields) {
                                MessageGetInCurrentFunctionGeneratingState(
                                    state, field,
                                    WhenBranchBuilder(
                                        buildCodeBlock {
                                            add("${field.descriptor.number} ->")
                                        },
                                        this
                                    )
                                ).advance()
                            }
                            addStatement("else -> getFieldInExtensions(fieldNumber)")
                        }
                    }
                )
            }
        }
        return true
    }
}

class MessageFieldGetFieldInCurrentFunctionGenerator : GroupedGenerator<MessageGetInCurrentFunctionGeneratingState> {
    override fun generate(state: MessageGetInCurrentFunctionGeneratingState): Boolean {
        state.target.codeBlock.apply {
            addStatement("%L this.%N.%M()", state.target.branch, state.descriptor.name(), RuntimeMethods.UNCHECK_CAST)
        }
        return true
    }
}

class MessageSetFieldInCurrentFunctionGenerator : GroupedGenerator<MessageImplementationGeneratingState> {
    override fun generate(state: MessageImplementationGeneratingState): Boolean {
        state.target.apply {
            function("setFieldInCurrent") {
                this += KModifier.OVERRIDE
                addTypeVariable(TypeVariableName("T", ANY.copy(true)))
                addParameter("fieldName", String::class)
                addParameter("value", TypeVariableName("T"))
                addCode(
                    buildCodeBlock {
                        if (state.descriptor.fields.isEmpty()) {
                            addStatement("setFieldInExtensions(fieldName, value)")
                            return@buildCodeBlock
                        }
                        beginScope("when(fieldName)") {
                            for (field in state.descriptor.fields) {
                                if (field.descriptor.name != field.descriptor.jsonName) {
                                    MessageSetFieldInCurrentFunctionGeneratingState(
                                        state, field,
                                        WhenBranchBuilder(
                                            buildCodeBlock {
                                                add(
                                                    "%S, %S ->",
                                                    field.descriptor.name,
                                                    field.descriptor.jsonName
                                                )
                                            },
                                            this
                                        )
                                    ).advance()
                                } else {
                                    MessageSetFieldInCurrentFunctionGeneratingState(
                                        state, field,
                                        WhenBranchBuilder(
                                            buildCodeBlock {
                                                add("%S ->", field.descriptor.name)
                                            },
                                            this
                                        )
                                    ).advance()
                                }
                            }
                            addStatement("else -> setFieldInExtensions(fieldName, value)")
                        }
                    }
                )
            }

            function("setFieldInCurrent") {
                this += KModifier.OVERRIDE
                addTypeVariable(TypeVariableName("T", ANY.copy(true)))
                addParameter("fieldNumber", Int::class)
                addParameter("value", TypeVariableName("T"))
                addCode(
                    buildCodeBlock {
                        if (state.descriptor.fields.isEmpty()) {
                            addStatement("setFieldInExtensions(fieldNumber, value)")
                            return@buildCodeBlock
                        }
                        beginScope("when(fieldNumber)") {
                            for (field in state.descriptor.fields) {
                                MessageSetFieldInCurrentFunctionGeneratingState(
                                    state, field,
                                    WhenBranchBuilder(
                                        buildCodeBlock {
                                            add("${field.descriptor.number} ->")
                                        },
                                        this
                                    )
                                ).advance()
                            }
                            addStatement("else -> setFieldInExtensions(fieldNumber, value)")
                        }
                    }
                )
            }
        }
        return true
    }
}

class MessageFieldSetFieldInCurrentFunctionGenerator :
    GroupedGenerator<MessageSetFieldInCurrentFunctionGeneratingState> {
    override fun generate(state: MessageSetFieldInCurrentFunctionGeneratingState): Boolean {
        state.target.codeBlock.apply {
            if (state.descriptor.descriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED) {
                beginScope("%L", state.target.branch) {
                    addStatement("this.%N.clear()", state.descriptor.name())
                    if (state.descriptor.mapEntry() == null) {
                        addStatement(
                            "this.%N.addAll(value.%M())", state.descriptor.name(),
                            RuntimeMethods.UNCHECK_CAST
                        )
                    } else {
                        addStatement(
                            "this.%N.putAll(value.%M())", state.descriptor.name(),
                            RuntimeMethods.UNCHECK_CAST
                        )
                    }
                }
            } else {
                addStatement(
                    "%L this.%N·=·value.%M()",
                    state.target.branch,
                    state.descriptor.name(),
                    RuntimeMethods.UNCHECK_CAST
                )
            }
        }
        return true
    }
}

class MessageHasFieldInCurrentFunctionGenerator : GroupedGenerator<MessageImplementationGeneratingState> {
    override fun generate(state: MessageImplementationGeneratingState): Boolean {
        state.target.apply {
            function("hasFieldInCurrent") {
                this += KModifier.OVERRIDE
                addParameter("fieldName", String::class)
                returns(Boolean::class)
                addCode(
                    buildCodeBlock {
                        if (state.descriptor.fields.isEmpty()) {
                            addStatement("return hasFieldInExtensions(fieldName)")
                            return@buildCodeBlock
                        }
                        beginScope("return when(fieldName)") {
                            for (field in state.descriptor.fields) {
                                if (field.descriptor.name != field.descriptor.jsonName) {
                                    MessageHasFieldInCurrentFunctionGeneratingState(
                                        state, field,
                                        WhenBranchBuilder(
                                            buildCodeBlock {
                                                add(
                                                    "%S, %S ->",
                                                    field.descriptor.name,
                                                    field.descriptor.jsonName
                                                )
                                            },
                                            this
                                        )
                                    ).advance()
                                } else {
                                    MessageHasFieldInCurrentFunctionGeneratingState(
                                        state, field,
                                        WhenBranchBuilder(
                                            buildCodeBlock {
                                                add("%S ->", field.descriptor.name)
                                            },
                                            this
                                        )
                                    ).advance()
                                }
                            }
                            addStatement("else -> hasFieldInExtensions(fieldName)")
                        }
                    }
                )
            }

            function("hasFieldInCurrent") {
                this += KModifier.OVERRIDE
                addParameter("fieldNumber", Int::class)
                returns(Boolean::class)
                addCode(
                    buildCodeBlock {
                        if (state.descriptor.fields.isEmpty()) {
                            addStatement("return hasFieldInExtensions(fieldNumber)")
                            return@buildCodeBlock
                        }
                        beginScope("return when(fieldNumber)") {
                            for (field in state.descriptor.fields) {
                                MessageHasFieldInCurrentFunctionGeneratingState(
                                    state, field,
                                    WhenBranchBuilder(
                                        buildCodeBlock {
                                            add("${field.descriptor.number} ->")
                                        },
                                        this
                                    )
                                ).advance()
                            }
                            addStatement("else -> hasFieldInExtensions(fieldNumber)")
                        }
                    }
                )
            }
        }
        return true
    }
}

class MessageFieldHasFieldInCurrentFunctionGenerator :
    GroupedGenerator<MessageHasFieldInCurrentFunctionGeneratingState> {
    override fun generate(state: MessageHasFieldInCurrentFunctionGeneratingState): Boolean {
        state.target.codeBlock.apply {
            if (state.descriptor.descriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED) {
                addStatement("%L true", state.target.branch)
            } else {
                addStatement("%L this.has${state.descriptor.name().toPascalCase()}()", state.target.branch)
            }
        }
        return true
    }
}

class MessageEqualsMessageFunctionGenerator : GroupedGenerator<MessageImplementationGeneratingState> {
    override fun generate(state: MessageImplementationGeneratingState): Boolean {
        state.target.apply {
            function("equalsMessage") {
                this += KModifier.OVERRIDE
                addParameter("other", state.descriptor.className())
                returns(Boolean::class)
                addCode(
                    buildCodeBlock {
                        for (field in state.descriptor.fields) {
                            MessageEqualsFunctionGeneratingState(state, field, this).advance()
                        }
                        addStatement("return true")
                    }
                )
            }
        }
        return true
    }
}

class MessageFieldEqualsFunctionGenerator : GroupedGenerator<MessageEqualsFunctionGeneratingState> {
    override fun generate(state: MessageEqualsFunctionGeneratingState): Boolean {
        state.target.apply {
            when (state.descriptor.descriptor.label) {
                DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED, DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL -> {
                    if (state.descriptor.descriptor.type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES) {
                        addStatement(
                            "if (!%N.contentEquals(other.%N)) return false",
                            state.descriptor.name(),
                            state.descriptor.name()
                        )
                    } else {
                        addStatement(
                            "if (%N != other.%N) return false",
                            state.descriptor.name(),
                            state.descriptor.name()
                        )
                    }
                }
                DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED -> {
                    addStatement(
                        "if (!%N.%M(other.%N)) return false", state.descriptor.name(),
                        RuntimeMethods.CONTENT_EQUALS, state.descriptor.name()
                    )
                }
            }
        }
        return true
    }
}

class MessageComputeHashCodeFunctionGenerator : GroupedGenerator<MessageImplementationGeneratingState> {
    override fun generate(state: MessageImplementationGeneratingState): Boolean {
        state.target.apply {
            function("computeHashCode") {
                this += KModifier.OVERRIDE
                returns(Int::class)
                addCode(
                    buildCodeBlock {
                        if (state.descriptor.fields.isEmpty()) {
                            addStatement("return this.javaClass.hashCode()")
                            return@buildCodeBlock
                        }
                        addStatement("var result = this.javaClass.hashCode()")
                        for (field in state.descriptor.fields) {
                            MessageHashCodeFunctionGeneratingState(state, field, this).advance()
                        }
                        addStatement("return result")
                    }
                )
            }
        }
        return true
    }
}

class MessageFieldComputeHashCodeFunctionGenerator : GroupedGenerator<MessageHashCodeFunctionGeneratingState> {
    override fun generate(state: MessageHashCodeFunctionGeneratingState): Boolean {
        state.target.apply {
            when (state.descriptor.descriptor.label) {
                DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL -> {
                    beginScope("if (has${state.descriptor.name().toPascalCase()}())") {
                        addStatement("result·=·result·*·37·+·${state.descriptor.descriptor.number}")
                        if (state.descriptor.descriptor.type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE) {
                            addStatement("result·=·result·*·31·+·this.%N!!.hashCode()", state.descriptor.name())
                        } else {
                            addStatement("result·=·result·*·31·+·this.%N.hashCode()", state.descriptor.name())
                        }
                    }
                }
                DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED -> {
                    addStatement("result·=·result·*·37·+·${state.descriptor.descriptor.number}")
                    addStatement("result·=·result·*·31·+·this.%N.hashCode()", state.descriptor.name())
                }
                DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED -> {
                    if (state.descriptor.descriptor.type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE) {
                        if (state.descriptor.mapEntry() != null) {
                            beginScope("for ((key, value) in %N)", state.descriptor.name()) {
                                addStatement("result·=·result·*·37·+·${state.descriptor.descriptor.number}")
                                addStatement("result·=·result·*·31·+·key.hashCode()")
                                addStatement("result·=·result·*·31·+·value.hashCode()")
                            }
                            return true
                        }
                    }
                    beginScope("for (value in %N)", state.descriptor.name()) {
                        addStatement("result·=·result·*·37·+·${state.descriptor.descriptor.number}")
                        addStatement("result·=·result·*·31·+·value.hashCode()")
                    }
                }
            }
        }
        return true
    }
}

class MessageWriteFieldsFunctionGenerator : GroupedGenerator<MessageImplementationGeneratingState> {
    override fun generate(state: MessageImplementationGeneratingState): Boolean {
        state.target.apply {
            function("writeFields") {
                this += KModifier.OVERRIDE
                addParameter("writer", RuntimeTypes.WRITER)

                addCode(
                    buildCodeBlock {
                        for (field in state.descriptor.fields) {
                            MessageWriteFieldsFunctionGeneratingState(state, field, this).advance()
                        }
                    }
                )
            }
        }
        return true
    }
}

class MessageFieldWriteFunctionGenerator : GroupedGenerator<MessageWriteFieldsFunctionGeneratingState> {
    override fun generate(state: MessageWriteFieldsFunctionGeneratingState): Boolean {
        state.target.apply {
            val message = state.descriptor.descriptor.type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE
            val enum = state.descriptor.descriptor.type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM
            val repeated =
                state.descriptor.descriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED
            val optional =
                state.descriptor.descriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL
            val type = WireFormat.FieldType.values()[state.descriptor.descriptor.type.ordinal]
            val packed = repeated && type.isPackable
            val writeMethod = type.name.toLowerCase()
            val any = state.descriptor.descriptor.typeName == ".google.protobuf.Any"

            if (optional) {
                beginControlFlow("if (${state.descriptor.hasFunction()}())")
            }
            when {
                packed -> addStatement(
                    "writer.tag(${
                    makeTag(
                        state.descriptor.descriptor.number,
                        WireFormat.WIRETYPE_LENGTH_DELIMITED
                    )
                    }).beginLd().apply{ this@${state.descriptor.parent.implementationName()}.%N.forEach { $writeMethod(it) } }.endLd()",
                    state.descriptor.name()
                )
                repeated && message -> {
                    val typeDescriptor = state.descriptor.messageType()!!
                    if (typeDescriptor.mapEntry()) {
                        val keyType =
                            WireFormat.FieldType.values()[typeDescriptor.fields.first { it.descriptor.number == 1 }.descriptor.type.ordinal]
                        val valueDescriptor = typeDescriptor.fields.first { it.descriptor.number == 2 }.descriptor
                        val anyValue = valueDescriptor.typeName == ".google.protobuf.Any"
                        val valueType = WireFormat.FieldType.values()[valueDescriptor.type.ordinal]

                        addStatement(
                            "this.%N.forEach { (k, v) -> writer.tag(${
                            makeTag(
                                state.descriptor.descriptor.number,
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
                            }).${if (anyValue) "any" else valueType.name.toLowerCase()}(v).endLd() }",
                            state.descriptor.name()
                        )
                    } else {
                        addStatement(
                            "this.%N.forEach { writer.tag(${
                            makeTag(
                                state.descriptor.descriptor.number,
                                WireFormat.WIRETYPE_LENGTH_DELIMITED
                            )
                            }).${if (any) "any" else "message"}(it) }",
                            state.descriptor.name()
                        )
                    }
                }
                repeated -> addStatement(
                    "this.%N.forEach { writer.tag(${
                    makeTag(
                        state.descriptor.descriptor.number,
                        type.wireType
                    )
                    }).$writeMethod(it) }",
                    state.descriptor.name()
                )
                message -> addStatement(
                    "writer.tag(${
                    makeTag(
                        state.descriptor.descriptor.number,
                        WireFormat.WIRETYPE_LENGTH_DELIMITED
                    )
                    }).${if (any) "any" else "message"}(this.%N)",
                    state.descriptor.name()
                )
                else -> addStatement(
                    "writer.tag(${
                    makeTag(
                        state.descriptor.descriptor.number,
                        type.wireType
                    )
                    }).$writeMethod(this.%N)",
                    state.descriptor.name()
                )
            }
            if (optional) {
                endControlFlow()
            }
        }
        return true
    }
}

class MessageReadFieldFunctionGenerator : GroupedGenerator<MessageImplementationGeneratingState> {
    override fun generate(state: MessageImplementationGeneratingState): Boolean {
        state.target.apply {
            function("readField") {
                annotation(RuntimeTypes.INTERNAL_PROTO_API)
                this += KModifier.OVERRIDE
                addParameter("reader", RuntimeTypes.READER)
                addParameter("field", Int::class)
                addParameter("wire", Int::class)
                returns(Boolean::class)

                addCode(
                    buildCodeBlock {
                        if (state.descriptor.fields.isEmpty()) {
                            addStatement("return false")
                            return@buildCodeBlock
                        }
                        beginScope("when(field)") {
                            for (field in state.descriptor.fields) {
                                MessageReadFieldFunctionGeneratingState(state, field, this).advance()
                            }
                            addStatement("else -> return false")
                        }
                        addStatement("return true")
                    }
                )
            }
        }
        return true
    }
}

class MessageFieldReadFunctionGenerator : GroupedGenerator<MessageReadFieldFunctionGeneratingState> {
    override fun generate(state: MessageReadFieldFunctionGeneratingState): Boolean {
        state.target.apply {
            val message = state.descriptor.descriptor.type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE
            val enum = state.descriptor.descriptor.type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM
            val repeated =
                state.descriptor.descriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED
            val type = WireFormat.FieldType.values()[state.descriptor.descriptor.type.ordinal]
            val packed = repeated && type.isPackable
            val readMethod = type.name.toLowerCase()
            val any = state.descriptor.descriptor.typeName == ".google.protobuf.Any"

            when {
                packed && enum -> addStatement(
                    "${state.descriptor.descriptor.number} -> reader.packed(wire) { this.%N·+=·%T(it.int32()) }",
                    state.descriptor.name(),
                    state.descriptor.enumType()?.className()
                )
                packed -> addStatement(
                    "${state.descriptor.descriptor.number} -> reader.packed(wire) { this.%N·+=·it.$readMethod() }",
                    state.descriptor.name()
                )
                repeated && message -> {
                    val typeDescriptor = state.descriptor.messageType()!!
                    if (typeDescriptor.mapEntry()) {
                        val keyDescriptor = typeDescriptor.fields.first { it.descriptor.number == 1 }
                        val keyType = WireFormat.FieldType.values()[keyDescriptor.descriptor.type.ordinal]
                        val valueDescriptor = typeDescriptor.fields.first { it.descriptor.number == 2 }
                        val valueType = WireFormat.FieldType.values()[valueDescriptor.descriptor.type.ordinal]

                        when (valueType) {
                            WireFormat.FieldType.MESSAGE -> {
                                if (valueDescriptor.messageType()?.fullProtoName() == ".google.protobuf.Any") {
                                    addStatement(
                                        "${state.descriptor.descriptor.number} -> reader.mapEntry({ it.${keyType.name.toLowerCase()}() }, { reader.any() }) { k,·v·-> this.%N[k]·=·v }",
                                        state.descriptor.name()
                                    )
                                } else {
                                    addStatement(
                                        "${state.descriptor.descriptor.number} -> reader.mapEntry({ it.${keyType.name.toLowerCase()}() }, { %T.newMutable().apply { readFrom(reader) } }) { k,·v·-> this.%N[k]·=·v }",
                                        valueDescriptor.elementType(), state.descriptor.name()
                                    )
                                }
                            }
                            WireFormat.FieldType.ENUM -> {
                                addStatement(
                                    "${state.descriptor.descriptor.number} -> reader.mapEntry({ it.${keyType.name.toLowerCase()}() }, { %T(it.int32()) }) { k,·v·-> this.%N[k]·=·v }",
                                    valueDescriptor.elementType(), state.descriptor.name()
                                )
                            }
                            else -> {
                                addStatement(
                                    "${state.descriptor.descriptor.number} -> reader.mapEntry({ it.${keyType.name.toLowerCase()}() }, { it.${valueType.name.toLowerCase()}() }) { k,·v·-> this.%N[k]·=·v }",
                                    state.descriptor.name()
                                )
                            }
                        }
                    } else if (any) {
                        addStatement(
                            "${state.descriptor.descriptor.number} -> this.%N += reader.any()",
                            state.descriptor.name()
                        )
                    } else {
                        addStatement(
                            "${state.descriptor.descriptor.number} -> this.%N += %T.newMutable().apply { readFrom(reader) }",
                            state.descriptor.name(),
                            state.descriptor.elementType()
                        )
                    }
                }
                repeated -> addStatement(
                    "${state.descriptor.descriptor.number} -> this.%N += reader.$readMethod()",
                    state.descriptor.name()
                )
                message -> if (any) {
                    addStatement(
                        "${state.descriptor.descriptor.number} -> this.%N = reader.any()",
                        state.descriptor.name()
                    )
                } else {
                    addStatement(
                        "${state.descriptor.descriptor.number} -> this.%N = %T.newMutable().apply { readFrom(reader) }",
                        state.descriptor.name(),
                        state.descriptor.elementType()
                    )
                }
                enum -> addStatement(
                    "${state.descriptor.descriptor.number} -> this.%N = %T(reader.int32())", state.descriptor.name(),
                    state.descriptor.elementType()
                )
                else -> addStatement(
                    "${state.descriptor.descriptor.number} -> this.%N = reader.$readMethod()",
                    state.descriptor.name()
                )
            }
        }
        return true
    }
}
