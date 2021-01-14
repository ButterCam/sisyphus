package com.bybutter.sisyphus.protobuf.compiler.generating

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.util.escapeDoc
import com.bybutter.sisyphus.string.toPascalCase
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
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock

fun Generating<*, DescriptorProtos.FieldDescriptorProto, *>.defaultValue(): CodeBlock = buildCodeBlock {
    if (descriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED) {
        if (mapEntry() != null) {
            add("mutableMapOf()")
            return@buildCodeBlock
        }
        add("mutableListOf()")
        return@buildCodeBlock
    }

    when (descriptor.type) {
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE -> add("0.0")
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT -> add("0.0f")
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64 -> add("0L")
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64 -> add("0UL")
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32 -> add("0")
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64 -> add("0UL")
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32 -> add("0U")
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL -> add("false")
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING -> add("%S", "")
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE -> add("null")
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES -> add("byteArrayOf()")
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32 -> add("0U")
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM -> add("%T()", elementType())
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED32 -> add("0")
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED64 -> add("0L")
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT32 -> add("0")
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT64 -> add("0L")
        else -> TODO()
    }
}

fun Generating<*, DescriptorProtos.FieldDescriptorProto, *>.name(): String {
    return descriptor.jsonName
}

fun Generating<*, DescriptorProtos.FieldDescriptorProto, *>.hasFunction(): String {
    return "has${name().toPascalCase()}"
}

fun Generating<*, DescriptorProtos.FieldDescriptorProto, *>.clearFunction(): String {
    return "clear${name().toPascalCase()}"
}

fun Generating<out Generating<*, DescriptorProtos.DescriptorProto, *>, DescriptorProtos.FieldDescriptorProto, *>.path(): List<Int> {
    return parent.path() + DescriptorProtos.DescriptorProto.FIELD_FIELD_NUMBER + parent.descriptor.fieldList.indexOf(
        descriptor
    )
}

fun Generating<out Generating<*, DescriptorProtos.DescriptorProto, *>, DescriptorProtos.FieldDescriptorProto, *>.document(): String {
    return escapeDoc(file().descriptor.sourceCodeInfo?.locationList?.firstOrNull {
        it.pathList.contentEquals(path())
    }?.leadingComments ?: "")
}

fun Generating<*, *, *>.type(descriptor: DescriptorProtos.FieldDescriptorProto): TypeName {
    return when (descriptor.type) {
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL -> Boolean::class.asClassName()
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES -> ByteArray::class.asClassName()
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE -> Double::class.asClassName()
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT -> Float::class.asClassName()
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32 -> UInt::class.asClassName()
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64 -> ULong::class.asClassName()
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32 -> Int::class.asClassName()
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64 -> Long::class.asClassName()
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED32 -> Int::class.asClassName()
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED64 -> Long::class.asClassName()
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT32 -> Int::class.asClassName()
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT64 -> Long::class.asClassName()
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING -> String::class.asClassName()
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32 -> UInt::class.asClassName()
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64 -> ULong::class.asClassName()
        DescriptorProtos.FieldDescriptorProto.Type.TYPE_GROUP -> throw UnsupportedOperationException("Group is not supported by butter proto.")
        else -> {
            if (descriptor.typeName == ".google.protobuf.Any") {
                RuntimeTypes.MESSAGE.parameterizedBy(TypeVariableName("*"), TypeVariableName("*"))
            } else {
                compiler().protoClassName(descriptor.typeName)
            }
        }
    }
}

fun Generating<*, DescriptorProtos.FieldDescriptorProto, *>.elementType(): TypeName {
    return type(descriptor)
}

fun Generating<*, DescriptorProtos.FieldDescriptorProto, *>.fieldType(): TypeName {
    when (descriptor.label) {
        DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED -> {
            mapEntry()?.let {
                val descriptor = compiler().protoDescriptor(descriptor.typeName)
                val keyField = descriptor.fieldList.first { it.number == 1 }
                val valueField = descriptor.fieldList.first { it.number == 2 }
                return MAP.parameterizedBy(type(keyField), type(valueField))
            }
            return LIST.parameterizedBy(elementType())
        }
        DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL -> {
            if (descriptor.type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE) {
                return elementType().copy(true)
            }
            return elementType()
        }
        DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED -> {
            return elementType()
        }
    }
}

fun Generating<*, DescriptorProtos.FieldDescriptorProto, *>.mutableFieldType(): TypeName {
    if (descriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED) {
        val name = fieldType() as ParameterizedTypeName
        return when (name.rawType) {
            LIST -> MUTABLE_LIST.parameterizedBy(name.typeArguments)
            MAP -> MUTABLE_MAP.parameterizedBy(name.typeArguments)
            else -> TODO()
        }
    } else return fieldType()
}

fun Generating<*, DescriptorProtos.FieldDescriptorProto, *>.mapEntry(): DescriptorProtos.DescriptorProto? {
    if (descriptor.type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE) {
        val descriptor = compiler().protoDescriptor(descriptor.typeName)
        if (descriptor.options.mapEntry) {
            return descriptor
        }
    }
    return null
}

fun Generating<out Generating<*, DescriptorProtos.DescriptorProto, *>, DescriptorProtos.OneofDescriptorProto, *>.oneOfClassName(): ClassName {
    return parent.className().nestedClass(descriptor.name.toPascalCase())
}

fun Generating<out Generating<*, DescriptorProtos.DescriptorProto, *>, DescriptorProtos.FieldDescriptorProto, *>.oneOfClassName(oneOf: DescriptorProtos.OneofDescriptorProto): ClassName {
    return parent.className().nestedClass(oneOf.name.toPascalCase()).nestedClass(descriptor.name.toPascalCase())
}
