package com.bybutter.sisyphus.protobuf.compiler.generating.basic

import com.bybutter.sisyphus.protobuf.compiler.generating.ServiceGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.ServiceMethodGenerating
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

data class ServiceGeneratingState(
    override val parent: ApiFileGeneratingState,
    override val descriptor: DescriptorProtos.ServiceDescriptorProto,
    override val target: FileSpec.Builder
) : ServiceGenerating<ApiFileGeneratingState, FileSpec.Builder>, ApiGenerating

data class ServiceSupportGeneratingState(
    override val parent: ImplementationFileGeneratingState,
    override val descriptor: DescriptorProtos.ServiceDescriptorProto,
    override val target: FileSpec.Builder
) : ServiceGenerating<ImplementationFileGeneratingState, FileSpec.Builder>, SupportGenerating

data class ServiceMethodGeneratingState(
    override val parent: ServiceGeneratingState,
    override val descriptor: DescriptorProtos.MethodDescriptorProto,
    override val target: TypeSpec.Builder
) : ServiceMethodGenerating<ServiceGeneratingState, TypeSpec.Builder>, ApiGenerating

data class ServiceSupportMethodGeneratingState(
    override val parent: ServiceSupportGeneratingState,
    override val descriptor: DescriptorProtos.MethodDescriptorProto,
    override val target: TypeSpec.Builder
) : ServiceMethodGenerating<ServiceSupportGeneratingState, TypeSpec.Builder>, SupportGenerating

data class ClientMethodGeneratingState(
    override val parent: ServiceGeneratingState,
    override val descriptor: DescriptorProtos.MethodDescriptorProto,
    override val target: TypeSpec.Builder
) : ServiceMethodGenerating<ServiceGeneratingState, TypeSpec.Builder>, ClientGenerating

data class ServiceOptionGeneratingState(
    override val parent: ServiceGenerating<*, *>,
    override val descriptor: DescriptorProtos.ServiceDescriptorProto,
    override val target: TypeSpec.Builder
) : ServiceGenerating<ServiceGenerating<*, *>, TypeSpec.Builder>, OptionGenerating

data class ServiceOptionSupportGeneratingState(
    override val parent: ServiceGenerating<*, *>,
    override val descriptor: DescriptorProtos.ServiceDescriptorProto,
    override val target: TypeSpec.Builder
) : ServiceGenerating<ServiceGenerating<*, *>, TypeSpec.Builder>, OptionSupportGenerating

