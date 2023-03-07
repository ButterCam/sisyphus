package com.bybutter.sisyphus.protobuf.dynamic

import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoEnum
import com.bybutter.sisyphus.protobuf.ProtoReflection
import com.bybutter.sisyphus.protobuf.coded.Reader
import com.bybutter.sisyphus.protobuf.coded.WireType
import com.bybutter.sisyphus.protobuf.coded.Writer
import com.bybutter.sisyphus.protobuf.findEnumSupport
import com.bybutter.sisyphus.protobuf.findMapEntryDescriptor
import com.bybutter.sisyphus.protobuf.findMessageSupport
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.unwrapAny
import com.bybutter.sisyphus.protobuf.primitives.wrapAny

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
        operator fun invoke(message: DynamicMessageSupport, descriptor: FieldDescriptorProto): DynamicField<*> {
            return if (descriptor.label == FieldDescriptorProto.Label.REPEATED) {
                when (descriptor.type) {
                    FieldDescriptorProto.Type.DOUBLE -> RepeatedDoubleDynamicField(descriptor)
                    FieldDescriptorProto.Type.FLOAT -> RepeatedFloatDynamicField(descriptor)
                    FieldDescriptorProto.Type.INT64 -> RepeatedInt64DynamicField(descriptor)
                    FieldDescriptorProto.Type.UINT64 -> RepeatedUInt64DynamicField(descriptor)
                    FieldDescriptorProto.Type.INT32 -> RepeatedInt32DynamicField(descriptor)
                    FieldDescriptorProto.Type.FIXED64 -> RepeatedFixed64DynamicField(descriptor)
                    FieldDescriptorProto.Type.FIXED32 -> RepeatedFixed32DynamicField(descriptor)
                    FieldDescriptorProto.Type.BOOL -> RepeatedBooleanDynamicField(descriptor)
                    FieldDescriptorProto.Type.STRING -> RepeatedStringDynamicField(descriptor)
                    FieldDescriptorProto.Type.GROUP -> TODO()
                    FieldDescriptorProto.Type.MESSAGE -> {
                        val support = ProtoReflection.findMapEntryDescriptor(descriptor.typeName)
                        if (support?.options?.mapEntry == true) {
                            MapDynamicField<Any, Any>(descriptor)
                        } else if (descriptor.typeName == com.bybutter.sisyphus.protobuf.primitives.Any.name) {
                            RepeatedAnyDynamicField(descriptor)
                        } else {
                            RepeatedMessageDynamicField(descriptor)
                        }
                    }

                    FieldDescriptorProto.Type.BYTES -> RepeatedBytesDynamicField(descriptor)
                    FieldDescriptorProto.Type.UINT32 -> RepeatedUInt32DynamicField(descriptor)
                    FieldDescriptorProto.Type.ENUM -> RepeatedEnumDynamicField(descriptor)
                    FieldDescriptorProto.Type.SFIXED32 -> RepeatedSFixed32DynamicField(descriptor)
                    FieldDescriptorProto.Type.SFIXED64 -> RepeatedSFixed64DynamicField(descriptor)
                    FieldDescriptorProto.Type.SINT32 -> RepeatedSInt32DynamicField(descriptor)
                    FieldDescriptorProto.Type.SINT64 -> RepeatedSInt64DynamicField(descriptor)
                }
            } else {
                when (descriptor.type) {
                    FieldDescriptorProto.Type.DOUBLE -> DoubleDynamicField(descriptor)
                    FieldDescriptorProto.Type.FLOAT -> FloatDynamicField(descriptor)
                    FieldDescriptorProto.Type.INT64 -> Int64DynamicField(descriptor)
                    FieldDescriptorProto.Type.UINT64 -> UInt64DynamicField(descriptor)
                    FieldDescriptorProto.Type.INT32 -> Int32DynamicField(descriptor)
                    FieldDescriptorProto.Type.FIXED64 -> Fixed64DynamicField(descriptor)
                    FieldDescriptorProto.Type.FIXED32 -> Fixed32DynamicField(descriptor)
                    FieldDescriptorProto.Type.BOOL -> BooleanDynamicField(descriptor)
                    FieldDescriptorProto.Type.STRING -> StringDynamicField(descriptor)
                    FieldDescriptorProto.Type.GROUP -> TODO()
                    FieldDescriptorProto.Type.MESSAGE -> {
                        if (descriptor.typeName == com.bybutter.sisyphus.protobuf.primitives.Any.name) {
                            AnyDynamicField(descriptor)
                        } else {
                            MessageDynamicField(descriptor)
                        }
                    }

                    FieldDescriptorProto.Type.BYTES -> BytesDynamicField(descriptor)
                    FieldDescriptorProto.Type.UINT32 -> UInt32DynamicField(descriptor)
                    FieldDescriptorProto.Type.ENUM -> EnumDynamicField(descriptor)
                    FieldDescriptorProto.Type.SFIXED32 -> SFixed32DynamicField(descriptor)
                    FieldDescriptorProto.Type.SFIXED64 -> SFixed64DynamicField(descriptor)
                    FieldDescriptorProto.Type.SINT32 -> SInt32DynamicField(descriptor)
                    FieldDescriptorProto.Type.SINT64 -> SInt64DynamicField(descriptor)
                }
            }
        }
    }
}

