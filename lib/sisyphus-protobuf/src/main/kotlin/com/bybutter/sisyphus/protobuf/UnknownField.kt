package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.coded.Reader
import com.bybutter.sisyphus.protobuf.coded.WireType
import com.bybutter.sisyphus.protobuf.coded.Writer

abstract class UnknownField<T> {
    abstract val tag: Int
    abstract val data: T

    abstract fun writeTo(writer: Writer)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnknownField<*>

        if (tag != other.tag) return false
        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tag
        result = 31 * result + data.hashCode()
        return result
    }
}

class Varint(number: Int, override val data: Long) : UnknownField<Long>() {
    override val tag: Int = WireType.tagOf(number, WireType.VARINT)

    override fun writeTo(writer: Writer) {
        writer.tag(tag).int64(data)
    }
}

class Fixed32(number: Int, override val data: Int) : UnknownField<Int>() {
    override val tag: Int = WireType.tagOf(number, WireType.FIXED32)

    override fun writeTo(writer: Writer) {
        writer.tag(tag).sfixed32(data)
    }
}

class Fixed64(number: Int, override val data: Long) : UnknownField<Long>() {
    override val tag: Int = WireType.tagOf(number, WireType.FIXED64)

    override fun writeTo(writer: Writer) {
        writer.tag(tag).sfixed64(data)
    }
}

class Bytes(number: Int, override val data: ByteArray) : UnknownField<ByteArray>() {
    override val tag: Int = WireType.tagOf(number, WireType.LENGTH_DELIMITED)

    override fun writeTo(writer: Writer) {
        writer.tag(tag).bytes(data)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Bytes

        if (tag != other.tag) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tag
        result = 31 * result + data.contentHashCode()
        return result
    }
}

class UnknownFields {
    val fields: List<UnknownField<*>> get() = _fields
    private val _fields: MutableList<UnknownField<*>> = mutableListOf()

    fun writeTo(writer: Writer) {
        for (field in fields) {
            field.writeTo(writer)
        }
    }

    fun readFrom(reader: Reader, number: Int, wireType: Int) {
        when (wireType) {
            WireType.VARINT.ordinal -> {
                _fields.add(Varint(number, reader.int64()))
            }
            WireType.FIXED32.ordinal -> {
                _fields.add(Fixed32(number, reader.sfixed32()))
            }
            WireType.FIXED64.ordinal -> {
                _fields.add(Fixed64(number, reader.sfixed64()))
            }
            WireType.LENGTH_DELIMITED.ordinal -> {
                _fields.add(Bytes(number, reader.bytes()))
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

    fun clear(): List<UnknownField<*>> {
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

    companion object {
        val empty = UnknownFields()
    }
}
