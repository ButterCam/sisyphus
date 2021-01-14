package com.bybutter.sisyphus.protobuf.compiler.generating.basic

import com.bybutter.sisyphus.protobuf.compiler.ProtobufCompiler
import com.bybutter.sisyphus.protobuf.compiler.generating.CompilerChildGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.EnumGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.ExtensionFieldGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.FileGenerating
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.Taggable
import com.squareup.kotlinpoet.TypeSpec

interface BaseFileGenerating : CompilerChildGenerating<DescriptorProtos.FileDescriptorProto, MutableList<FileSpec>>,
    FileGenerating<ProtobufCompiler, MutableList<FileSpec>>

data class ApiFileGeneratingState(
    override val parent: ProtobufCompiler,
    override val descriptor: DescriptorProtos.FileDescriptorProto,
    override val target: MutableList<FileSpec>
) : BaseFileGenerating, ApiGenerating

data class ImplementationFileGeneratingState(
    override val parent: ProtobufCompiler,
    override val descriptor: DescriptorProtos.FileDescriptorProto,
    override val target: MutableList<FileSpec>
) : BaseFileGenerating, ImplementationGenerating

data class FileOptionGeneratingState(
    override val parent: FileGenerating<*, *>,
    override val descriptor: DescriptorProtos.FileDescriptorProto,
    override val target: FileSpec.Builder
) : FileGenerating<FileGenerating<*, *>, FileSpec.Builder>, OptionGenerating

data class FileOptionSupportGeneratingState(
    override val parent: FileGenerating<*, *>,
    override val descriptor: DescriptorProtos.FileDescriptorProto,
    override val target: FileSpec.Builder
) : FileGenerating<FileGenerating<*, *>, FileSpec.Builder>, OptionSupportGenerating