class DoubleDynamicField(descriptor: FieldDescriptorProto) : AbstractPackableDynamicField<Double>(descriptor) {
    override var value: Double = defaultValue()

    override fun defaultValue(): Double {
        return 0.0
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.FIXED64).double(get())
        }
    }

    override fun read0(reader: Reader, field: Int, wire: Int) {
        set(reader.double())
    }
}

class RepeatedDoubleDynamicField(descriptor: FieldDescriptorProto) :
    AbstractRepeatedPackableDynamicField<Double>(descriptor) {
    override fun read0(reader: Reader, field: Int, wire: Int) {
        get() += reader.double()
    }

    override fun write0(writer: Writer, value: Double) {
        writer.double(value)
    }
}

class FloatDynamicField(descriptor: FieldDescriptorProto) : AbstractPackableDynamicField<Float>(descriptor) {
    override var value: Float = defaultValue()

    override fun defaultValue(): Float {
        return 0.0f
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.FIXED32).float(get())
        }
    }

    override fun read0(reader: Reader, field: Int, wire: Int) {
        set(reader.float())
    }
}

class RepeatedFloatDynamicField(descriptor: FieldDescriptorProto) :
    AbstractRepeatedPackableDynamicField<Float>(descriptor) {
    override fun read0(reader: Reader, field: Int, wire: Int) {
        get() += reader.float()
    }

    override fun write0(writer: Writer, value: Float) {
        writer.float(value)
    }
}

class Int64DynamicField(descriptor: FieldDescriptorProto) : AbstractPackableDynamicField<Long>(descriptor) {
    override var value: Long = defaultValue()

    override fun defaultValue(): Long {
        return 0
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.VARINT).int64(get())
        }
    }

    override fun read0(reader: Reader, field: Int, wire: Int) {
        set(reader.int64())
    }
}

class RepeatedInt64DynamicField(descriptor: FieldDescriptorProto) :
    AbstractRepeatedPackableDynamicField<Long>(descriptor) {
    override fun read0(reader: Reader, field: Int, wire: Int) {
        get() += reader.int64()
    }

    override fun write0(writer: Writer, value: Long) {
        writer.int64(value)
    }
}

class UInt64DynamicField(descriptor: FieldDescriptorProto) : AbstractPackableDynamicField<ULong>(descriptor) {
    override var value: ULong = defaultValue()

    override fun defaultValue(): ULong {
        return 0UL
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.VARINT).uint64(get())
        }
    }

    override fun read0(reader: Reader, field: Int, wire: Int) {
        set(reader.uint64())
    }
}

