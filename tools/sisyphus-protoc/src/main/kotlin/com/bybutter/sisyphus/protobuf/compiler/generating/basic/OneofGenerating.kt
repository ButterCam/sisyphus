package com.bybutter.sisyphus.protobuf.compiler.generating.basic

import com.bybutter.sisyphus.protobuf.compiler.generating.MessageOneofFieldGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.MessageGenerating
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.TypeSpec

interface BaseOneofGenerating<TParent : MessageGenerating<*, *>, TTarget> : MessageOneofFieldGenerating<TParent, TTarget>

data class OneofGeneratingState(
    override val parent: MessageGenerating<*, *>,
    override val descriptor: DescriptorProtos.OneofDescriptorProto,
    override val target: TypeSpec.Builder
) : BaseOneofGenerating<MessageGenerating<*, *>, TypeSpec.Builder>, ApiGenerating

data class MutableOneofGeneratingState(
    override val parent: MessageGenerating<*, *>,
    override val descriptor: DescriptorProtos.OneofDescriptorProto,
    override val target: TypeSpec.Builder
) : BaseOneofGenerating<MessageGenerating<*, *>, TypeSpec.Builder>, MutableGenerating

data class OneofImplementationGeneratingState(
    override val parent: MessageGenerating<*, *>,
    override val descriptor: DescriptorProtos.OneofDescriptorProto,
    override val target: TypeSpec.Builder
) : BaseOneofGenerating<MessageGenerating<*, *>, TypeSpec.Builder>, ImplementationGenerating