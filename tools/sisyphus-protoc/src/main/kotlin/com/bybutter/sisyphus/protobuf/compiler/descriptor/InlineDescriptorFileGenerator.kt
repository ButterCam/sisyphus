package com.bybutter.sisyphus.protobuf.compiler.descriptor

import com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator
import com.bybutter.sisyphus.protobuf.compiler.RuntimeMethods
import com.bybutter.sisyphus.protobuf.compiler.SortableGenerator
import com.bybutter.sisyphus.protobuf.compiler.beginScope
import com.bybutter.sisyphus.protobuf.compiler.core.generator.DescriptorFileGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.FileSupportDescriptorGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.state.DescriptorGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.FileDescriptorGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.FileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.advance
import com.bybutter.sisyphus.security.base64
import com.squareup.kotlinpoet.buildCodeBlock

class InlineDescriptorFileGenerator :
    GroupedGenerator<FileGeneratingState>,
    SortableGenerator<FileGeneratingState> {
    override val order: Int get() = -1000
    override val group: String get() = DescriptorFileGenerator::class.java.canonicalName

    override fun generate(state: FileGeneratingState): Boolean {
        return true
    }
}

class InlineFileSupportDescriptorGenerator :
    GroupedGenerator<FileDescriptorGeneratingState>,
    SortableGenerator<FileDescriptorGeneratingState> {
    override val order: Int get() = -1000
    override val group: String get() = FileSupportDescriptorGenerator::class.java.canonicalName

    override fun generate(state: FileDescriptorGeneratingState): Boolean {
        val descriptor = state.descriptor.descriptor.toBuilder().apply {
            DescriptorGeneratingState(state, state.descriptor, this).advance()
        }.build()
        state.target.apply {
            delegate(
                buildCodeBlock {
                    beginScope("%M", RuntimeMethods.LAZY) {
                        addStatement(
                            "readDescriptorInline(%S)",
                            descriptor.toString().base64()
                        )
                    }
                }
            )
        }
        return true
    }
}
