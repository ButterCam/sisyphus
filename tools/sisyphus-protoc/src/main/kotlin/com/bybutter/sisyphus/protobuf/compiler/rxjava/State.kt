package com.bybutter.sisyphus.protobuf.compiler.rxjava

import com.bybutter.sisyphus.protobuf.compiler.FileDescriptor
import com.bybutter.sisyphus.protobuf.compiler.ServiceDescriptor
import com.bybutter.sisyphus.protobuf.compiler.core.state.ChildGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.GeneratingState
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

class ClientGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: ServiceDescriptor,
    override val target: TypeSpec.Builder
) : ChildGeneratingState<ServiceDescriptor, TypeSpec.Builder>

class ClientApiFileGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: FileDescriptor,
    override val target: FileSpec.Builder
) : ChildGeneratingState<FileDescriptor, FileSpec.Builder>