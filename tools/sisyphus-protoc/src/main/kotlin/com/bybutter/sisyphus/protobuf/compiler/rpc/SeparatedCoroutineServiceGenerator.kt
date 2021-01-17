package com.bybutter.sisyphus.protobuf.compiler.rpc

import com.bybutter.sisyphus.io.replaceExtensionName
import com.bybutter.sisyphus.protobuf.compiler.FileDescriptor
import com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator
import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.SortableGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.state.FileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.FileSupportGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.advance
import com.bybutter.sisyphus.protobuf.compiler.extends
import com.bybutter.sisyphus.protobuf.compiler.function
import com.bybutter.sisyphus.protobuf.compiler.getter
import com.bybutter.sisyphus.protobuf.compiler.kClass
import com.bybutter.sisyphus.protobuf.compiler.kFile
import com.bybutter.sisyphus.protobuf.compiler.kObject
import com.bybutter.sisyphus.protobuf.compiler.plusAssign
import com.bybutter.sisyphus.protobuf.compiler.property
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier

class SeparatedCoroutineServiceApiFileGenerator : GroupedGenerator<FileGeneratingState> {
    override fun generate(state: FileGeneratingState): Boolean {
        if(state.descriptor.services.isNotEmpty()) {
            state.target += kFile(state.descriptor.packageName(), state.descriptor.rpcKotlinFileName()) {
                RpcApiFileGeneratingState(state, state.descriptor, this).advance()
            }
        }
        return true
    }
}

class SeparatedCoroutineServiceInternalFileGenerator : GroupedGenerator<FileGeneratingState> {
    override fun generate(state: FileGeneratingState): Boolean {
        if(state.descriptor.services.isNotEmpty()) {
            state.target += kFile(state.descriptor.internalPackageName(), state.descriptor.rpcKotlinFileName()) {
                RpcInternalFileGeneratingState(state, state.descriptor, this).advance()
            }
        }
        return true
    }
}

class SeparatedCoroutineServiceFileSupportGenerator : GroupedGenerator<RpcInternalFileGeneratingState> {
    override fun generate(state: RpcInternalFileGeneratingState): Boolean {
        state.target.addType(kObject(state.descriptor.rpcFileMetadataName()) {
            this extends RuntimeTypes.FILE_SUPPORT

            property("descriptor", RuntimeTypes.FILE_DESCRIPTOR_PROTO) {
                this += KModifier.OVERRIDE
                initializer("readDescriptor(%S)", state.descriptor.descriptor.name.replaceExtensionName("proto", "pb"))
            }

            function("register") {
                this += KModifier.OVERRIDE
                for (service in state.descriptor.services) {
                    ServiceRegisterGeneratingState(state, service, this).advance()
                }
            }

            FileSupportGeneratingState(state, state.descriptor, this).advance()
        })
        return true
    }
}

class SeparatedCoroutineServiceGenerator : GroupedGenerator<RpcApiFileGeneratingState>,
    SortableGenerator<RpcApiFileGeneratingState> {
    override val group: String = CoroutineServiceGenerator::class.java.canonicalName
    override val order: Int = -1000

    override fun generate(state: RpcApiFileGeneratingState): Boolean {
        for (service in state.descriptor.services) {
            state.target.addType(kClass(service.name()) {
                ServiceGeneratingState(state, service, this).advance()
            })
        }
        return true
    }
}

class SeparatedCoroutineServiceSupportGenerator : GroupedGenerator<RpcInternalFileGeneratingState>,
    SortableGenerator<RpcInternalFileGeneratingState> {
    override val group: String = CoroutineServiceSupportGenerator::class.java.canonicalName
    override val order: Int = -1000

    override fun generate(state: RpcInternalFileGeneratingState): Boolean {
        for (service in state.descriptor.services) {
            state.target.addType(kClass(service.supportName()) {
                ServiceSupportGeneratingState(state, service, this).advance()
            })
        }
        return true
    }
}

class SeparatedCoroutineServiceSupportBasicGenerator : GroupedGenerator<ServiceSupportGeneratingState>,
    SortableGenerator<ServiceSupportGeneratingState> {
    override val order: Int = 1000

    override fun generate(state: ServiceSupportGeneratingState): Boolean {
        state.target.propertySpecs.removeIf { it.name == "parent" }
        state.target.property("parent", RuntimeTypes.FILE_SUPPORT) {
            this += KModifier.OVERRIDE
            getter {
                addStatement("return %T", state.descriptor.parent.rpcFileMetadataClassName())
            }
        }
        return true
    }
}

fun FileDescriptor.rpcKotlinFileName(): String {
    return "${kotlinFileName()}Rpc"
}

fun FileDescriptor.rpcFileMetadataName(): String {
    return "${kotlinFileName()}RpcMetadata"
}

fun FileDescriptor.rpcFileMetadataClassName(): ClassName {
    return ClassName(internalPackageName(), rpcFileMetadataName())
}