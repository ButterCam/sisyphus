package com.bybutter.sisyphus.protobuf.compiler.rpc

import com.bybutter.sisyphus.protobuf.compiler.FileDescriptor
import com.bybutter.sisyphus.protobuf.compiler.GeneratedKotlinFile
import com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator
import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.SortableGenerator
import com.bybutter.sisyphus.protobuf.compiler.booster.ProtobufBoosterGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.FileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.advance
import com.bybutter.sisyphus.protobuf.compiler.getter
import com.bybutter.sisyphus.protobuf.compiler.kClass
import com.bybutter.sisyphus.protobuf.compiler.kFile
import com.bybutter.sisyphus.protobuf.compiler.plusAssign
import com.bybutter.sisyphus.protobuf.compiler.property
import com.squareup.kotlinpoet.KModifier

class SeparatedCoroutineServiceBoosterGenerator : GroupedGenerator<ProtobufBoosterGeneratingState> {
    override fun generate(state: ProtobufBoosterGeneratingState): Boolean {
        state.target.order = -1000

        state.descriptor.services.forEach {
            state.target.builder.addStatement(
                "reflection.register(%T)",
                it.className(),
            )
        }
        return true
    }
}

class SeparatedCoroutineServiceApiFileGenerator : GroupedGenerator<FileGeneratingState> {
    override fun generate(state: FileGeneratingState): Boolean {
        if (state.descriptor.services.isNotEmpty()) {
            state.target +=
                GeneratedKotlinFile(
                    kFile(state.descriptor.packageName(), state.descriptor.rpcKotlinFileName()) {
                        RpcApiFileGeneratingState(state, state.descriptor, this).advance()
                    },
                )
        }
        return true
    }
}

class SeparatedCoroutineServiceInternalFileGenerator : GroupedGenerator<FileGeneratingState> {
    override fun generate(state: FileGeneratingState): Boolean {
        if (state.descriptor.services.isNotEmpty()) {
            state.target +=
                GeneratedKotlinFile(
                    kFile(state.descriptor.internalPackageName(), state.descriptor.rpcKotlinFileName()) {
                        RpcInternalFileGeneratingState(state, state.descriptor, this).advance()
                    },
                )
        }
        return true
    }
}

class SeparatedCoroutineServiceGenerator :
    GroupedGenerator<RpcApiFileGeneratingState>,
    SortableGenerator<RpcApiFileGeneratingState> {
    override val group: String = CoroutineServiceGenerator::class.java.canonicalName
    override val order: Int = -1000

    override fun generate(state: RpcApiFileGeneratingState): Boolean {
        for (service in state.descriptor.services) {
            state.target.addType(
                kClass(service.name()) {
                    ServiceGeneratingState(state, service, this).advance()
                },
            )
        }
        return true
    }
}

class SeparatedCoroutineServiceSupportGenerator :
    GroupedGenerator<RpcInternalFileGeneratingState>,
    SortableGenerator<RpcInternalFileGeneratingState> {
    override val group: String = CoroutineServiceSupportGenerator::class.java.canonicalName
    override val order: Int = -1000

    override fun generate(state: RpcInternalFileGeneratingState): Boolean {
        for (service in state.descriptor.services) {
            state.target.addType(
                kClass(service.supportName()) {
                    ServiceSupportGeneratingState(state, service, this).advance()
                },
            )
        }
        return true
    }
}

class SeparatedCoroutineServiceSupportBasicGenerator :
    GroupedGenerator<ServiceSupportGeneratingState>,
    SortableGenerator<ServiceSupportGeneratingState> {
    override val order: Int = 1000

    override fun generate(state: ServiceSupportGeneratingState): Boolean {
        state.target.propertySpecs.removeIf { it.name == "parent" }
        state.target.property("parent", RuntimeTypes.FILE_SUPPORT) {
            this += KModifier.OVERRIDE
            getter {
                addStatement("return %T", state.descriptor.parent.fileMetadataClassName())
            }
        }
        return true
    }
}

fun FileDescriptor.rpcKotlinFileName(): String {
    return "${kotlinFileName()}Rpc"
}
