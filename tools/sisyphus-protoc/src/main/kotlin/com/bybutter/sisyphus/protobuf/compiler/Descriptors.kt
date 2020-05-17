package com.bybutter.sisyphus.protobuf.compiler

import com.google.protobuf.DescriptorProtos
import com.google.protobuf.WireFormat
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaMethod

val DescriptorProtos.FieldDescriptorProto.isRequired: Boolean get() = this.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED

val DescriptorProtos.FieldDescriptorProto.isOptional: Boolean get() = this.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL

val DescriptorProtos.FieldDescriptorProto.isRepeated: Boolean get() = this.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED

val DescriptorProtos.FieldDescriptorProto.isScalar: Boolean get() = this.typeName.isEmpty()

val DescriptorProtos.FieldDescriptorProto.canPack: Boolean
    get() {
        return when (this.type) {
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_GROUP,
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING,
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE,
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES -> false
            else -> true
        }
    }

val DescriptorProtos.FieldDescriptorProto.wireType: Int
    get() {
        return WireFormat.FieldType.values()[this.type.ordinal].wireType
    }

val DescriptorProtos.DescriptorProto.isMapType: Boolean get() = this.options?.mapEntry == true

fun KFunction<*>.asMemberName(): MemberName {
    return if (this.extensionReceiverParameter != null) {
        MemberName(this.javaMethod!!.declaringClass.`package`.name, this.name)
    } else {
        MemberName(this.javaMethod!!.declaringClass.asClassName(), this.name)
    }
}

fun KProperty<*>.asMemberName(): MemberName {
    return if (this.extensionReceiverParameter != null) {
        MemberName(this.javaGetter!!.declaringClass.`package`.name, this.name)
    } else {
        MemberName(this.javaGetter!!.declaringClass.asClassName(), this.name)
    }
}
