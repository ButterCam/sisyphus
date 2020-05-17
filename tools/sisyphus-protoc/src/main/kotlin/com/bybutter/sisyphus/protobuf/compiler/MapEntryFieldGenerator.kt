package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.protobuf.ProtoReader
import com.bybutter.sisyphus.protobuf.ProtoWriter
import com.bybutter.sisyphus.protobuf.Size
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

class MapEntryFieldGenerator(parent: MapEntryGenerator, descriptor: DescriptorProtos.FieldDescriptorProto) : FieldGenerator(parent, descriptor) {
    override val nullable = false

    override fun defaultValue(): String {
        if (descriptor.isRepeated) {
            return if (typeElement is MapEntryGenerator) {
                "mapOf()"
            } else {
                "listOf()"
            }
        }

        return when (descriptor.type) {
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE -> "0.0"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT -> "0.0f"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT64,
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED64,
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64 -> "0L"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64,
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64 -> "0UL"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT32,
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED32,
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32 -> "0"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL -> "false"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING -> "\"\""
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES -> "byteArrayOf()"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32,
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32 -> "0U"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM -> "$valueType()"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_GROUP -> throw UnsupportedOperationException("Group is not supported by butter proto.")
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE -> "$valueType()"
        }
    }

    override fun applyToMessage(builder: TypeSpec.Builder) {
        builder.addProperty(
            PropertySpec.builder(kotlinName, valueType)
                .addKdoc(escapeDoc(documentation))
                .addModifiers(KModifier.OVERRIDE)
                .addAnnotations(generateAnnotations())
                .build()
        )
    }

    override fun applyToMutable(builder: TypeSpec.Builder) {
        builder.addProperty(
            PropertySpec.builder(kotlinName, valueType)
                .mutable()
                .addModifiers(KModifier.OVERRIDE)
                .addKdoc(escapeDoc(documentation))
                .addAnnotations(generateAnnotations())
                .build()
        )
    }

    override fun applyToImpl(builder: TypeSpec.Builder) {
        builder.addProperty(
            PropertySpec.builder("_$kotlinName", valueType.copy(true), KModifier.PRIVATE)
                .mutable()
                .initializer("null")
                .build()
        )
        builder.addProperty(
            PropertySpec.builder(kotlinName, valueType, KModifier.OVERRIDE)
                .mutable()
                .getter(
                    FunSpec.getterBuilder()
                        .addStatement("return %N!!", "_$kotlinName")
                        .build()
                )
                .setter(
                    FunSpec.setterBuilder()
                        .addParameter("value", valueType)
                        .addStatement("%N = value", "_$kotlinName")
                        .addStatement("invalidCache()")
                        .build()
                )
                .build()
        )
    }

    override fun applyToMergeWithFun(builder: FunSpec.Builder, other: String) {
        builder.addStatement("this.%N = %N.%N", kotlinName, other, kotlinName)
    }

    override fun applyToClearByNameFun(builder: FunSpec.Builder) {
        builder.addStatement("%S -> throw %T(%S)", protoName, IllegalStateException::class, "Can't remove required field")
    }

    override fun applyToClearByNumberFun(builder: FunSpec.Builder) {
        builder.addStatement("%L -> throw %T(%S)", descriptor.number, IllegalStateException::class, "Can't remove required field")
    }

    override fun applyToHasByNameFun(builder: FunSpec.Builder) {
        builder.addStatement("%S -> true", protoName)
    }

    override fun applyToHasByNumberFun(builder: FunSpec.Builder) {
        builder.addStatement("%L -> true", descriptor.number)
    }

    override fun applyToEqualsFun(builder: FunSpec.Builder, other: String) {
        builder.beginControlFlow("if(this.%N != %N.%N)", kotlinName, other, kotlinName)
        builder.addStatement("return false")
        builder.endControlFlow()
    }

    override fun applyToComputeSizeFun(builder: FunSpec.Builder) {
        builder.addStatement("result += %T.of${getTypeName()}(${descriptor.number}, this.%N)", Size::class, kotlinName)
    }

    override fun applyToWriteFieldsFun(builder: FunSpec.Builder) {
        builder.addStatement("%T.write${getTypeName()}(output, ${descriptor.number}, this.%N)", ProtoWriter::class, kotlinName)
    }

    override fun applyToReadFieldFun(builder: FunSpec.Builder) {
        when {
            getTypeName() == "Message" -> {
                builder.addStatement("%L -> this.%N = %T.readMessage(input, %T::class.java, field, wire, input.readInt32())", descriptor.number, kotlinName, ProtoReader::class, kotlinType)
            }
            getTypeName() == "Any" -> {
                builder.addStatement("%L -> this.%N = %T.readAny(input, field, wire, input.readInt32())", descriptor.number, kotlinName, ProtoReader::class)
            }
            getTypeName() == "Enum" -> {
                builder.addStatement("%L -> %T.readEnum(input, %T::class.java, field, wire)?.let{ this.%N = it }", descriptor.number, ProtoReader::class, kotlinType, kotlinName)
            }
            else -> {
                builder.addStatement("%L -> %T.read${getTypeName()}(input, field, wire)?.let { this.%N = it }", descriptor.number, ProtoReader::class, kotlinName)
            }
        }
    }

    override fun applyToComputeHashCode(builder: FunSpec.Builder) {
        builder.addStatement("result·=·result·*37·+·${descriptor.number}")
        builder.addStatement("result = result·*31·+·this.%N.hashCode()", kotlinName)
    }

    override fun applyToClearFun(builder: FunSpec.Builder) {
    }

    fun applyToWritePairFun(builder: FunSpec.Builder) {
        builder.addStatement("%T.write${getTypeName()}(output, ${descriptor.number}, entry.%N)", ProtoWriter::class, kotlinName)
    }

    fun applyToSizeOfPairFun(builder: FunSpec.Builder) {
        builder.addStatement("result += %T.of${getTypeName()}(${descriptor.number}, entry.%N)", Size::class, kotlinName)
    }
}
