package com.bybutter.sisyphus.protobuf.compiler.generating.basic

import com.bybutter.sisyphus.protobuf.compiler.generating.MessageFieldGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.MessageGenerating
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.TypeSpec

interface BaseFieldGenerating<TParent : MessageGenerating<*, *>, TTarget> : MessageFieldGenerating<TParent, TTarget>

data class FieldGeneratingState(
    override val parent: MessageGenerating<*, *>,
    override val descriptor: DescriptorProtos.FieldDescriptorProto,
    override val target: TypeSpec.Builder
) : BaseFieldGenerating<MessageGenerating<*, *>, TypeSpec.Builder>, ApiGenerating

data class MutableFieldGeneratingState(
    override val parent: MessageGenerating<*, *>,
    override val descriptor: DescriptorProtos.FieldDescriptorProto,
    override val target: TypeSpec.Builder
) : BaseFieldGenerating<MessageGenerating<*, *>, TypeSpec.Builder>, MutableGenerating

data class FieldImplementationGeneratingState(
    override val parent: MessageGenerating<*, *>,
    override val descriptor: DescriptorProtos.FieldDescriptorProto,
    override val target: TypeSpec.Builder
) : BaseFieldGenerating<MessageGenerating<*, *>, TypeSpec.Builder>, ImplementationGenerating