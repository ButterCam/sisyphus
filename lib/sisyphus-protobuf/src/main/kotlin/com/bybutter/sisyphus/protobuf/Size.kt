package com.bybutter.sisyphus.protobuf

import com.google.protobuf.CodedOutputStream

object Size {
    fun ofEnum(field: Int, value: ProtoEnum?): Int {
        value ?: return 0
        return CodedOutputStream.computeEnumSize(field, value.number)
    }

    fun ofEnum(field: Int, value: List<ProtoEnum>): Int {
        if (value.isEmpty()) return 0
        val size = value.sumBy { CodedOutputStream.computeEnumSizeNoTag(it.number) }
        return CodedOutputStream.computeTagSize(field) + CodedOutputStream.computeInt32SizeNoTag(size) + size
    }

    fun ofInt32(field: Int, value: Int?): Int {
        value ?: return 0
        return CodedOutputStream.computeInt32Size(field, value)
    }

    fun ofInt32(field: Int, value: List<Int>): Int {
        if (value.isEmpty()) return 0
        val size = value.sumBy { CodedOutputStream.computeInt32SizeNoTag(it) }
        return CodedOutputStream.computeTagSize(field) + CodedOutputStream.computeInt32SizeNoTag(size) + size
    }

    fun ofSInt32(field: Int, value: Int?): Int {
        value ?: return 0
        return CodedOutputStream.computeSInt32Size(field, value)
    }

    fun ofSInt32(field: Int, value: List<Int>): Int {
        if (value.isEmpty()) return 0
        val size = value.sumBy { CodedOutputStream.computeSInt32SizeNoTag(it) }
        return CodedOutputStream.computeTagSize(field) + CodedOutputStream.computeInt32SizeNoTag(size) + size
    }

    fun ofUInt32(field: Int, value: UInt?): Int {
        value ?: return 0
        return CodedOutputStream.computeUInt32Size(field, value.toInt())
    }

    fun ofUInt32(field: Int, value: List<UInt>): Int {
        if (value.isEmpty()) return 0
        val size = value.sumBy { CodedOutputStream.computeUInt32SizeNoTag(it.toInt()) }
        return CodedOutputStream.computeTagSize(field) + CodedOutputStream.computeInt32SizeNoTag(size) + size
    }

    fun ofInt64(field: Int, value: Long?): Int {
        value ?: return 0
        return CodedOutputStream.computeInt64Size(field, value)
    }

    fun ofInt64(field: Int, value: List<Long>): Int {
        if (value.isEmpty()) return 0
        val size = value.sumBy { CodedOutputStream.computeInt64SizeNoTag(it) }
        return CodedOutputStream.computeTagSize(field) + CodedOutputStream.computeInt32SizeNoTag(size) + size
    }

    fun ofSInt64(field: Int, value: Long?): Int {
        value ?: return 0
        return CodedOutputStream.computeSInt64Size(field, value)
    }

    fun ofSInt64(field: Int, value: List<Long>): Int {
        if (value.isEmpty()) return 0
        val size = value.sumBy { CodedOutputStream.computeSInt64SizeNoTag(it) }
        return CodedOutputStream.computeTagSize(field) + CodedOutputStream.computeInt32SizeNoTag(size) + size
    }

    fun ofUInt64(field: Int, value: ULong?): Int {
        value ?: return 0
        return CodedOutputStream.computeUInt64Size(field, value.toLong())
    }

    fun ofUInt64(field: Int, value: List<ULong>): Int {
        if (value.isEmpty()) return 0
        val size = value.sumBy { CodedOutputStream.computeUInt64SizeNoTag(it.toLong()) }
        return CodedOutputStream.computeTagSize(field) + CodedOutputStream.computeInt32SizeNoTag(size) + size
    }

    fun ofBool(field: Int, value: Boolean?): Int {
        value ?: return 0
        return CodedOutputStream.computeBoolSize(field, value)
    }

    fun ofBool(field: Int, value: List<Boolean>): Int {
        if (value.isEmpty()) return 0
        val size = value.sumBy { CodedOutputStream.computeBoolSizeNoTag(it) }
        return CodedOutputStream.computeTagSize(field) + CodedOutputStream.computeInt32SizeNoTag(size) + size
    }

