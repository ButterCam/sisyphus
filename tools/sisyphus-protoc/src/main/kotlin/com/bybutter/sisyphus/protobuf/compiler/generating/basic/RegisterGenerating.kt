package com.bybutter.sisyphus.protobuf.compiler.generating.basic

import com.bybutter.sisyphus.protobuf.compiler.generating.Generating
import com.bybutter.sisyphus.protobuf.compiler.generating.MessageGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.ServiceGenerating
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.FunSpec

interface RegisterGenerating<TParent, TDescriptor> : Generating<TParent, TDescriptor, FunSpec.Builder>

data class MessageRegisterGeneratingState(
    override val parent: ImplementationFileGeneratingState,
    override val descriptor: DescriptorProtos.DescriptorProto,
    override val target: FunSpec.Builder
) : RootMessageGenerating<ImplementationFileGeneratingState, FunSpec.Builder>, RegisterGenerating<ImplementationFileGeneratingState, DescriptorProtos.DescriptorProto>

data class NestedMessageRegisterGeneratingState(
    override val parent: MessageGenerating<*, *>,
    override val descriptor: DescriptorProtos.DescriptorProto,
    override val target: FunSpec.Builder
) : NestedMessageGenerating<MessageGenerating<*, *>, FunSpec.Builder>, RegisterGenerating<MessageGenerating<*, *>, DescriptorProtos.DescriptorProto>

data class EnumRegisterGeneratingState(
    override val parent: ImplementationFileGeneratingState,
    override val descriptor: DescriptorProtos.EnumDescriptorProto,
    override val target: FunSpec.Builder
) : RootEnumGenerating<ImplementationFileGeneratingState, FunSpec.Builder>, RegisterGenerating<ImplementationFileGeneratingState, DescriptorProtos.EnumDescriptorProto>

data class NestedEnumRegisterGeneratingState(
    override val parent: MessageGenerating<*, *>,
    override val descriptor: DescriptorProtos.EnumDescriptorProto,
    override val target: FunSpec.Builder
) : NestedEnumGenerating<MessageGenerating<*, *>, FunSpec.Builder>, RegisterGenerating<MessageGenerating<*, *>, DescriptorProtos.EnumDescriptorProto>

data class ExtensionRegisterGeneratingState(
    override val parent: ImplementationFileGeneratingState,
    override val descriptor: DescriptorProtos.FieldDescriptorProto,
    override val target: FunSpec.Builder
) : RootExtensionFieldGenerating<ImplementationFileGeneratingState, FunSpec.Builder>, RegisterGenerating<ImplementationFileGeneratingState, DescriptorProtos.FieldDescriptorProto>

data class NestedExtensionRegisterGeneratingState(
    override val parent: MessageGenerating<*, *>,
    override val descriptor: DescriptorProtos.FieldDescriptorProto,
    override val target: FunSpec.Builder
) : NestedExtensionFieldGenerating<MessageGenerating<*, *>, FunSpec.Builder>, RegisterGenerating<MessageGenerating<*, *>, DescriptorProtos.FieldDescriptorProto>

data class ServiceRegisterGeneratingState(
    override val parent: ImplementationFileGeneratingState,
    override val descriptor: DescriptorProtos.ServiceDescriptorProto,
    override val target: FunSpec.Builder
) : ServiceGenerating<ImplementationFileGeneratingState, FunSpec.Builder>, RegisterGenerating<ImplementationFileGeneratingState, DescriptorProtos.ServiceDescriptorProto>
