package com.bybutter.sisyphus.protobuf.compiler

import com.google.protobuf.DescriptorProtos

open class NestedMessageGenerator(override val parent: MessageGenerator, descriptor: DescriptorProtos.DescriptorProto) : MessageGenerator(parent, descriptor)
