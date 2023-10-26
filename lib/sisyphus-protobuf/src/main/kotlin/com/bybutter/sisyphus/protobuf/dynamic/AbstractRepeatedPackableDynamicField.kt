package com.bybutter.sisyphus.protobuf.dynamic

import com.bybutter.sisyphus.protobuf.coded.Reader
import com.bybutter.sisyphus.protobuf.coded.WireType
import com.bybutter.sisyphus.protobuf.coded.Writer
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto

abstract class AbstractRepeatedPackableDynamicField<T>(
    descriptor: FieldDescriptorProto,
) : AbstractRepeatedDynamicField<T>(descriptor) {
    abstract fun read0(
        reader: Reader,
        field: Int,
        wire: Int,
    )

    abstract fun write0(
        writer: Writer,
        value: T,
    )

    override fun writeTo(writer: Writer) {
        if (!has()) return
        val list = get()
        if (list.isEmpty()) return

        if (list.size == 1) {
            for (value in list) {
                writer.tag(descriptor().number, WireType.ofType(descriptor().type))
                write0(writer, value)
            }
        } else {
            writer.tag(descriptor().number, WireType.LENGTH_DELIMITED)
                .beginLd()
            for (value in list) {
                write0(writer, value)
            }
            writer.endLd()
        }
    }

    override fun read(
        reader: Reader,
        field: Int,
        wire: Int,
    ) {
        reader.packed(wire) {
            read0(reader, field, wire)
        }
    }
}
