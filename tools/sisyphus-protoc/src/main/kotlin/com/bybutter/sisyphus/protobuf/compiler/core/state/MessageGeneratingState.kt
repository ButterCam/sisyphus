package com.bybutter.sisyphus.protobuf.compiler.core.state

import com.bybutter.sisyphus.protobuf.compiler.MessageDescriptor
import com.squareup.kotlinpoet.TypeSpec

class MessageInterfaceGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: MessageDescriptor,
    override val target: TypeSpec.Builder
) : ChildGeneratingState<MessageDescriptor, TypeSpec.Builder>

class MutableMessageInterfaceGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: MessageDescriptor,
    override val target: TypeSpec.Builder
) : ChildGeneratingState<MessageDescriptor, TypeSpec.Builder>

class MessageImplementationGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: MessageDescriptor,
    override val target: TypeSpec.Builder
) : ChildGeneratingState<MessageDescriptor, TypeSpec.Builder>

class MessageSupportGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: MessageDescriptor,
    override val target: TypeSpec.Builder
) : ChildGeneratingState<MessageDescriptor, TypeSpec.Builder>

class MessageCompanionGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: MessageDescriptor,
    override val target: TypeSpec.Builder
) : ChildGeneratingState<MessageDescriptor, TypeSpec.Builder>
