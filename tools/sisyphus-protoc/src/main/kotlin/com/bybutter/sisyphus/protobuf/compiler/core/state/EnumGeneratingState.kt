package com.bybutter.sisyphus.protobuf.compiler.core.state

import com.bybutter.sisyphus.protobuf.compiler.EnumDescriptor
import com.bybutter.sisyphus.protobuf.compiler.EnumValueDescriptor
import com.squareup.kotlinpoet.TypeSpec

class EnumGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: EnumDescriptor,
    override val target: TypeSpec.Builder
) : ChildGeneratingState<EnumDescriptor, TypeSpec.Builder>

class EnumSupportGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: EnumDescriptor,
    override val target: TypeSpec.Builder
) : ChildGeneratingState<EnumDescriptor, TypeSpec.Builder>

class EnumValueGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: EnumValueDescriptor,
    override val target: TypeSpec.Builder
) : ChildGeneratingState<EnumValueDescriptor, TypeSpec.Builder>
