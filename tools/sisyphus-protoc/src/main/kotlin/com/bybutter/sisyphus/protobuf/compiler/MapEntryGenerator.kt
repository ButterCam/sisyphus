package com.bybutter.sisyphus.protobuf.compiler

import com.google.protobuf.DescriptorProtos

class MapEntryGenerator(parent: MessageGenerator, descriptor: DescriptorProtos.DescriptorProto) : NestedMessageGenerator(parent, descriptor) {
    val keyField: MapEntryFieldGenerator get() = children[0] as MapEntryFieldGenerator
    val valueField: MapEntryFieldGenerator get() = children[1] as MapEntryFieldGenerator

    override fun init() {
        for (field in descriptor.fieldList) {
            addElement(MapEntryFieldGenerator(this, field))
        }
    }
}
