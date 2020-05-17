package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.protobuf.primitives.Any
import com.bybutter.sisyphus.protobuf.primitives.toMessage
import com.google.protobuf.CodedInputStream
import com.google.protobuf.WireFormat
import kotlin.reflect.full.companionObjectInstance

@OptIn(ExperimentalUnsignedTypes::class)
object ProtoReader {
    fun <T : ProtoEnum> readEnum(input: CodedInputStream, clazz: Class<T>, field: Int, wire: Int): T? {
        return when (wire) {
            WireFormat.WIRETYPE_VARINT -> {
                ProtoEnum(input.readEnum(), clazz)
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val length = input.readInt32()
                if (length == 0) return null
                var result = 0
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result = input.readEnum()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                ProtoEnum(result, clazz)
            }
            else -> throw IllegalStateException("Enum field only accept 'varint' or 'lengthDelimited' data.")
        }
    }

    fun <T : ProtoEnum> readEnumList(input: CodedInputStream, clazz: Class<T>, field: Int, wire: Int): List<T> {
        return when (wire) {
            WireFormat.WIRETYPE_VARINT -> {
                listOf(ProtoEnum(input.readEnum(), clazz))
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val result = mutableListOf<T>()
                val length = input.readInt32()
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result += ProtoEnum(input.readEnum(), clazz)
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("Enum field only accept 'varint' or 'lengthDelimited' data.")
        }
    }

    fun readInt32(input: CodedInputStream, field: Int, wire: Int): Int? {
        return when (wire) {
            WireFormat.WIRETYPE_VARINT -> {
                input.readInt32()
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val length = input.readInt32()
                if (length == 0) return null
                var result = 0
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result = input.readInt32()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("Int32 field only accept 'varint' or 'lengthDelimited' data.")
        }
    }

    fun readInt32List(input: CodedInputStream, field: Int, wire: Int): List<Int> {
        return when (wire) {
            WireFormat.WIRETYPE_VARINT -> {
                listOf(input.readInt32())
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val result = mutableListOf<Int>()
                val length = input.readInt32()
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result += input.readInt32()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("Int32 field only accept 'varint' or 'lengthDelimited' data.")
        }
    }

    fun readSInt32(input: CodedInputStream, field: Int, wire: Int): Int? {
        return when (wire) {
            WireFormat.WIRETYPE_VARINT -> {
                input.readSInt32()
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val length = input.readInt32()
                if (length == 0) return null
                var result = 0
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result = input.readSInt32()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("Int32 field only accept 'varint' or 'lengthDelimited' data.")
        }
    }

    fun readSInt32List(input: CodedInputStream, field: Int, wire: Int): List<Int> {
        return when (wire) {
            WireFormat.WIRETYPE_VARINT -> {
                listOf(input.readSInt32())
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val result = mutableListOf<Int>()
                val length = input.readInt32()
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result += input.readSInt32()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("SInt32 field only accept 'varint' or 'lengthDelimited' data.")
        }
    }

    fun readUInt32(input: CodedInputStream, field: Int, wire: Int): UInt? {
        return when (wire) {
            WireFormat.WIRETYPE_VARINT -> {
                input.readUInt32().toUInt()
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val length = input.readInt32()
                if (length == 0) return null
                var result = 0
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result = input.readUInt32()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result.toUInt()
            }
            else -> throw IllegalStateException("UInt32 field only accept 'varint' or 'lengthDelimited' data.")
        }
    }

    fun readUInt32List(input: CodedInputStream, field: Int, wire: Int): List<UInt> {
        return when (wire) {
            WireFormat.WIRETYPE_VARINT -> {
                listOf(input.readUInt32().toUInt())
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val result = mutableListOf<UInt>()
                val length = input.readInt32()
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result += input.readUInt32().toUInt()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("UInt32 field only accept 'varint' or 'lengthDelimited' data.")
        }
    }

    fun readInt64(input: CodedInputStream, field: Int, wire: Int): Long? {
        return when (wire) {
            WireFormat.WIRETYPE_VARINT -> {
                input.readInt64()
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val length = input.readInt32()
                if (length == 0) return null
                var result = 0L
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result = input.readInt64()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("Int64 field only accept 'varint' or 'lengthDelimited' data.")
        }
    }

    fun readInt64List(input: CodedInputStream, field: Int, wire: Int): List<Long> {
        return when (wire) {
            WireFormat.WIRETYPE_VARINT -> {
                listOf(input.readInt64())
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val result = mutableListOf<Long>()
                val length = input.readInt32()
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result += input.readInt64()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("Int64 field only accept 'varint' or 'lengthDelimited' data.")
        }
    }

    fun readSInt64(input: CodedInputStream, field: Int, wire: Int): Long? {
        return when (wire) {
            WireFormat.WIRETYPE_VARINT -> {
                input.readSInt64()
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val length = input.readInt32()
                if (length == 0) return null
                var result = 0L
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result = input.readSInt64()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("Int64 field only accept 'varint' or 'lengthDelimited' data.")
        }
    }

    fun readSInt64List(input: CodedInputStream, field: Int, wire: Int): List<Long> {
        return when (wire) {
            WireFormat.WIRETYPE_VARINT -> {
                listOf(input.readSInt64())
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val result = mutableListOf<Long>()
                val length = input.readInt32()
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result += input.readSInt64()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("SInt64 field only accept 'varint' or 'lengthDelimited' data.")
        }
    }

    fun readUInt64(input: CodedInputStream, field: Int, wire: Int): ULong? {
        return when (wire) {
            WireFormat.WIRETYPE_VARINT -> {
                input.readUInt64().toULong()
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val length = input.readInt32()
                if (length == 0) return null
                var result = 0L
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result = input.readUInt64()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result.toULong()
            }
            else -> throw IllegalStateException("UInt64 field only accept 'varint' or 'lengthDelimited' data.")
        }
    }

    fun readUInt64List(input: CodedInputStream, field: Int, wire: Int): List<ULong> {
        return when (wire) {
            WireFormat.WIRETYPE_VARINT -> {
                listOf(input.readUInt64().toULong())
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val result = mutableListOf<ULong>()
                val length = input.readInt32()
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result += input.readUInt64().toULong()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("UInt64 field only accept 'varint' or 'lengthDelimited' data.")
        }
    }

    fun readBool(input: CodedInputStream, field: Int, wire: Int): Boolean? {
        return when (wire) {
            WireFormat.WIRETYPE_VARINT -> {
                input.readBool()
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val length = input.readInt32()
                if (length == 0) return null
                var result = false
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result = input.readBool()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("Bool field only accept 'varint' or 'lengthDelimited' data.")
        }
    }

    fun readBoolList(input: CodedInputStream, field: Int, wire: Int): List<Boolean> {
        return when (wire) {
            WireFormat.WIRETYPE_VARINT -> {
                listOf(input.readBool())
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val result = mutableListOf<Boolean>()
                val length = input.readInt32()
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result += input.readBool()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("Bool field only accept 'varint' or 'lengthDelimited' data.")
        }
    }

    fun readFloat(input: CodedInputStream, field: Int, wire: Int): Float? {
        return when (wire) {
            WireFormat.WIRETYPE_FIXED32 -> {
                input.readFloat()
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val length = input.readInt32()
                if (length == 0) return null
                var result = 0.0f
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result = input.readFloat()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("Float field only accept 'fixed32' or 'lengthDelimited' data.")
        }
    }

    fun readFloatList(input: CodedInputStream, field: Int, wire: Int): List<Float> {
        return when (wire) {
            WireFormat.WIRETYPE_FIXED32 -> {
                listOf(input.readFloat())
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val result = mutableListOf<Float>()
                val length = input.readInt32()
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result += input.readFloat()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("Float field only accept 'fixed32' or 'lengthDelimited' data.")
        }
    }

    fun readSFixed32(input: CodedInputStream, field: Int, wire: Int): Int? {
        return when (wire) {
            WireFormat.WIRETYPE_FIXED32 -> {
                input.readSFixed32()
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val length = input.readInt32()
                if (length == 0) return null
                var result = 0
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result = input.readSFixed32()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("SFixed32 field only accept 'fixed32' or 'lengthDelimited' data.")
        }
    }

    fun readSFixed32List(input: CodedInputStream, field: Int, wire: Int): List<Int> {
        return when (wire) {
            WireFormat.WIRETYPE_FIXED32 -> {
                listOf(input.readSFixed32())
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val result = mutableListOf<Int>()
                val length = input.readInt32()
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result += input.readSFixed32()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("SFixed32 field only accept 'fixed32' or 'lengthDelimited' data.")
        }
    }

    fun readFixed32(input: CodedInputStream, field: Int, wire: Int): UInt? {
        return when (wire) {
            WireFormat.WIRETYPE_FIXED32 -> {
                input.readFixed32().toUInt()
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val length = input.readInt32()
                if (length == 0) return null
                var result = 0
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result = input.readFixed32()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result.toUInt()
            }
            else -> throw IllegalStateException("Fixed32 field only accept 'fixed32' or 'lengthDelimited' data.")
        }
    }

    fun readFixed32List(input: CodedInputStream, field: Int, wire: Int): List<UInt> {
        return when (wire) {
            WireFormat.WIRETYPE_FIXED32 -> {
                listOf(input.readFixed32().toUInt())
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val result = mutableListOf<UInt>()
                val length = input.readInt32()
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result += input.readFixed32().toUInt()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("Fixed32 field only accept 'fixed64' or 'lengthDelimited' data.")
        }
    }

    fun readDouble(input: CodedInputStream, field: Int, wire: Int): Double? {
        return when (wire) {
            WireFormat.WIRETYPE_FIXED64 -> {
                input.readDouble()
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val length = input.readInt32()
                if (length == 0) return null
                var result = 0.0
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result = input.readDouble()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("Double field only accept 'fixed64' or 'lengthDelimited' data.")
        }
    }

    fun readDoubleList(input: CodedInputStream, field: Int, wire: Int): List<Double> {
        return when (wire) {
            WireFormat.WIRETYPE_FIXED64 -> {
                listOf(input.readDouble())
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val result = mutableListOf<Double>()
                val length = input.readInt32()
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result += input.readDouble()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("Double field only accept 'fixed64' or 'lengthDelimited' data.")
        }
    }

    fun readSFixed64(input: CodedInputStream, field: Int, wire: Int): Long? {
        return when (wire) {
            WireFormat.WIRETYPE_FIXED64 -> {
                input.readSFixed64()
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val length = input.readInt32()
                if (length == 0) return null
                var result = 0L
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result = input.readSFixed64()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("SFixed64 field only accept 'fixed64' or 'lengthDelimited' data.")
        }
    }

    fun readSFixed64List(input: CodedInputStream, field: Int, wire: Int): List<Long> {
        return when (wire) {
            WireFormat.WIRETYPE_FIXED64 -> {
                listOf(input.readSFixed64())
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val result = mutableListOf<Long>()
                val length = input.readInt32()
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result += input.readSFixed64()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("SFixed64 field only accept 'fixed64' or 'lengthDelimited' data.")
        }
    }

    fun readFixed64(input: CodedInputStream, field: Int, wire: Int): ULong? {
        return when (wire) {
            WireFormat.WIRETYPE_FIXED64 -> {
                input.readFixed64().toULong()
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val length = input.readInt32()
                if (length == 0) return null
                var result = 0L
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result = input.readFixed64()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result.toULong()
            }
            else -> throw IllegalStateException("Fixed64 field only accept 'fixed64' or 'lengthDelimited' data.")
        }
    }

    fun readFixed64List(input: CodedInputStream, field: Int, wire: Int): List<ULong> {
        return when (wire) {
            WireFormat.WIRETYPE_FIXED64 -> {
                listOf(input.readFixed64().toULong())
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                val result = mutableListOf<ULong>()
                val length = input.readInt32()
                val current = input.totalBytesRead
                while (input.totalBytesRead - current < length) {
                    result += input.readFixed64().toULong()
                }
                if (input.totalBytesRead - current != length) {
                    throw IllegalStateException("Wrong packed data at position $current with length $length.")
                }
                result
            }
            else -> throw IllegalStateException("Fixed64 field only accept 'fixed64' or 'lengthDelimited' data.")
        }
    }

    fun readString(input: CodedInputStream, field: Int, wire: Int): String {
        return when (wire) {
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                input.readString()
            }
            else -> throw IllegalStateException("String field only accept 'lengthDelimited' data.")
        }
    }

    fun readBytes(input: CodedInputStream, field: Int, wire: Int): ByteArray {
        return when (wire) {
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                input.readByteArray()
            }
            else -> throw IllegalStateException("Bytes field only accept 'lengthDelimited' data.")
        }
    }

    fun <T : Message<*, *>> readMessage(input: CodedInputStream, clazz: Class<T>, field: Int, wire: Int, size: Int): T {
        return when (wire) {
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                try {
                    val support = clazz.kotlin.companionObjectInstance as? ProtoSupport<*, *>
                            ?: throw UnsupportedOperationException("Message must be generated by proto compiler.")
                    support.parse(input, size) as T
                } catch (e: Exception) {
                    clazz.kotlin.companionObjectInstance
                    throw e
                }
            }
            else -> throw IllegalStateException("Message field only accept 'lengthDelimited' data.")
        }
    }

    fun readAny(input: CodedInputStream, field: Int, wire: Int, size: Int): Message<*, *> {
        return when (wire) {
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                Any {
                    readFrom(input, size)
                }.toMessage()
            }
            else -> throw IllegalStateException("Message field only accept 'lengthDelimited' data.")
        }
    }
}
