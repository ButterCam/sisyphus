package com.bybutter.sisyphus.protobuf.compiler.core.state

import com.bybutter.sisyphus.protobuf.compiler.MessageFieldDescriptor
import com.squareup.kotlinpoet.TypeSpec

class FieldImplementationGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: MessageFieldDescriptor,
    override val target: TypeSpec.Builder
) : ChildGeneratingState<MessageFieldDescriptor, TypeSpec.Builder>
