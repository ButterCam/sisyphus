package com.bybutter.sisyphus.protobuf

import com.google.protobuf.CodedOutputStream
import com.google.protobuf.WireFormat

object ProtoWriter {
    fun writeEnum(output: CodedOutputStream, field: Int, value: ProtoEnum?) {
        value ?: return
        output.writeEnum(field, value.number)
    }

    fun writeEnum(output: CodedOutputStream, field: Int, value: List<ProtoEnum>) {
        if (value.isEmpty()) return
        output.writeTag(field, WireFormat.WIRETYPE_LENGTH_DELIMITED)
        output.writeInt32NoTag(value.sumBy { CodedOutputStream.computeEnumSizeNoTag(it.number) })
        for (v in value) {
            output.writeEnumNoTag(v.number)
        }
    }

    fun writeInt32(output: CodedOutputStream, field: Int, value: Int?) {
        value ?: return
        output.writeInt32(field, value)
    }

    fun writeInt32(output: CodedOutputStream, field: Int, value: List<Int>) {
        if (value.isEmpty()) return
        output.writeTag(field, WireFormat.WIRETYPE_LENGTH_DELIMITED)
        output.writeInt32NoTag(value.sumBy { CodedOutputStream.computeInt32SizeNoTag(it) })
        for (v in value) {
            output.writeInt32NoTag(v)
        }
    }

    fun writeSInt32(output: CodedOutputStream, field: Int, value: Int?) {
        value ?: return
        output.writeSInt32(field, value)
    }

    fun writeSInt32(output: CodedOutputStream, field: Int, value: List<Int>) {
        if (value.isEmpty()) return
        output.writeTag(field, WireFormat.WIRETYPE_LENGTH_DELIMITED)
        output.writeSInt32NoTag(value.sumBy { CodedOutputStream.computeSInt32SizeNoTag(it) })
        for (v in value) {
            output.writeSInt32NoTag(v)
        }
    }

    fun writeUInt32(output: CodedOutputStream, field: Int, value: UInt?) {
        value ?: return
        output.writeUInt32(field, value.toInt())
    }

    fun writeUInt32(output: CodedOutputStream, field: Int, value: List<UInt>) {
        if (value.isEmpty()) return
        output.writeTag(field, WireFormat.WIRETYPE_LENGTH_DELIMITED)
        output.writeUInt32NoTag(value.sumBy { CodedOutputStream.computeUInt32SizeNoTag(it.toInt()) })
        for (v in value) {
            output.writeUInt32NoTag(v.toInt())
        }
    }

    fun writeInt64(output: CodedOutputStream, field: Int, value: Long?) {
        value ?: return
        output.writeInt64(field, value)
    }

    fun writeInt64(output: CodedOutputStream, field: Int, value: List<Long>) {
        if (value.isEmpty()) return
        output.writeTag(field, WireFormat.WIRETYPE_LENGTH_DELIMITED)
        output.writeInt32NoTag(value.sumBy { CodedOutputStream.computeInt64SizeNoTag(it) })
        for (v in value) {
            output.writeInt64NoTag(v)
        }
    }

    fun writeSInt64(output: CodedOutputStream, field: Int, value: Long?) {
        value ?: return
        output.writeSInt64(field, value)
    }

    fun writeSInt64(output: CodedOutputStream, field: Int, value: List<Long>) {
        if (value.isEmpty()) return
        output.writeTag(field, WireFormat.WIRETYPE_LENGTH_DELIMITED)
        output.writeInt32NoTag(value.sumBy { CodedOutputStream.computeSInt64SizeNoTag(it) })
        for (v in value) {
            output.writeSInt64NoTag(v)
        }
    }

    fun writeUInt64(output: CodedOutputStream, field: Int, value: ULong?) {
        value ?: return
        output.writeUInt64(field, value.toLong())
    }

    fun writeUInt64(output: CodedOutputStream, field: Int, value: List<ULong>) {
        if (value.isEmpty()) return
        output.writeTag(field, WireFormat.WIRETYPE_LENGTH_DELIMITED)
        output.writeInt32NoTag(value.sumBy { CodedOutputStream.computeUInt64SizeNoTag(it.toLong()) })
        for (v in value) {
            output.writeUInt64NoTag(v.toLong())
        }
    }

    fun writeBool(output: CodedOutputStream, field: Int, value: Boolean?) {
        value ?: return
        output.writeBool(field, value)
    }

    fun writeBool(output: CodedOutputStream, field: Int, value: List<Boolean>) {
        if (value.isEmpty()) return
        output.writeTag(field, WireFormat.WIRETYPE_LENGTH_DELIMITED)
        output.writeInt32NoTag(value.sumBy { CodedOutputStream.computeBoolSizeNoTag(it) })
        for (v in value) {
            output.writeBoolNoTag(v)
        }
    }

    fun writeFloat(output: CodedOutputStream, field: Int, value: Float?) {
        value ?: return
        output.writeFloat(field, value)
    }

