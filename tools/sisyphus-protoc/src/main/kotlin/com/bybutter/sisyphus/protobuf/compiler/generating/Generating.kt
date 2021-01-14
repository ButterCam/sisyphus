package com.bybutter.sisyphus.protobuf.compiler.generating

import com.bybutter.sisyphus.protobuf.compiler.ProtobufCompiler
import com.bybutter.sisyphus.protobuf.compiler.generator.CodeGenerators
import com.google.protobuf.DescriptorProtos

interface Generating<TParent, TDesc, TTarget> {
    val parent: TParent

    val descriptor: TDesc

    val target: TTarget
}

interface FileGenerating<TParent, TTarget> : Generating<TParent, DescriptorProtos.FileDescriptorProto, TTarget>

interface MessageGenerating<TParent, TTarget> : Generating<TParent, DescriptorProtos.DescriptorProto, TTarget>

interface ExtensionFieldGenerating<TParent, TTarget> : Generating<TParent, DescriptorProtos.FieldDescriptorProto, TTarget>

interface MessageFieldGenerating<TParent, TTarget> : Generating<TParent, DescriptorProtos.FieldDescriptorProto, TTarget>

interface MessageOneofFieldGenerating<TParent, TTarget> : Generating<TParent, DescriptorProtos.OneofDescriptorProto, TTarget>

interface EnumGenerating<TParent, TTarget> : Generating<TParent, DescriptorProtos.EnumDescriptorProto, TTarget>

interface EnumValueGenerating<TParent, TTarget> : Generating<TParent, DescriptorProtos.EnumValueDescriptorProto, TTarget>

interface ServiceGenerating<TParent, TTarget> : Generating<TParent, DescriptorProtos.ServiceDescriptorProto, TTarget>

interface ServiceMethodGenerating<TParent, TTarget> : Generating<TParent, DescriptorProtos.MethodDescriptorProto, TTarget>

interface CompilerChildGenerating<TDesc, TTarget> : Generating<ProtobufCompiler, TDesc, TTarget>

fun Generating<*, *, *>.compiler(): ProtobufCompiler {
    var state: Any? = this
    while (state is Generating<*, *, *>) {
        state = state.parent
    }
    if(state is ProtobufCompiler) return state
    throw IllegalStateException("Root of 'GenerationState' must be 'ProtobufCompiler'.")
}

fun Generating<*, *, *>.file(): FileGenerating<*, *> {
    var state: Any? = this
    while (state is Generating<*, *, *>) {
        state = state.parent
        if(state is FileGenerating<*, *>) return state
    }
    throw IllegalStateException("FileGenerationState not found in lookup path")
}

fun Generating<*, *, *>.advance() {
    compiler().generators.advance(this)
}
