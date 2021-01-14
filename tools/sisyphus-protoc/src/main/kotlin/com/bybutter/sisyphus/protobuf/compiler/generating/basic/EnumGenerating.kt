package com.bybutter.sisyphus.protobuf.compiler.generating.basic

import com.bybutter.sisyphus.protobuf.compiler.generating.EnumGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.FileGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.MessageGenerating
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.Taggable
import com.squareup.kotlinpoet.TypeSpec

interface RootEnumGenerating<TParent : FileGenerating<*, *>, TTarget> : EnumGenerating<TParent, TTarget>

interface NestedEnumGenerating<TParent : MessageGenerating<*, *>, TTarget> : EnumGenerating<TParent, TTarget>

data class EnumGeneratingState(
    override val parent: ApiFileGeneratingState,
    override val descriptor: DescriptorProtos.EnumDescriptorProto,
    override val target: FileSpec.Builder
) : RootEnumGenerating<ApiFileGeneratingState, FileSpec.Builder>, ApiGenerating

data class NestedEnumGeneratingState(
    override val parent: MessageGenerating<*, *>,
    override val descriptor: DescriptorProtos.EnumDescriptorProto,
    override val target: TypeSpec.Builder
) : NestedEnumGenerating<MessageGenerating<*, *>, TypeSpec.Builder>, ApiGenerating

data class EnumSupportGeneratingState(
    override val parent: ImplementationFileGeneratingState,
    override val descriptor: DescriptorProtos.EnumDescriptorProto,
    override val target: FileSpec.Builder
) : RootEnumGenerating<ImplementationFileGeneratingState, FileSpec.Builder>, SupportGenerating

data class NestedEnumSupportGeneratingState(
    override val parent: MessageGenerating<*, *>,
    override val descriptor: DescriptorProtos.EnumDescriptorProto,
    override val target: TypeSpec.Builder
) : NestedEnumGenerating<MessageGenerating<*, *>, TypeSpec.Builder>, SupportGenerating

data class EnumOptionGeneratingState(
    override val parent: EnumGenerating<*,  *>,
    override val descriptor: DescriptorProtos.EnumDescriptorProto,
    override val target: TypeSpec.Builder
) : EnumGenerating<EnumGenerating<*,  *>, TypeSpec.Builder>, OptionGenerating

data class EnumOptionSupportGeneratingState(
    override val parent: EnumGenerating<*,  *>,
    override val descriptor: DescriptorProtos.EnumDescriptorProto,
    override val target: TypeSpec.Builder
) : EnumGenerating<EnumGenerating<*,  *>, TypeSpec.Builder>, OptionSupportGenerating
