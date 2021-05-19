package com.bybutter.sisyphus.protobuf.dynamic

import com.bybutter.sisyphus.protobuf.EnumSupport
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.MessageSupport
import com.bybutter.sisyphus.protobuf.ProtoEnum
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.coded.Reader
import com.bybutter.sisyphus.protobuf.coded.WireType
import com.bybutter.sisyphus.protobuf.coded.Writer
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto

interface DynamicField<T> {
    fun descriptor(): FieldDescriptorProto

    fun writeTo(writer: Writer)

    fun get(): T

    fun set(value: T)

    fun has(): Boolean

    fun clear(): T?

    fun read(reader: Reader, field: Int, wire: Int)

    override fun hashCode(): Int

    override fun equals(other: Any?): Boolean

    companion object {
        operator fun invoke(descriptor: FieldDescriptorProto): DynamicField<*> {
            when (descriptor.type) {
                FieldDescriptorProto.Type.DOUBLE -> TODO()
                FieldDescriptorProto.Type.FLOAT -> TODO()
                FieldDescriptorProto.Type.INT64 -> TODO()
                FieldDescriptorProto.Type.UINT64 -> TODO()
                FieldDescriptorProto.Type.INT32 -> TODO()
                FieldDescriptorProto.Type.FIXED64 -> TODO()
                FieldDescriptorProto.Type.FIXED32 -> TODO()
                FieldDescriptorProto.Type.BOOL -> TODO()
                FieldDescriptorProto.Type.STRING -> TODO()
                FieldDescriptorProto.Type.GROUP -> TODO()
                FieldDescriptorProto.Type.MESSAGE -> TODO()
                FieldDescriptorProto.Type.BYTES -> TODO()
                FieldDescriptorProto.Type.UINT32 -> TODO()
                FieldDescriptorProto.Type.ENUM -> TODO()
                FieldDescriptorProto.Type.SFIXED32 -> TODO()
                FieldDescriptorProto.Type.SFIXED64 -> TODO()
                FieldDescriptorProto.Type.SINT32 -> TODO()
                FieldDescriptorProto.Type.SINT64 -> TODO()
            }
        }
    }
}

class DoubleDynamicField(descriptor: FieldDescriptorProto) : AbstractDynamicField<Double>(descriptor) {
    override var value: Double = defaultValue()

    override fun defaultValue(): Double {
        return 0.0
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.FIXED64)
                .double(get())
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.FIXED64.ordinal) {
            set(reader.double())
        }
    }
}

class RepeatedDoubleDynamicField(descriptor: FieldDescriptorProto) : AbstractRepeatedDynamicField<Double>(descriptor) {
    override fun writeTo(writer: Writer) {
        for (value in get()) {
            writer.tag(descriptor().number, WireType.FIXED64)
                .double(value)
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.FIXED64.ordinal) {
            get() += reader.double()
        }
    }
}

class FloatDynamicField(descriptor: FieldDescriptorProto) : AbstractDynamicField<Float>(descriptor) {
    override var value: Float = defaultValue()

    override fun defaultValue(): Float {
        return 0.0f
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.FIXED32)
                .float(get())
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.FIXED32.ordinal) {
            set(reader.float())
        }
    }
}

class RepeatedFloatDynamicField(descriptor: FieldDescriptorProto) : AbstractRepeatedDynamicField<Float>(descriptor) {
    override fun writeTo(writer: Writer) {
        for (value in get()) {
            writer.tag(descriptor().number, WireType.FIXED32)
                .float(value)
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.FIXED32.ordinal) {
            get() += reader.float()
        }
    }
}

class Int64DynamicField(descriptor: FieldDescriptorProto) : AbstractDynamicField<Long>(descriptor) {
    override var value: Long = defaultValue()

    override fun defaultValue(): Long {
        return 0
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.VARINT)
                .int64(get())
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.VARINT.ordinal) {
            set(reader.int64())
        }
    }
}

class RepeatedInt64DynamicField(descriptor: FieldDescriptorProto) : AbstractRepeatedDynamicField<Long>(descriptor) {
    override fun writeTo(writer: Writer) {
        for (value in get()) {
            writer.tag(descriptor().number, WireType.VARINT)
                .int64(value)
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        reader.packed(wire) {
            get() += reader.int64()
        }
    }
}

class UInt64DynamicField(descriptor: FieldDescriptorProto) : AbstractDynamicField<ULong>(descriptor) {
    override var value: ULong = defaultValue()

    override fun defaultValue(): ULong {
        return 0UL
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.VARINT)
                .uint64(get())
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.VARINT.ordinal) {
            set(reader.uint64())
        }
    }
}

class RepeatedUInt64DynamicField(descriptor: FieldDescriptorProto) : AbstractRepeatedDynamicField<ULong>(descriptor) {
    override fun writeTo(writer: Writer) {
        for (value in get()) {
            writer.tag(descriptor().number, WireType.VARINT)
                .uint64(value)
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        reader.packed(wire) {
            get() += reader.uint64()
        }
    }
}

class Int32DynamicField(descriptor: FieldDescriptorProto) : AbstractDynamicField<Int>(descriptor) {
    override var value: Int = defaultValue()