class RepeatedUInt64DynamicField(descriptor: FieldDescriptorProto) :
    AbstractRepeatedPackableDynamicField<ULong>(descriptor) {

    override fun read0(reader: Reader, field: Int, wire: Int) {
        get() += reader.uint64()
    }

    override fun write0(writer: Writer, value: ULong) {
        writer.uint64(value)
    }
}

class Int32DynamicField(descriptor: FieldDescriptorProto) : AbstractPackableDynamicField<Int>(descriptor) {
    override var value: Int = defaultValue()

    override fun defaultValue(): Int {
        return 0
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.VARINT).int32(get())
        }
    }

    override fun read0(reader: Reader, field: Int, wire: Int) {
        set(reader.int32())
    }
}

class RepeatedInt32DynamicField(descriptor: FieldDescriptorProto) :
    AbstractRepeatedPackableDynamicField<Int>(descriptor) {

    override fun read0(reader: Reader, field: Int, wire: Int) {
        get() += reader.int32()
    }

    override fun write0(writer: Writer, value: Int) {
        writer.int32(value)
    }
}

class Fixed64DynamicField(descriptor: FieldDescriptorProto) : AbstractPackableDynamicField<ULong>(descriptor) {
    override var value: ULong = defaultValue()

    override fun defaultValue(): ULong {
        return 0UL
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.FIXED64).fixed64(get())
        }
    }

    override fun read0(reader: Reader, field: Int, wire: Int) {
        set(reader.fixed64())
    }
}

class RepeatedFixed64DynamicField(descriptor: FieldDescriptorProto) :
    AbstractRepeatedPackableDynamicField<ULong>(descriptor) {
    override fun read0(reader: Reader, field: Int, wire: Int) {
        get() += reader.fixed64()
    }

    override fun write0(writer: Writer, value: ULong) {
        writer.fixed64(value)
    }
}

class Fixed32DynamicField(descriptor: FieldDescriptorProto) : AbstractPackableDynamicField<UInt>(descriptor) {
    override var value: UInt = defaultValue()

    override fun defaultValue(): UInt {
        return 0U
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.FIXED32).fixed32(get())
        }
    }

    override fun read0(reader: Reader, field: Int, wire: Int) {
        set(reader.fixed32())
    }
}

class RepeatedFixed32DynamicField(descriptor: FieldDescriptorProto) :
    AbstractRepeatedPackableDynamicField<UInt>(descriptor) {
    override fun read0(reader: Reader, field: Int, wire: Int) {
        get() += reader.fixed32()
    }

    override fun write0(writer: Writer, value: UInt) {
        writer.fixed32(value)
    }
}

class BooleanDynamicField(descriptor: FieldDescriptorProto) : AbstractPackableDynamicField<Boolean>(descriptor) {
    override var value: Boolean = defaultValue()

    override fun defaultValue(): Boolean {
        return false
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.VARINT).bool(get())
        }
    }

    override fun read0(reader: Reader, field: Int, wire: Int) {
        set(reader.bool())
    }
}

class RepeatedBooleanDynamicField(descriptor: FieldDescriptorProto) :
    AbstractRepeatedPackableDynamicField<Boolean>(descriptor) {

    override fun read0(reader: Reader, field: Int, wire: Int) {
        get() += reader.bool()
    }

    override fun write0(writer: Writer, value: Boolean) {
        writer.bool(value)
    }
}

class StringDynamicField(descriptor: FieldDescriptorProto) : AbstractDynamicField<String>(descriptor) {
    override var value: String = defaultValue()

    override fun defaultValue(): String {
        return ""
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.LENGTH_DELIMITED).string(get())
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.LENGTH_DELIMITED.ordinal) {
            set(reader.string())
        }
    }
}

class RepeatedStringDynamicField(descriptor: FieldDescriptorProto) : AbstractRepeatedDynamicField<String>(descriptor) {
    override fun writeTo(writer: Writer) {
        for (value in get()) {
            writer.tag(descriptor().number, WireType.LENGTH_DELIMITED).string(value)
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
            writer.tag(descriptor().number, WireType.LENGTH_DELIMITED).bytes(get())
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.LENGTH_DELIMITED.ordinal) {
            set(reader.bytes())
        }
    }
}

