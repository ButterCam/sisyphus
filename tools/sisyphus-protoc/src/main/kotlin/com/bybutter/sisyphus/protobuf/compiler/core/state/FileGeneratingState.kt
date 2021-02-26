package com.bybutter.sisyphus.protobuf.compiler.core.state

import com.bybutter.sisyphus.protobuf.compiler.FileDescriptor
import com.bybutter.sisyphus.protobuf.compiler.ProtobufCompiler
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

class FileGeneratingState(
    val compiler: ProtobufCompiler,
    override val descriptor: FileDescriptor,
    override val target: MutableList<FileSpec>
) : GeneratingState<FileDescriptor, MutableList<FileSpec>>

class ApiFileGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: FileDescriptor,
    override val target: FileSpec.Builder
) : ChildGeneratingState<FileDescriptor, FileSpec.Builder>

class InternalFileGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: FileDescriptor,
    override val target: FileSpec.Builder
) : ChildGeneratingState<FileDescriptor, FileSpec.Builder>

class FileSupportGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: FileDescriptor,
    override val target: TypeSpec.Builder
) : ChildGeneratingState<FileDescriptor, TypeSpec.Builder>
