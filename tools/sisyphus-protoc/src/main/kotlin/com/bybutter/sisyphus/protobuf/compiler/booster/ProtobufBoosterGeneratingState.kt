package com.bybutter.sisyphus.protobuf.compiler.booster

import com.bybutter.sisyphus.protobuf.compiler.FileDescriptor
import com.bybutter.sisyphus.protobuf.compiler.core.state.ChildGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.GeneratingState

class ProtobufBoosterGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: FileDescriptor,
    override val target: ProtobufBoosterContext
) : ChildGeneratingState<FileDescriptor, ProtobufBoosterContext>
