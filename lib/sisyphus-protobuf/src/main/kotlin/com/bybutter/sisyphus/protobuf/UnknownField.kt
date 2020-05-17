package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.collection.takeWhen
import com.google.protobuf.CodedInputStream
import com.google.protobuf.CodedOutputStream
import com.google.protobuf.WireFormat
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

data class UnknownField(
    var number: Int,
    var wireType: Int,
    var data: ByteArray
) {
    val size: Int
        get() {
            return CodedOutputStream.computeTagSize(this.number) + when (this.wireType) {
                WireFormat.WIRETYPE_VARINT -> this.data.size
                WireFormat.WIRETYPE_FIXED32 -> this.data.size
                WireFormat.WIRETYPE_FIXED64 -> this.data.size
                WireFormat.WIRETYPE_LENGTH_DELIMITED -> CodedOutputStream.computeInt32SizeNoTag(this.data.size) + this.data.size
                else -> throw UnsupportedOperationException("Unsupported wire type '${this.wireType}'.")
            }
        }

    fun writeTo(output: CodedOutputStream) {
        output.writeTag(this.number, this.wireType)
        when (this.wireType) {
            WireFormat.WIRETYPE_VARINT,
            WireFormat.WIRETYPE_FIXED32,
            WireFormat.WIRETYPE_FIXED64 -> {
                output.writeRawBytes(this.data)
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                output.writeByteArrayNoTag(this.data)
            }
            else -> throw UnsupportedOperationException("Unsupported wire type '${this.wireType}'.")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnknownField

        if (number != other.number) return false
        if (wireType != other.wireType) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = number
        result = 31 * result + wireType
        result = 31 * result + data.contentHashCode()
        return result
    }
}

class UnknownFields {
    val fields: List<UnknownField> get() = _fields
    private val _fields: MutableList<UnknownField> = mutableListOf()

    val size: Int
        get() {
            return fields.sumBy { it.size }
        }

    fun writeTo(output: CodedOutputStream) {
        for (field in fields) {
            field.writeTo(output)
        }
    }

    fun readFrom(input: CodedInputStream, number: Int, wireType: Int) {
        when (wireType) {
            WireFormat.WIRETYPE_VARINT -> {
                ByteArrayOutputStream().use {
                    do {
                        val byte = input.readRawByte().toInt()
                        it.write(byte)
                    } while (byte and 0x80 > 0)
                    _fields.add(UnknownField(number, wireType, it.toByteArray()))
                }
            }
            WireFormat.WIRETYPE_FIXED32 -> {
                _fields.add(UnknownField(number, wireType, input.readRawBytes(4)))
            }
            WireFormat.WIRETYPE_FIXED64 -> {
                _fields.add(UnknownField(number, wireType, input.readRawBytes(8)))
            }
            WireFormat.WIRETYPE_LENGTH_DELIMITED -> {
                _fields.add(UnknownField(number, wireType, input.readByteArray()))
            }
            else -> throw UnsupportedOperationException("Unsupported wire type '$wireType'.")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnknownFields

        if (!fields.contentEquals(other.fields)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = this.javaClass.hashCode()
        for (field in fields) {
            result = result * 31 + field.hashCode()
        }
        return result
    }

    fun clear(): List<UnknownField> {
        return _fields.toList().apply {
            _fields.clear()
        }
    }

    operator fun plus(other: UnknownFields): UnknownFields {
        return UnknownFields().apply {
            this += this@UnknownFields
            this += other
        }
    }

    operator fun plusAssign(other: UnknownFields) {
        this._fields.addAll(other.fields)
    }

    @UseExperimental(InternalProtoApi::class)
    fun exportExtension(support: ExtensionSupport<*, *>): Message<*, *> {
        val extendedFields = support.extendedFields.map { it.number }.toSet()
        val extended = support.extendedFields

        val fieldsData = _fields.takeWhen {
            it.number in extendedFields
        }

        val buffer = ByteBuffer.allocate(fieldsData.sumBy { it.size })
        val output = CodedOutputStream.newInstance(buffer)

        for (field in fieldsData) {
            field.writeTo(output)
        }
        output.flush()

        buffer.rewind()
        val input = CodedInputStream.newInstance(buffer)

        return support.newMutable().apply {
            readFrom(input, Int.MAX_VALUE)
        }
    }
}
