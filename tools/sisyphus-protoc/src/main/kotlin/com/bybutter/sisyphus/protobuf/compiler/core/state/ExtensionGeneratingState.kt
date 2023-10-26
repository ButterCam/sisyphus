package com.bybutter.sisyphus.protobuf.compiler.core.state

import com.bybutter.sisyphus.protobuf.compiler.ExtensionDescriptor
import com.squareup.kotlinpoet.Taggable
import com.squareup.kotlinpoet.TypeSpec

class ExtensionGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: ExtensionDescriptor,
    override val target: Taggable.Builder<*>,
) : ChildGeneratingState<ExtensionDescriptor, Taggable.Builder<*>>

class ExtensionSupportGeneratingState(
    override val parent: GeneratingState<*, *>,
    override val descriptor: ExtensionDescriptor,
    override val target: TypeSpec.Builder,
) : ChildGeneratingState<ExtensionDescriptor, TypeSpec.Builder>