    fun writeFloat(output: CodedOutputStream, field: Int, value: List<Float>) {
        if (value.isEmpty()) return
        output.writeTag(field, WireFormat.WIRETYPE_LENGTH_DELIMITED)
        output.writeInt32NoTag(value.sumBy { CodedOutputStream.computeFloatSizeNoTag(it) })
        for (v in value) {
            output.writeFloatNoTag(v)
        }
    }

    fun writeSFixed32(output: CodedOutputStream, field: Int, value: Int?) {
        value ?: return
        output.writeSFixed32(field, value)
    }

    fun writeSFixed32(output: CodedOutputStream, field: Int, value: List<Int>) {
        if (value.isEmpty()) return
        output.writeTag(field, WireFormat.WIRETYPE_LENGTH_DELIMITED)
        output.writeInt32NoTag(value.sumBy { CodedOutputStream.computeSFixed32SizeNoTag(it) })
        for (v in value) {
            output.writeSFixed32NoTag(v)
        }
    }

    fun writeFixed32(output: CodedOutputStream, field: Int, value: UInt?) {
        value ?: return
        output.writeFixed32(field, value.toInt())
    }

    fun writeFixed32(output: CodedOutputStream, field: Int, value: List<UInt>) {
        if (value.isEmpty()) return
        output.writeTag(field, WireFormat.WIRETYPE_LENGTH_DELIMITED)
        output.writeInt32NoTag(value.sumBy { CodedOutputStream.computeFixed32SizeNoTag(it.toInt()) })
        for (v in value) {
            output.writeFixed32NoTag(v.toInt())
        }
    }

    fun writeDouble(output: CodedOutputStream, field: Int, value: Double?) {
        value ?: return
        output.writeDouble(field, value)
    }

    fun writeDouble(output: CodedOutputStream, field: Int, value: List<Double>) {
        if (value.isEmpty()) return
        output.writeTag(field, WireFormat.WIRETYPE_LENGTH_DELIMITED)
        output.writeInt32NoTag(value.sumBy { CodedOutputStream.computeDoubleSizeNoTag(it) })
        for (v in value) {
            output.writeDoubleNoTag(v)
        }
    }

    fun writeSFixed64(output: CodedOutputStream, field: Int, value: Long?) {
        value ?: return
        output.writeSFixed64(field, value)
    }

    fun writeSFixed64(output: CodedOutputStream, field: Int, value: List<Long>) {
        if (value.isEmpty()) return
        output.writeTag(field, WireFormat.WIRETYPE_LENGTH_DELIMITED)
        output.writeInt32NoTag(value.sumBy { CodedOutputStream.computeSFixed64SizeNoTag(it) })
        for (v in value) {
            output.writeSFixed64NoTag(v)
        }
    }

    fun writeFixed64(output: CodedOutputStream, field: Int, value: ULong?) {
        value ?: return
        output.writeFixed64(field, value.toLong())
    }

    fun writeFixed64(output: CodedOutputStream, field: Int, value: List<ULong>) {
        if (value.isEmpty()) return
        output.writeTag(field, WireFormat.WIRETYPE_LENGTH_DELIMITED)
        output.writeInt32NoTag(value.sumBy { CodedOutputStream.computeFixed64SizeNoTag(it.toLong()) })
        for (v in value) {
            output.writeFixed64NoTag(v.toLong())
        }
    }

    fun writeString(output: CodedOutputStream, field: Int, value: String?) {
        value ?: return
        output.writeString(field, value)
    }

    fun writeBytes(output: CodedOutputStream, field: Int, value: ByteArray?) {
        value ?: return
        output.writeByteArray(field, value)
    }

    fun writeMessage(output: CodedOutputStream, field: Int, value: Message<*, *>?) {
        value ?: return
        output.writeTag(field, WireFormat.WIRETYPE_LENGTH_DELIMITED)
        output.writeInt32NoTag(value.size())
        value.writeTo(output)
    }

    fun writeAny(output: CodedOutputStream, field: Int, value: Message<*, *>?) {
        value ?: return
        output.writeTag(field, WireFormat.WIRETYPE_LENGTH_DELIMITED)

        val size = CodedOutputStream.computeStringSize(com.google.protobuf.Any.TYPE_URL_FIELD_NUMBER, value.typeUrl()) +
                CodedOutputStream.computeTagSize(com.google.protobuf.Any.VALUE_FIELD_NUMBER) +
                CodedOutputStream.computeInt32SizeNoTag(value.size()) +
                value.size()

        output.writeInt32NoTag(size)
        output.writeString(com.google.protobuf.Any.TYPE_URL_FIELD_NUMBER, value.typeUrl())
        output.writeTag(com.google.protobuf.Any.VALUE_FIELD_NUMBER, WireFormat.WIRETYPE_LENGTH_DELIMITED)
        output.writeInt32NoTag(value.size())
        value.writeTo(output)
    }
}
