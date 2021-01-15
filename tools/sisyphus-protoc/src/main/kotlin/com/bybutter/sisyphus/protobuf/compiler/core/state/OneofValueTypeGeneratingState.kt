package com.bybutter.sisyphus.protobuf.compiler.core.state

import com.bybutter.sisyphus.protobuf.compiler.OneofFieldDescriptor
import com.squareup.kotlinpoet.TypeSpec

class OneofValueTypeGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: OneofFieldDescriptor,
    override val target: TypeSpec.Builder
) : ChildGeneratingState<OneofFieldDescriptor, TypeSpec.Builder>
