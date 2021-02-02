package com.bybutter.sisyphus.protobuf.compiler.resourcename

import com.bybutter.sisyphus.protobuf.compiler.core.state.ChildGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.GeneratingState
import com.squareup.kotlinpoet.TypeSpec

class ResourceNameGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: ResourceDescriptor,
    override val target: TypeSpec.Builder
) : ChildGeneratingState<ResourceDescriptor, TypeSpec.Builder>

class ResourceNameCompanionGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: ResourceDescriptor,
    override val target: TypeSpec.Builder
) : ChildGeneratingState<ResourceDescriptor, TypeSpec.Builder>
