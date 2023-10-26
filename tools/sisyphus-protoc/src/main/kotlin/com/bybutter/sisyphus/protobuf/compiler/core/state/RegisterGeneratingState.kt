package com.bybutter.sisyphus.protobuf.compiler.core.state

import com.bybutter.sisyphus.protobuf.compiler.FileDescriptor
import com.bybutter.sisyphus.protobuf.compiler.MessageDescriptor
import com.squareup.kotlinpoet.TypeName

class FileParentGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: FileDescriptor,
    override val target: MutableList<TypeName>,
) : ChildGeneratingState<FileDescriptor, MutableList<TypeName>>

class MessageParentGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: MessageDescriptor,
    override val target: MutableList<TypeName>,
) : ChildGeneratingState<MessageDescriptor, MutableList<TypeName>>
