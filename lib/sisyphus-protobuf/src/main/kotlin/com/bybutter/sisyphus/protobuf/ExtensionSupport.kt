package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import java.util.UUID

abstract class ExtensionSupport<T : Message<T, TM>, TM : MutableMessage<T, TM>> : ProtoSupport<T, TM>(UUID.randomUUID().toString()) {
    abstract val extendedFields: List<FieldDescriptorProto>

    override val fieldDescriptors: List<FieldDescriptorProto>
        get() = extendedFields
}
