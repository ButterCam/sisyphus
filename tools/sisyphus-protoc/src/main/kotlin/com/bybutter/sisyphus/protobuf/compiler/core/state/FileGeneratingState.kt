package com.bybutter.sisyphus.protobuf.compiler.core.state

import com.bybutter.sisyphus.protobuf.compiler.FileDescriptor
import com.bybutter.sisyphus.protobuf.compiler.GeneratedFile
import com.bybutter.sisyphus.protobuf.compiler.ProtobufCompiler
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

class FileGeneratingState(
    val compiler: ProtobufCompiler,
    override val descriptor: FileDescriptor,
    override val target: MutableList<GeneratedFile>,
) : GeneratingState<FileDescriptor, MutableList<GeneratedFile>>

class DescriptorGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: FileDescriptor,
    override val target: DescriptorProtos.FileDescriptorProto.Builder,
) : ChildGeneratingState<FileDescriptor, DescriptorProtos.FileDescriptorProto.Builder>

class ApiFileGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: FileDescriptor,
    override val target: FileSpec.Builder,
) : ChildGeneratingState<FileDescriptor, FileSpec.Builder>

class InternalFileGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: FileDescriptor,
    override val target: FileSpec.Builder,
) : ChildGeneratingState<FileDescriptor, FileSpec.Builder>

class FileSupportGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: FileDescriptor,
    override val target: TypeSpec.Builder,
) : ChildGeneratingState<FileDescriptor, TypeSpec.Builder>

class FileDescriptorGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: FileDescriptor,
    override val target: PropertySpec.Builder,
) : ChildGeneratingState<FileDescriptor, PropertySpec.Builder>
