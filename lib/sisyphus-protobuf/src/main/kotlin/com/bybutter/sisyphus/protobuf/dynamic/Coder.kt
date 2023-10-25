package com.bybutter.sisyphus.protobuf.dynamic

import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoEnum
import com.bybutter.sisyphus.protobuf.ProtoReflection
import com.bybutter.sisyphus.protobuf.coded.Reader
import com.bybutter.sisyphus.protobuf.coded.Writer
import com.bybutter.sisyphus.protobuf.findEnumSupport
import com.bybutter.sisyphus.protobuf.findMessageSupport
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto

fun <T> Reader.read(
    type: FieldDescriptorProto.Type,
    typename: String,
): T {
    return when (type) {
        FieldDescriptorProto.Type.DOUBLE -> double()
        FieldDescriptorProto.Type.FLOAT -> float()
        FieldDescriptorProto.Type.INT64 -> int64()
        FieldDescriptorProto.Type.UINT64 -> uint64()
        FieldDescriptorProto.Type.INT32 -> int32()
        FieldDescriptorProto.Type.FIXED64 -> fixed64()
        FieldDescriptorProto.Type.FIXED32 -> fixed32()
        FieldDescriptorProto.Type.BOOL -> bool()
        FieldDescriptorProto.Type.STRING -> string()
        FieldDescriptorProto.Type.GROUP -> TODO()
        FieldDescriptorProto.Type.BYTES -> bytes()
        FieldDescriptorProto.Type.UINT32 -> uint32()
        FieldDescriptorProto.Type.SFIXED32 -> sfixed32()
        FieldDescriptorProto.Type.SFIXED64 -> sfixed64()
        FieldDescriptorProto.Type.SINT32 -> sint32()
        FieldDescriptorProto.Type.SINT64 -> sint64()
        FieldDescriptorProto.Type.MESSAGE -> ProtoReflection.findMessageSupport(typename).parse(this, int32())
        FieldDescriptorProto.Type.ENUM -> ProtoReflection.findEnumSupport(typename).invoke(int32())
    } as T
}

fun Writer.write(
    type: FieldDescriptorProto.Type,
    value: Any?,
): Writer {
    value ?: return this
    return when (type) {
        FieldDescriptorProto.Type.DOUBLE -> double(value as Double)
        FieldDescriptorProto.Type.FLOAT -> float(value as Float)
        FieldDescriptorProto.Type.INT64 -> int64(value as Long)
        FieldDescriptorProto.Type.UINT64 -> uint64(value as ULong)
        FieldDescriptorProto.Type.INT32 -> int32(value as Int)
        FieldDescriptorProto.Type.FIXED64 -> fixed64(value as ULong)
        FieldDescriptorProto.Type.FIXED32 -> fixed32(value as UInt)
        FieldDescriptorProto.Type.BOOL -> bool(value as Boolean)
        FieldDescriptorProto.Type.STRING -> string(value as String)
        FieldDescriptorProto.Type.GROUP -> TODO()
        FieldDescriptorProto.Type.BYTES -> bytes(value as ByteArray)
        FieldDescriptorProto.Type.UINT32 -> uint32(value as UInt)
        FieldDescriptorProto.Type.SFIXED32 -> sfixed32(value as Int)
        FieldDescriptorProto.Type.SFIXED64 -> sfixed64(value as Long)
        FieldDescriptorProto.Type.SINT32 -> sint32(value as Int)
        FieldDescriptorProto.Type.SINT64 -> sint64(value as Long)
        FieldDescriptorProto.Type.MESSAGE -> message(value as Message<*, *>)
        FieldDescriptorProto.Type.ENUM -> enum(value as ProtoEnum<*>)
    }
}