class RepeatedBytesDynamicField(descriptor: FieldDescriptorProto) :
    AbstractRepeatedDynamicField<ByteArray>(descriptor) {
    override fun writeTo(writer: Writer) {
        for (value in get()) {
            writer.tag(descriptor().number, WireType.LENGTH_DELIMITED).bytes(value)
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.LENGTH_DELIMITED.ordinal) {
            get() += reader.bytes()
        }
    }
}

class UInt32DynamicField(descriptor: FieldDescriptorProto) : AbstractPackableDynamicField<UInt>(descriptor) {
    override var value: UInt = defaultValue()

    override fun defaultValue(): UInt {
        return 0U
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.VARINT).uint32(get())
        }
    }

    override fun read0(reader: Reader, field: Int, wire: Int) {
        set(reader.uint32())
    }
}

class RepeatedUInt32DynamicField(descriptor: FieldDescriptorProto) :
    AbstractRepeatedPackableDynamicField<UInt>(descriptor) {
    override fun read0(reader: Reader, field: Int, wire: Int) {
        get() += reader.uint32()
    }

    override fun write0(writer: Writer, value: UInt) {
        writer.uint32(value)
    }
}

class SFixed64DynamicField(descriptor: FieldDescriptorProto) : AbstractPackableDynamicField<Long>(descriptor) {
    override var value: Long = defaultValue()

    override fun defaultValue(): Long {
        return 0
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.FIXED64).sfixed64(get())
        }
    }

    override fun read0(reader: Reader, field: Int, wire: Int) {
        set(reader.sfixed64())
    }
}

class RepeatedSFixed64DynamicField(descriptor: FieldDescriptorProto) :
    AbstractRepeatedPackableDynamicField<Long>(descriptor) {
    override fun read0(reader: Reader, field: Int, wire: Int) {
        get() += reader.sfixed64()
    }

    override fun write0(writer: Writer, value: Long) {
        writer.sfixed64(value)
    }
}

class SFixed32DynamicField(descriptor: FieldDescriptorProto) : AbstractPackableDynamicField<Int>(descriptor) {
    override var value: Int = defaultValue()

    override fun defaultValue(): Int {
        return 0
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.FIXED32).sfixed32(get())
        }
    }

    override fun read0(reader: Reader, field: Int, wire: Int) {
        set(reader.sfixed32())
    }
}

class RepeatedSFixed32DynamicField(descriptor: FieldDescriptorProto) :
    AbstractRepeatedPackableDynamicField<Int>(descriptor) {
    override fun read0(reader: Reader, field: Int, wire: Int) {
        get() += reader.sfixed32()
    }

    override fun write0(writer: Writer, value: Int) {
        writer.sfixed32(value)
    }
}

class SInt64DynamicField(descriptor: FieldDescriptorProto) : AbstractPackableDynamicField<Long>(descriptor) {
    override var value: Long = defaultValue()

    override fun defaultValue(): Long {
        return 0
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.VARINT).sint64(get())
        }
    }

    override fun read0(reader: Reader, field: Int, wire: Int) {
        set(reader.sint64())
    }
}

class RepeatedSInt64DynamicField(descriptor: FieldDescriptorProto) :
    AbstractRepeatedPackableDynamicField<Long>(descriptor) {
    override fun read0(reader: Reader, field: Int, wire: Int) {
        get() += reader.sint64()
    }

    override fun write0(writer: Writer, value: Long) {
        writer.sint64(value)
    }
}

class SInt32DynamicField(descriptor: FieldDescriptorProto) : AbstractPackableDynamicField<Int>(descriptor) {
    override var value: Int = defaultValue()

    override fun defaultValue(): Int {
        return 0
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.VARINT).sint32(get())
        }
    }

    override fun read0(reader: Reader, field: Int, wire: Int) {
        set(reader.sint32())
    }
}

