package com.bybutter.sisyphus.protobuf.dynamic

import com.bybutter.sisyphus.protobuf.coded.Reader
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto

abstract class AbstractPackableDynamicField<T>(
    descriptor: FieldDescriptorProto
) : AbstractDynamicField<T>(descriptor) {
    abstract fun read0(reader: Reader, field: Int, wire: Int)

    override fun read(reader: Reader, field: Int, wire: Int) {
        reader.packed(wire) {
            read0(reader, field, wire)
        }
    }
}
