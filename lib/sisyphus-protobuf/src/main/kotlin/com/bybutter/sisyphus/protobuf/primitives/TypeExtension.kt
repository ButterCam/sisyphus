package com.bybutter.sisyphus.protobuf.primitives

import com.bybutter.sisyphus.protobuf.EnumSupport
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoReflection
import com.bybutter.sisyphus.protobuf.findMessageSupport

fun DescriptorProto.toType(typeName: String, reflection: ProtoReflection): Type {
    return Type {
        val support = reflection.findMessageSupport(typeName)
        this.name = this@toType.name
        for (fieldDescriptor in support.fieldDescriptors) {
            this.fields += fieldDescriptor.toField()
        }
        this.oneofs += this@toType.oneofDecl.map { it.name }
        this@toType.options?.let {
            this.options += it.toOptions()
        }
        this.sourceContext = SourceContext {
            this.fileName = support.file().name
        }
        this.syntax = when (support.file().descriptor.syntax) {
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

fun EnumDescriptorProto.toEnum(typeName: String, reflection: ProtoReflection): Enum {
    return Enum {
        val support = reflection.findSupport(typeName) as EnumSupport<*>
        this.name = this@toEnum.name
        this.enumvalue += this@toEnum.value.map { it.toEnumValue() }
        this@toEnum.options?.let {
            this.options += it.toOptions()
        }
        this.sourceContext = SourceContext {
            this.fileName = support.file().name
        }
        this.syntax = when (support.file().descriptor.syntax) {
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

    loop@ for ((field, value) in this) {
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