class RepeatedSInt32DynamicField(descriptor: FieldDescriptorProto) :
    AbstractRepeatedPackableDynamicField<Int>(descriptor) {
    override fun read0(reader: Reader, field: Int, wire: Int) {
        get() += reader.sint32()
    }

    override fun write0(writer: Writer, value: Int) {
        writer.sint32(value)
    }
}

class EnumDynamicField(
    descriptor: FieldDescriptorProto
) : AbstractPackableDynamicField<ProtoEnum<*>>(descriptor) {
    private val support = ProtoReflection.findEnumSupport(descriptor().typeName)

    override var value: ProtoEnum<*> = defaultValue()

    override fun defaultValue(): ProtoEnum<*> {
        return support()
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.VARINT).enum(get())
        }
    }

    override fun read0(reader: Reader, field: Int, wire: Int) {
        set(support(reader.int32()))
    }
}

class RepeatedEnumDynamicField(
    descriptor: FieldDescriptorProto
) : AbstractRepeatedPackableDynamicField<ProtoEnum<*>>(descriptor) {
    private val support = ProtoReflection.findEnumSupport(descriptor().typeName)

    override fun read0(reader: Reader, field: Int, wire: Int) {
        get() += support(reader.int32())
    }

    override fun write0(writer: Writer, value: ProtoEnum<*>) {
        writer.enum(value)
    }
}

class MessageDynamicField(
    descriptor: FieldDescriptorProto
) : AbstractDynamicField<Message<*, *>?>(descriptor) {
    private val support = ProtoReflection.findMessageSupport(descriptor().typeName)

    override var value: Message<*, *>? = defaultValue()

    override fun defaultValue(): Message<*, *>? {
        return null
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            writer.tag(descriptor().number, WireType.LENGTH_DELIMITED).message(value)
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.LENGTH_DELIMITED.ordinal) {
            set(support.parse(reader, reader.int32()))
        } else {
            reader.skip(WireType.valueOf(wire))
        }
    }
}

class RepeatedMessageDynamicField(
    descriptor: FieldDescriptorProto
) : AbstractRepeatedDynamicField<Message<*, *>>(descriptor) {
    private val support = ProtoReflection.findMessageSupport(descriptor().typeName)

    override fun writeTo(writer: Writer) {
        for (value in get()) {
            writer.tag(descriptor().number, WireType.LENGTH_DELIMITED).message(value)
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.LENGTH_DELIMITED.ordinal) {
            get() += support.parse(reader, reader.int32())
        } else {
            reader.skip(WireType.valueOf(wire))
        }
    }
}

class AnyDynamicField(
    descriptor: FieldDescriptorProto
) : AbstractDynamicField<Message<*, *>?>(descriptor) {
    override var value: Message<*, *>? = defaultValue()

    override fun defaultValue(): Message<*, *>? {
        return null
    }

    override fun writeTo(writer: Writer) {
        if (has()) {
            val value = value ?: return
            writer.tag(descriptor().number, WireType.LENGTH_DELIMITED).message(value.wrapAny())
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.LENGTH_DELIMITED.ordinal) {
            val support = ProtoReflection.findMessageSupport(com.bybutter.sisyphus.protobuf.primitives.Any.name)
            val any = support.parse(reader, reader.int32())
            set(any.unwrapAny() ?: any)
        } else {
            reader.skip(WireType.valueOf(wire))
        }
    }
}

class RepeatedAnyDynamicField(
    descriptor: FieldDescriptorProto
) : AbstractRepeatedDynamicField<Message<*, *>>(descriptor) {

    override fun writeTo(writer: Writer) {
        for (value in get()) {
            writer.tag(descriptor().number, WireType.LENGTH_DELIMITED).message(value.wrapAny())
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        if (wire == WireType.LENGTH_DELIMITED.ordinal) {
            val support = ProtoReflection.findMessageSupport(com.bybutter.sisyphus.protobuf.primitives.Any.name)
            val any = support.parse(reader, reader.int32())
            get() += any.unwrapAny() ?: any
        } else {
            reader.skip(WireType.valueOf(wire))
        }
    }
}