    override fun defaultValue(): Int {
        return 0
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.VARINT)
                .int32(get())
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.VARINT.ordinal) {
            set(reader.int32())
        }
    }
}

class RepeatedInt32DynamicField(descriptor: FieldDescriptorProto) : AbstractRepeatedDynamicField<Int>(descriptor) {
    override fun writeTo(writer: Writer) {
        for (value in get()) {
            writer.tag(descriptor().number, WireType.VARINT)
                .int32(value)
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        reader.packed(wire) {
            get() += reader.int32()
        }
    }
}

class Fixed64DynamicField(descriptor: FieldDescriptorProto) : AbstractDynamicField<ULong>(descriptor) {
    override var value: ULong = defaultValue()

    override fun defaultValue(): ULong {
        return 0UL
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.FIXED64)
                .fixed64(get())
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.FIXED64.ordinal) {
            set(reader.fixed64())
        }
    }
}

class RepeatedFixed64DynamicField(descriptor: FieldDescriptorProto) : AbstractRepeatedDynamicField<ULong>(descriptor) {
    override fun writeTo(writer: Writer) {
        for (value in get()) {
            writer.tag(descriptor().number, WireType.FIXED64)
                .fixed64(value)
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.FIXED64.ordinal) {
            get() += reader.fixed64()
        }
    }
}

class Fixed32DynamicField(descriptor: FieldDescriptorProto) : AbstractDynamicField<UInt>(descriptor) {
    override var value: UInt = defaultValue()

    override fun defaultValue(): UInt {
        return 0U
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.FIXED32)
                .fixed32(get())
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.FIXED32.ordinal) {
            set(reader.fixed32())
        }
    }
}

class RepeatedFixed32DynamicField(descriptor: FieldDescriptorProto) : AbstractRepeatedDynamicField<UInt>(descriptor) {
    override fun writeTo(writer: Writer) {
        for (value in get()) {
            writer.tag(descriptor().number, WireType.FIXED32)
                .fixed32(value)
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.FIXED32.ordinal) {
            get() += reader.fixed32()
        }
    }
}

class BooleanDynamicField(descriptor: FieldDescriptorProto) : AbstractDynamicField<Boolean>(descriptor) {
    override var value: Boolean = defaultValue()

    override fun defaultValue(): Boolean {
        return false
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.VARINT)
                .bool(get())
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.VARINT.ordinal) {
            set(reader.bool())
        }
    }
}

class RepeatedBooleanDynamicField(descriptor: FieldDescriptorProto) :
    AbstractRepeatedDynamicField<Boolean>(descriptor) {
    override fun writeTo(writer: Writer) {
        for (value in get()) {
            writer.tag(descriptor().number, WireType.VARINT)
                .bool(value)
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.VARINT.ordinal) {
            get() += reader.bool()
        }
    }
}

class StringDynamicField(descriptor: FieldDescriptorProto) : AbstractDynamicField<String>(descriptor) {
    override var value: String = defaultValue()

    override fun defaultValue(): String {
        return ""
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.LENGTH_DELIMITED)
                .string(get())
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.VARINT.ordinal) {
            set(reader.string())
        }
    }
}

class RepeatedStringDynamicField(descriptor: FieldDescriptorProto) : AbstractRepeatedDynamicField<String>(descriptor) {
    override fun writeTo(writer: Writer) {
        for (value in get()) {
            writer.tag(descriptor().number, WireType.LENGTH_DELIMITED)
                .string(value)
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.LENGTH_DELIMITED.ordinal) {
            get() += reader.string()
        }
    }
}

class BytesDynamicField(descriptor: FieldDescriptorProto) : AbstractDynamicField<ByteArray>(descriptor) {
    override var value: ByteArray = defaultValue()

    override fun defaultValue(): ByteArray {
        return byteArrayOf()
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.LENGTH_DELIMITED)
                .bytes(get())
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.VARINT.ordinal) {
            set(reader.bytes())
        }
    }
}

class RepeatedBytesDynamicField(descriptor: FieldDescriptorProto) :
    AbstractRepeatedDynamicField<ByteArray>(descriptor) {
    override fun writeTo(writer: Writer) {
        for (value in get()) {
            writer.tag(descriptor().number, WireType.LENGTH_DELIMITED)
                .bytes(value)
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.LENGTH_DELIMITED.ordinal) {
            get() += reader.bytes()
        }
    }
}

class UInt32DynamicField(descriptor: FieldDescriptorProto) : AbstractDynamicField<UInt>(descriptor) {
    override var value: UInt = defaultValue()

    override fun defaultValue(): UInt {
        return 0U
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.VARINT)
                .uint32(get())
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.VARINT.ordinal) {
            set(reader.uint32())
        }
    }
}

class RepeatedUInt32DynamicField(descriptor: FieldDescriptorProto) : AbstractRepeatedDynamicField<UInt>(descriptor) {
    override fun writeTo(writer: Writer) {
        for (value in get()) {
            writer.tag(descriptor().number, WireType.VARINT)
                .uint32(value)
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        reader.packed(wire) {
            get() += reader.uint32()
        }
    }
}

class SFixed64DynamicField(descriptor: FieldDescriptorProto) : AbstractDynamicField<Long>(descriptor) {
    override var value: Long = defaultValue()

