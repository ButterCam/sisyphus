package com.bybutter.sisyphus.starter.grpc.transcoding.support.operation

import com.bybutter.sisyphus.longrunning.OperationSupport
import com.bybutter.sisyphus.longrunning.Operations
import com.bybutter.sisyphus.longrunning.cancelOperationRules
import com.bybutter.sisyphus.longrunning.deleteOperationRules
import com.bybutter.sisyphus.longrunning.getOperationRules
import com.bybutter.sisyphus.longrunning.listOperationsRules
import com.bybutter.sisyphus.protobuf.primitives.ServiceDescriptorProto
import com.bybutter.sisyphus.starter.grpc.transcoding.TranscodingRouterRule
import com.bybutter.sisyphus.starter.grpc.transcoding.TranscodingRouterRuleExporter
import io.grpc.Server
import io.grpc.ServerMethodDefinition
import io.grpc.ServerServiceDefinition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class OperationsTranscodingRuleExporter : TranscodingRouterRuleExporter {
    @Autowired(required = false)
    private var operationSupports: List<OperationSupport> = listOf()

    override fun export(server: Server, enableServices: Set<String>, rules: MutableList<TranscodingRouterRule>) {
        val operations = server.services.firstOrNull {
            it.serviceDescriptor.name == Operations.serviceDescriptor.name
        } ?: return

        val serviceProto = Operations.descriptor
        for (method in operations.methods) {
            when (method.methodDescriptor.fullMethodName) {
                Operations.listOperations.fullMethodName -> exportListOperations(
                    operations,
                    method,
                    serviceProto,
                    rules
                )
                Operations.getOperation.fullMethodName -> exportGetOperation(operations, method, serviceProto, rules)
                Operations.deleteOperation.fullMethodName -> exportDeleteOperation(
                    operations,
                    method,
                    serviceProto,
                    rules
                )
                Operations.cancelOperation.fullMethodName -> exportCancelOperation(
                    operations,
                    method,
                    serviceProto,
                    rules
                )
            }
        }
    }

    private fun exportListOperations(
        service: ServerServiceDefinition,
        method: ServerMethodDefinition<*, *>,
        serviceProto: ServiceDescriptorProto,
        rules: MutableList<TranscodingRouterRule>
    ) {
        val methodName = method.methodDescriptor.bareMethodName
        val methodProto = serviceProto.method.firstOrNull { it.name == methodName } ?: return
        for (support in operationSupports) {
            for (rule in support.listOperationsRules()) {
                rules += TranscodingRouterRule(service, method, serviceProto, methodProto, rule)
            }
        }
    }

    private fun exportGetOperation(
        service: ServerServiceDefinition,
        method: ServerMethodDefinition<*, *>,
        serviceProto: ServiceDescriptorProto,
        rules: MutableList<TranscodingRouterRule>
    ) {
        val methodName = method.methodDescriptor.bareMethodName
        val methodProto = serviceProto.method.firstOrNull { it.name == methodName } ?: return
        for (support in operationSupports) {
            for (rule in support.getOperationRules()) {
                rules += TranscodingRouterRule(service, method, serviceProto, methodProto, rule)
            }
        }
    }

    private fun exportDeleteOperation(
        service: ServerServiceDefinition,
        method: ServerMethodDefinition<*, *>,
        serviceProto: ServiceDescriptorProto,
        rules: MutableList<TranscodingRouterRule>
    ) {
        val methodName = method.methodDescriptor.bareMethodName
        val methodProto = serviceProto.method.firstOrNull { it.name == methodName } ?: return
        for (support in operationSupports) {
            for (rule in support.deleteOperationRules()) {
                rules += TranscodingRouterRule(service, method, serviceProto, methodProto, rule)
            }
        }
    }

    private fun exportCancelOperation(
        service: ServerServiceDefinition,
        method: ServerMethodDefinition<*, *>,
        serviceProto: ServiceDescriptorProto,
        rules: MutableList<TranscodingRouterRule>
    ) {
        val methodName = method.methodDescriptor.bareMethodName
        val methodProto = serviceProto.method.firstOrNull { it.name == methodName } ?: return
        for (support in operationSupports) {
            for (rule in support.cancelOperationRules()) {
                rules += TranscodingRouterRule(service, method, serviceProto, methodProto, rule)
            }
        }
    }
}
