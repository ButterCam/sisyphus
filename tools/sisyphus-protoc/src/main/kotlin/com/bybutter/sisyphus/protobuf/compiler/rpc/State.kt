package com.bybutter.sisyphus.protobuf.compiler.rpc

import com.bybutter.sisyphus.protobuf.compiler.FileDescriptor
import com.bybutter.sisyphus.protobuf.compiler.ServiceDescriptor
import com.bybutter.sisyphus.protobuf.compiler.core.state.ChildGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.GeneratingState
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec

class RpcApiFileGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: FileDescriptor,
    override val target: FileSpec.Builder
) : ChildGeneratingState<FileDescriptor, FileSpec.Builder>

class RpcInternalFileGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: FileDescriptor,
    override val target: FileSpec.Builder
) : ChildGeneratingState<FileDescriptor, FileSpec.Builder>

class ServiceGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: ServiceDescriptor,
    override val target: TypeSpec.Builder
) : ChildGeneratingState<ServiceDescriptor, TypeSpec.Builder>

class ClientGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: ServiceDescriptor,
    override val target: TypeSpec.Builder
) : ChildGeneratingState<ServiceDescriptor, TypeSpec.Builder>

class ServiceSupportGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: ServiceDescriptor,
    override val target: TypeSpec.Builder
) : ChildGeneratingState<ServiceDescriptor, TypeSpec.Builder>

class ServiceRegisterGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: ServiceDescriptor,
    override val target: FunSpec.Builder
) : ChildGeneratingState<ServiceDescriptor, FunSpec.Builder>
