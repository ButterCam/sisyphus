package com.bybutter.sisyphus.protobuf.compiler.generating.basic

import com.bybutter.sisyphus.protobuf.compiler.generating.EnumGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.EnumValueGenerating
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.Taggable
import com.squareup.kotlinpoet.TypeSpec

data class EnumValueGeneratingState(
    override val parent: EnumGenerating<*, *>,
    override val descriptor: DescriptorProtos.EnumValueDescriptorProto,
    override val target: TypeSpec.Builder
): EnumValueGenerating<EnumGenerating<*, *>, TypeSpec.Builder>, ApiGenerating

data class EnumValueOptionGeneratingState(
    override val parent: EnumValueGenerating<*,  *>,
    override val descriptor: DescriptorProtos.EnumValueDescriptorProto,
    override val target: TypeSpec.Builder
) : EnumValueGenerating<EnumValueGenerating<*,  *>, TypeSpec.Builder>, OptionGenerating