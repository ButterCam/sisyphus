package com.bybutter.sisyphus.protobuf.compiler.core.state

import com.bybutter.sisyphus.protobuf.compiler.MessageFieldDescriptor
import com.bybutter.sisyphus.protobuf.compiler.WhenBranchBuilder
import com.squareup.kotlinpoet.CodeBlock

class MessageClearInCurrentFunctionGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: MessageFieldDescriptor,
    override val target: WhenBranchBuilder,
) : ChildGeneratingState<MessageFieldDescriptor, WhenBranchBuilder>

class MessageGetInCurrentFunctionGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: MessageFieldDescriptor,
    override val target: WhenBranchBuilder,
) : ChildGeneratingState<MessageFieldDescriptor, WhenBranchBuilder>

class MessageSetFieldInCurrentFunctionGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: MessageFieldDescriptor,
    override val target: WhenBranchBuilder,
) : ChildGeneratingState<MessageFieldDescriptor, WhenBranchBuilder>

class MessageHasFieldInCurrentFunctionGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: MessageFieldDescriptor,
    override val target: WhenBranchBuilder,
) : ChildGeneratingState<MessageFieldDescriptor, WhenBranchBuilder>

class MessageEqualsFunctionGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: MessageFieldDescriptor,
    override val target: CodeBlock.Builder,
) : ChildGeneratingState<MessageFieldDescriptor, CodeBlock.Builder>

class MessageHashCodeFunctionGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: MessageFieldDescriptor,
    override val target: CodeBlock.Builder,
) : ChildGeneratingState<MessageFieldDescriptor, CodeBlock.Builder>

class MessageWriteFieldsFunctionGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: MessageFieldDescriptor,
    override val target: CodeBlock.Builder,
) : ChildGeneratingState<MessageFieldDescriptor, CodeBlock.Builder>

class MessageReadFieldFunctionGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: MessageFieldDescriptor,
    override val target: CodeBlock.Builder,
) : ChildGeneratingState<MessageFieldDescriptor, CodeBlock.Builder>
