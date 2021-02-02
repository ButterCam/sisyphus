package com.bybutter.sisyphus.protobuf.compiler.core.generator

import com.bybutter.sisyphus.io.replaceExtensionName
import com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator
import com.bybutter.sisyphus.protobuf.compiler.RuntimeMethods
import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.beginScope
import com.bybutter.sisyphus.protobuf.compiler.core.state.ApiFileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.FileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.FileParentRegisterGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.FileSupportGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.InternalFileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.advance
import com.bybutter.sisyphus.protobuf.compiler.extends
import com.bybutter.sisyphus.protobuf.compiler.function
import com.bybutter.sisyphus.protobuf.compiler.kFile
import com.bybutter.sisyphus.protobuf.compiler.kObject
import com.bybutter.sisyphus.protobuf.compiler.plusAssign
import com.bybutter.sisyphus.protobuf.compiler.property
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.buildCodeBlock

class ApiFileGenerator : GroupedGenerator<FileGeneratingState> {
    override fun generate(state: FileGeneratingState): Boolean {
        state.target += kFile(state.descriptor.packageName(), state.descriptor.kotlinFileName()) {
            ApiFileGeneratingState(state, state.descriptor, this).advance()
        }
        return true
    }
}

class InternalFileGenerator : GroupedGenerator<FileGeneratingState> {
    override fun generate(state: FileGeneratingState): Boolean {
        state.target += kFile(state.descriptor.internalPackageName(), state.descriptor.kotlinFileName()) {
            InternalFileGeneratingState(state, state.descriptor, this).advance()
        }
        return true
    }
}

class FileSupportGenerator : GroupedGenerator<InternalFileGeneratingState> {
    override fun generate(state: InternalFileGeneratingState): Boolean {
        state.target.addType(kObject(state.descriptor.fileMetadataName()) {
            this extends RuntimeTypes.FILE_SUPPORT

            property("name", String::class) {
                this += KModifier.OVERRIDE
                initializer("%S", state.descriptor.descriptor.name)
            }

            property("descriptor", RuntimeTypes.FILE_DESCRIPTOR_PROTO) {
                this += KModifier.OVERRIDE
                delegate(buildCodeBlock {
                    beginScope("%M", RuntimeMethods.LAZY) {
                        addStatement("readDescriptor(%S)", state.descriptor.descriptor.name.replaceExtensionName("proto", "pb"))
                    }
                })
            }

            function("register") {
                this += KModifier.OVERRIDE
                FileParentRegisterGeneratingState(state, state.descriptor, this).advance()
            }

            FileSupportGeneratingState(state, state.descriptor, this).advance()
        })
        return true
    }
}