    fun ofFloat(field: Int, value: Float?): Int {
        value ?: return 0
        return CodedOutputStream.computeFloatSize(field, value)
    }

    fun ofFloat(field: Int, value: List<Float>): Int {
        if (value.isEmpty()) return 0
        val size = value.sumBy { CodedOutputStream.computeFloatSizeNoTag(it) }
        return CodedOutputStream.computeTagSize(field) + CodedOutputStream.computeInt32SizeNoTag(size) + size
    }

    fun ofSFixed32(field: Int, value: Int?): Int {
        value ?: return 0
        return CodedOutputStream.computeSFixed32Size(field, value)
    }

    fun ofSFixed32(field: Int, value: List<Int>): Int {
        if (value.isEmpty()) return 0
        val size = value.sumBy { CodedOutputStream.computeSFixed32SizeNoTag(it) }
        return CodedOutputStream.computeTagSize(field) + CodedOutputStream.computeInt32SizeNoTag(size) + size
    }

    fun ofFixed32(field: Int, value: UInt?): Int {
        value ?: return 0
        return CodedOutputStream.computeFixed32Size(field, value.toInt())
    }

    fun ofFixed32(field: Int, value: List<UInt>): Int {
        if (value.isEmpty()) return 0
        val size = value.sumBy { CodedOutputStream.computeFixed32SizeNoTag(it.toInt()) }
        return CodedOutputStream.computeTagSize(field) + CodedOutputStream.computeInt32SizeNoTag(size) + size
    }

    fun ofDouble(field: Int, value: Double?): Int {
        value ?: return 0
        return CodedOutputStream.computeDoubleSize(field, value)
    }

    fun ofDouble(field: Int, value: List<Double>): Int {
        if (value.isEmpty()) return 0
        val size = value.sumBy { CodedOutputStream.computeDoubleSizeNoTag(it) }
        return CodedOutputStream.computeTagSize(field) + CodedOutputStream.computeInt32SizeNoTag(size) + size
    }

    fun ofSFixed64(field: Int, value: Long?): Int {
        value ?: return 0
        return CodedOutputStream.computeSFixed64Size(field, value)
    }

    fun ofSFixed64(field: Int, value: List<Long>): Int {
        if (value.isEmpty()) return 0
        val size = value.sumBy { CodedOutputStream.computeSFixed64SizeNoTag(it) }
        return CodedOutputStream.computeTagSize(field) + CodedOutputStream.computeInt32SizeNoTag(size) + size
    }

    fun ofFixed64(field: Int, value: ULong?): Int {
        value ?: return 0
        return CodedOutputStream.computeFixed64Size(field, value.toLong())
    }

    fun ofFixed64(field: Int, value: List<ULong>): Int {
        if (value.isEmpty()) return 0
        val size = value.sumBy { CodedOutputStream.computeFixed64SizeNoTag(it.toLong()) }
        return CodedOutputStream.computeTagSize(field) + CodedOutputStream.computeInt32SizeNoTag(size) + size
    }

    fun ofString(field: Int, value: String?): Int {
        value ?: return 0
        return CodedOutputStream.computeStringSize(field, value)
    }

    fun ofBytes(field: Int, value: ByteArray?): Int {
        value ?: return 0
        return CodedOutputStream.computeByteArraySize(field, value)
    }

    fun ofMessage(field: Int, value: Message<*, *>?): Int {
        value ?: return 0
        val size = value.size()
        return CodedOutputStream.computeTagSize(field) + CodedOutputStream.computeInt32SizeNoTag(size) + size
    }

    fun ofMessage(field: Int, size: Int?): Int {
        size ?: return 0
        return CodedOutputStream.computeTagSize(field) + CodedOutputStream.computeInt32SizeNoTag(size) + size
    }

    fun ofAny(field: Int, value: Message<*, *>?): Int {
        value ?: return 0
        var size = value.size()
        size += CodedOutputStream.computeInt32SizeNoTag(size)
        size += CodedOutputStream.computeTagSize(com.google.protobuf.Any.VALUE_FIELD_NUMBER)
        size += CodedOutputStream.computeStringSize(com.google.protobuf.Any.TYPE_URL_FIELD_NUMBER, value.typeUrl())
        size += CodedOutputStream.computeInt32SizeNoTag(size)
        size += CodedOutputStream.computeTagSize(field)
        return size
    }
}
