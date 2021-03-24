package com.bybutter.sisyphus.protobuf.compiler.rxjava

import com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator
import com.bybutter.sisyphus.protobuf.compiler.SortableGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.state.FileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.advance
import com.bybutter.sisyphus.protobuf.compiler.kClass
import com.bybutter.sisyphus.protobuf.compiler.kFile
import com.bybutter.sisyphus.protobuf.compiler.rpc.rpcKotlinFileName

class SeparatedRxClientApiFileGenerator : GroupedGenerator<FileGeneratingState> {
    override fun generate(state: FileGeneratingState): Boolean {
        if (state.descriptor.services.isNotEmpty()) {
            state.target += kFile(state.descriptor.packageName(), state.descriptor.rpcKotlinFileName()) {
                ClientApiFileGeneratingState(state, state.descriptor, this).advance()
            }
        }
        return true
    }
}

class SeparatedRxClientGenerator :
    GroupedGenerator<ClientApiFileGeneratingState>,
    SortableGenerator<ClientApiFileGeneratingState> {
    override val group: String = RxClientGenerator::class.java.canonicalName
    override val order: Int = -1000

    override fun generate(state: ClientApiFileGeneratingState): Boolean {
        for (service in state.descriptor.services) {
            state.target.addType(
                kClass(service.name()) {
                    ClientGeneratingState(state, service, this).advance()
                }
            )
        }
        return true
    }
}
