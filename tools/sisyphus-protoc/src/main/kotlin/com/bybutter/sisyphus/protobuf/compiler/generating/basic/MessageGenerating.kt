package com.bybutter.sisyphus.protobuf.compiler.generating.basic

import com.bybutter.sisyphus.protobuf.compiler.WhenBranchBuilder
import com.bybutter.sisyphus.protobuf.compiler.generating.FileGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.MessageGenerating
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

interface RootMessageGenerating<TParent : FileGenerating<*, *>, TTarget> : MessageGenerating<TParent, TTarget>

interface NestedMessageGenerating<TParent : MessageGenerating<*, *>, TTarget> : MessageGenerating<TParent, TTarget>

data class MessageGeneratingState(
    override val parent: ApiFileGeneratingState,
    override val descriptor: DescriptorProtos.DescriptorProto,
    override val target: FileSpec.Builder
) : RootMessageGenerating<ApiFileGeneratingState, FileSpec.Builder>, ApiGenerating

data class NestedMessageGeneratingState(
    override val parent: MessageGenerating<*, *>,
    override val descriptor: DescriptorProtos.DescriptorProto,
    override val target: TypeSpec.Builder
) : NestedMessageGenerating<MessageGenerating<*, *>, TypeSpec.Builder>, ApiGenerating

data class MutableMessageGeneratingState(
    override val parent: ImplementationFileGeneratingState,
    override val descriptor: DescriptorProtos.DescriptorProto,
    override val target: FileSpec.Builder
) : RootMessageGenerating<ImplementationFileGeneratingState, FileSpec.Builder>, MutableGenerating

data class NestedMutableMessageGeneratingState(
    override val parent: MessageGenerating<*, *>,
    override val descriptor: DescriptorProtos.DescriptorProto,
    override val target: TypeSpec.Builder
) : NestedMessageGenerating<MessageGenerating<*, *>, TypeSpec.Builder>, MutableGenerating

data class MessageImplementationGeneratingState(
    override val parent: ImplementationFileGeneratingState,
    override val descriptor: DescriptorProtos.DescriptorProto,
    override val target: FileSpec.Builder
) : RootMessageGenerating<ImplementationFileGeneratingState, FileSpec.Builder>, ImplementationGenerating

data class NestedMessageImplementationGeneratingState(
    override val parent: MessageGenerating<*, *>,
    override val descriptor: DescriptorProtos.DescriptorProto,
    override val target: TypeSpec.Builder
) : NestedMessageGenerating<MessageGenerating<*, *>, TypeSpec.Builder>, ImplementationGenerating

data class MessageFunctionGeneratingState(
    override val parent: MessageGenerating<*, *>,
    override val descriptor: DescriptorProtos.DescriptorProto,
    override val target: TypeSpec.Builder
) : MessageGenerating<MessageGenerating<*, *>, TypeSpec.Builder>, ImplementationFunctionGenerating

data class MessageSupportGeneratingState(
    override val parent: ImplementationFileGeneratingState,
    override val descriptor: DescriptorProtos.DescriptorProto,
    override val target: FileSpec.Builder
) : RootMessageGenerating<ImplementationFileGeneratingState, FileSpec.Builder>, SupportGenerating

data class NestedMessageSupportGeneratingState(
    override val parent: MessageGenerating<*, *>,
    override val descriptor: DescriptorProtos.DescriptorProto,
    override val target: TypeSpec.Builder
) : NestedMessageGenerating<MessageGenerating<*, *>, TypeSpec.Builder>, SupportGenerating

data class MessageClearFieldInCurrentFunctionGeneratingState(
    override val parent: MessageFunctionGeneratingState,
    override val descriptor: DescriptorProtos.FieldDescriptorProto,
    override val target: WhenBranchBuilder
) : BaseFieldGenerating<MessageFunctionGeneratingState, WhenBranchBuilder>, ImplementationFunctionGenerating

data class MessageGetFieldInCurrentFunctionGeneratingState(
    override val parent: MessageFunctionGeneratingState,
    override val descriptor: DescriptorProtos.FieldDescriptorProto,
    override val target: WhenBranchBuilder
) : BaseFieldGenerating<MessageFunctionGeneratingState, WhenBranchBuilder>, ImplementationFunctionGenerating

data class MessageGetPropertyFunctionGeneratingState(
    override val parent: MessageFunctionGeneratingState,
    override val descriptor: DescriptorProtos.FieldDescriptorProto,
    override val target: WhenBranchBuilder
) : BaseFieldGenerating<MessageFunctionGeneratingState, WhenBranchBuilder>, ImplementationFunctionGenerating

data class MessageSetFieldInCurrentFunctionGeneratingState(
    override val parent: MessageFunctionGeneratingState,
    override val descriptor: DescriptorProtos.FieldDescriptorProto,
    override val target: WhenBranchBuilder
) : BaseFieldGenerating<MessageFunctionGeneratingState, WhenBranchBuilder>, ImplementationFunctionGenerating

data class MessageHasFieldInCurrentFunctionGeneratingState(
    override val parent: MessageFunctionGeneratingState,
    override val descriptor: DescriptorProtos.FieldDescriptorProto,
    override val target: WhenBranchBuilder
) : BaseFieldGenerating<MessageFunctionGeneratingState, WhenBranchBuilder>, ImplementationFunctionGenerating

data class MessageEqualsFunctionGeneratingState(
    override val parent: MessageFunctionGeneratingState,
    override val descriptor: DescriptorProtos.FieldDescriptorProto,
    override val target: CodeBlock.Builder
) : BaseFieldGenerating<MessageFunctionGeneratingState, CodeBlock.Builder>, ImplementationFunctionGenerating

data class MessageComputeHashCodeFunctionGeneratingState(
    override val parent: MessageFunctionGeneratingState,
    override val descriptor: DescriptorProtos.FieldDescriptorProto,
    override val target: CodeBlock.Builder
) : BaseFieldGenerating<MessageFunctionGeneratingState, CodeBlock.Builder>, ImplementationFunctionGenerating

data class MessageWriteFieldsFunctionGeneratingState(
    override val parent: MessageFunctionGeneratingState,
    override val descriptor: DescriptorProtos.FieldDescriptorProto,
    override val target: CodeBlock.Builder
) : BaseFieldGenerating<MessageFunctionGeneratingState, CodeBlock.Builder>, ImplementationFunctionGenerating

data class MessageReadFieldFunctionGeneratingState(
    override val parent: MessageFunctionGeneratingState,
    override val descriptor: DescriptorProtos.FieldDescriptorProto,
    override val target: CodeBlock.Builder
) : BaseFieldGenerating<MessageFunctionGeneratingState, CodeBlock.Builder>, ImplementationFunctionGenerating

data class MessageOptionGeneratingState(
    override val parent: MessageGenerating<*, *>,
    override val descriptor: DescriptorProtos.DescriptorProto,
    override val target: TypeSpec.Builder
) : MessageGenerating<MessageGenerating<*, *>, TypeSpec.Builder>, OptionGenerating

data class MessageOptionSupportGeneratingState(
    override val parent: MessageGenerating<*, *>,
    override val descriptor: DescriptorProtos.DescriptorProto,
    override val target: TypeSpec.Builder
) : MessageGenerating<MessageGenerating<*, *>, TypeSpec.Builder>, OptionSupportGenerating
