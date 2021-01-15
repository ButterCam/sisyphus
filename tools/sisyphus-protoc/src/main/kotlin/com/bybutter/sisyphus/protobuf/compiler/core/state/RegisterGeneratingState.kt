package com.bybutter.sisyphus.protobuf.compiler.core.state

import com.bybutter.sisyphus.protobuf.compiler.EnumDescriptor
import com.bybutter.sisyphus.protobuf.compiler.ExtensionDescriptor
import com.bybutter.sisyphus.protobuf.compiler.FileDescriptor
import com.bybutter.sisyphus.protobuf.compiler.MessageDescriptor
import com.squareup.kotlinpoet.FunSpec

class FileParentRegisterGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: FileDescriptor,
    override val target: FunSpec.Builder
) : ChildGeneratingState<FileDescriptor, FunSpec.Builder>

class MessageParentRegisterGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: MessageDescriptor,
    override val target: FunSpec.Builder
) : ChildGeneratingState<MessageDescriptor, FunSpec.Builder>

class MessageRegisterGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: MessageDescriptor,
    override val target: FunSpec.Builder
) : ChildGeneratingState<MessageDescriptor, FunSpec.Builder>

class EnumRegisterGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: EnumDescriptor,
    override val target: FunSpec.Builder
) : ChildGeneratingState<EnumDescriptor, FunSpec.Builder>

class ExtensionRegisterGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: ExtensionDescriptor,
    override val target: FunSpec.Builder
) : ChildGeneratingState<ExtensionDescriptor, FunSpec.Builder>
