package com.bybutter.sisyphus.protobuf.compiler.core.state

import com.bybutter.sisyphus.protobuf.compiler.MessageFieldDescriptor
import com.bybutter.sisyphus.protobuf.compiler.OneofFieldDescriptor
import com.squareup.kotlinpoet.TypeSpec

class OneofValueTypeGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: OneofFieldDescriptor,
    override val target: TypeSpec.Builder
) : ChildGeneratingState<OneofFieldDescriptor, TypeSpec.Builder>

class OneofKindTypeGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: MessageFieldDescriptor,
    override val target: TypeSpec.Builder
) : ChildGeneratingState<MessageFieldDescriptor, TypeSpec.Builder>