    override fun defaultValue(): Long {
        return 0
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.FIXED64)
                .sfixed64(get())
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.FIXED64.ordinal) {
            set(reader.sfixed64())
        }
    }
}

class RepeatedSFixed64DynamicField(descriptor: FieldDescriptorProto) : AbstractRepeatedDynamicField<Long>(descriptor) {
    override fun writeTo(writer: Writer) {
        for (value in get()) {
            writer.tag(descriptor().number, WireType.FIXED64)
                .sfixed64(value)
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.FIXED64.ordinal) {
            get() += reader.sfixed64()
        }
    }
}

class SFixed32DynamicField(descriptor: FieldDescriptorProto) : AbstractDynamicField<Int>(descriptor) {
    override var value: Int = defaultValue()

    override fun defaultValue(): Int {
        return 0
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.FIXED32)
                .sfixed32(get())
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.FIXED32.ordinal) {
            set(reader.sfixed32())
        }
    }
}

class RepeatedSFixed32DynamicField(descriptor: FieldDescriptorProto) : AbstractRepeatedDynamicField<Int>(descriptor) {
    override fun writeTo(writer: Writer) {
        for (value in get()) {
            writer.tag(descriptor().number, WireType.FIXED32)
                .sfixed32(value)
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.FIXED32.ordinal) {
            get() += reader.sfixed32()
        }
    }
}

class SInt64DynamicField(descriptor: FieldDescriptorProto) : AbstractDynamicField<Long>(descriptor) {
    override var value: Long = defaultValue()

    override fun defaultValue(): Long {
        return 0
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.VARINT)
                .sint64(get())
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.VARINT.ordinal) {
            set(reader.sint64())
        }
    }
}

class RepeatedSInt64DynamicField(descriptor: FieldDescriptorProto) : AbstractRepeatedDynamicField<Long>(descriptor) {
    override fun writeTo(writer: Writer) {
        for (value in get()) {
            writer.tag(descriptor().number, WireType.VARINT)
                .sint64(value)
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        reader.packed(wire) {
            get() += reader.sint64()
        }
    }
}

class SInt32DynamicField(descriptor: FieldDescriptorProto) : AbstractDynamicField<Int>(descriptor) {
    override var value: Int = defaultValue()

    override fun defaultValue(): Int {
        return 0
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.VARINT)
                .sint32(get())
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.VARINT.ordinal) {
            set(reader.sint32())
        }
    }
}

class RepeatedSInt32DynamicField(descriptor: FieldDescriptorProto) : AbstractRepeatedDynamicField<Int>(descriptor) {
    override fun writeTo(writer: Writer) {
        for (value in get()) {
            writer.tag(descriptor().number, WireType.VARINT)
                .sint32(value)
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        reader.packed(wire) {
            get() += reader.sint32()
        }
    }
}

class EnumDynamicField<T : ProtoEnum<T>>(descriptor: FieldDescriptorProto) : AbstractDynamicField<T>(descriptor) {
    override var value: T = defaultValue()

    private val support = ProtoTypes.findEnumSupport(descriptor().typeName) as EnumSupport<T>

    override fun defaultValue(): T {
        return support()
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.VARINT)
                .enum(get())
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.VARINT.ordinal) {
            set(support(reader.int32()))
        }
    }
}

class RepeatedEnumDynamicField<T : ProtoEnum<T>>(descriptor: FieldDescriptorProto) :
    AbstractRepeatedDynamicField<T>(descriptor) {
    private val support = ProtoTypes.findEnumSupport(descriptor().typeName) as EnumSupport<T>

    override fun writeTo(writer: Writer) {
        for (value in get()) {
            writer.tag(descriptor().number, WireType.VARINT)
                .enum(value)
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        reader.packed(wire) {
            get() += support(reader.int32())
        }
    }
}

class MessageDynamicField<T : Message<T, *>>(descriptor: FieldDescriptorProto) : AbstractDynamicField<T?>(descriptor) {
    override var value: T? = defaultValue()

    private val support = ProtoTypes.findMessageSupport(descriptor().typeName) as MessageSupport<T, *>

    override fun defaultValue(): T? {
        return null
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.VARINT)
                .message(value)
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.VARINT.ordinal) {
            set(support.parse(reader, reader.int32()))
        } else {
            reader.skip(WireType.valueOf(wire))
        }
    }
}

class RepeatedMessageDynamicField<T : Message<T, *>>(descriptor: FieldDescriptorProto) :
    AbstractRepeatedDynamicField<T>(descriptor) {
    private val support = ProtoTypes.findMessageSupport(descriptor().typeName) as MessageSupport<T, *>

    override fun writeTo(writer: Writer) {
        for (value in get()) {
            writer.tag(descriptor().number, WireType.VARINT)
                .message(value)
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.VARINT.ordinal) {
            get() += support.parse(reader, reader.int32())
        } else {
            reader.skip(WireType.valueOf(wire))
        }
    }
}
