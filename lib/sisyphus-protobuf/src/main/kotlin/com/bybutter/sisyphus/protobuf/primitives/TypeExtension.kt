package com.bybutter.sisyphus.protobuf.primitives

import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoTypes

fun DescriptorProto.toType(typeName: String): Type {
    return Type {
        val fileInfo = ProtoTypes.getFileDescriptorByName(typeName)
        this.name = this@toType.name
        this.fields += this@toType.field.map { it.toField() }
        this.fields += ProtoTypes.getTypeExtensions(typeName).mapNotNull { ProtoTypes.getExtensionDescriptor(typeName, it)?.toField() }
        this.oneofs += this@toType.oneofDecl.map { it.name }
        this@toType.options?.let {
            this.options += it.toOptions()
        }
        this.sourceContext = fileInfo?.let {
            SourceContext {
                this.fileName = it.name
            }
        }
        this.syntax = when (fileInfo?.syntax) {
            "proto3" -> Syntax.PROTO3
            else -> Syntax.PROTO2
        }
    }
}

fun FieldDescriptorProto.toField(): Field {
    return Field {
        this.kind = Field.Kind(this@toField.type.number)
        this.cardinality = Field.Cardinality(this@toField.label.number)
        this.number = this@toField.number
        this.name = this@toField.name
        when (this.kind) {
            Field.Kind.TYPE_MESSAGE,
            Field.Kind.TYPE_ENUM -> {
                this.typeUrl = "types.bybutter.com/${this@toField.typeName.substring(1)}"
            }
            else -> {
            }
        }
        if (this@toField.hasOneofIndex()) {
            this.oneofIndex = this@toField.oneofIndex
        }
        this@toField.options?.packed?.let {
            this.packed = it
        }
        this@toField.options?.let {
            this.options += it.toOptions()
        }
        this.jsonName = this@toField.jsonName
        if (this@toField.hasDefaultValue()) {
            this.defaultValue = this@toField.defaultValue
        }
    }
}

fun EnumDescriptorProto.toEnum(typeName: String): Enum {
    return Enum {
        val fileInfo = ProtoTypes.getFileDescriptorByName(typeName)
        this.name = this@toEnum.name
        this.enumvalue += this@toEnum.value.map { it.toEnumValue() }
        this@toEnum.options?.let {
            this.options += it.toOptions()
        }
        this.sourceContext = fileInfo?.let {
            SourceContext {
                this.fileName = it.name
            }
        }
        this.syntax = when (fileInfo?.syntax) {
            "proto3" -> Syntax.PROTO3
            else -> Syntax.PROTO2
        }
    }
}

fun EnumValueDescriptorProto.toEnumValue(): EnumValue {
    return EnumValue {
        this.number = this@toEnumValue.number
        this.name = this@toEnumValue.name
        this@toEnumValue.options?.let {
            this.options += it.toOptions()
        }
    }
}

private fun Message<*, *>.toOptions(): List<Option> {
    val result = mutableListOf<Option>()

    loop@for ((field, value) in this) {
        when (value) {
            is List<*> -> {
                for (v in value) {
                    val protoValue = v.wrapToProtoValue() ?: continue
                    result += Option {
                        this.name = field.name
                        this.value = protoValue
                    }
                }
            }
            is Map<*, *> -> continue@loop
            else -> {
                val protoValue = value.wrapToProtoValue() ?: continue@loop
                result += Option {
                    this.name = field.name
                    this.value = protoValue
                }
            }
        }
    }

    return result
}

private fun kotlin.Any?.wrapToProtoValue(): Message<*, *>? {
    return when (this) {
        is Message<*, *> -> this
        is String -> this.wrapper()
        is Int -> this.wrapper()
        is UInt -> this.wrapper()
        is Long -> this.wrapper()
        is ULong -> this.wrapper()
        is Boolean -> this.wrapper()
        is Float -> this.wrapper()
        is Double -> this.wrapper()
        is ByteArray -> this.wrapper()
        else -> null
    }
}
