package com.bybutter.sisyphus.protobuf.compiler.generating.basic

import com.bybutter.sisyphus.protobuf.compiler.generating.ExtensionFieldGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.FileGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.MessageGenerating
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.Taggable
import com.squareup.kotlinpoet.TypeSpec

interface RootExtensionFieldGenerating<TParent : FileGenerating<*, *>, TTarget> :
    ExtensionFieldGenerating<TParent, TTarget>

interface NestedExtensionFieldGenerating<TParent : MessageGenerating<*, *>, TTarget> :
    ExtensionFieldGenerating<TParent, TTarget>

data class ExtensionFieldGeneratingState(
    override val parent: ApiFileGeneratingState,
    override val descriptor: DescriptorProtos.FieldDescriptorProto,
    override val target: FileSpec.Builder
) : RootExtensionFieldGenerating<ApiFileGeneratingState, FileSpec.Builder>, ApiGenerating

data class ExtensionFieldSupportGeneratingState(
    override val parent: ImplementationFileGeneratingState,
    override val descriptor: DescriptorProtos.FieldDescriptorProto,
    override val target: FileSpec.Builder
) : RootExtensionFieldGenerating<ImplementationFileGeneratingState, FileSpec.Builder>, SupportGenerating

data class NestedExtensionFieldGeneratingState(
    override val parent: MessageGenerating<*, *>,
    override val descriptor: DescriptorProtos.FieldDescriptorProto,
    override val target: TypeSpec.Builder
) : NestedExtensionFieldGenerating<MessageGenerating<*, *>, TypeSpec.Builder>, ApiGenerating

data class NestedExtensionFieldSupportGeneratingState(
    override val parent: MessageGenerating<*, *>,
    override val descriptor: DescriptorProtos.FieldDescriptorProto,
    override val target: TypeSpec.Builder
) : NestedExtensionFieldGenerating<MessageGenerating<*, *>, TypeSpec.Builder>, SupportGenerating
