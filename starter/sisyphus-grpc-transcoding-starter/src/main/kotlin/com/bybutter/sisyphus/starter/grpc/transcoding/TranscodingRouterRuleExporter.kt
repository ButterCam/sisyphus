package com.bybutter.sisyphus.starter.grpc.transcoding

import com.bybutter.sisyphus.api.HttpRule
import com.bybutter.sisyphus.api.http
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.findServiceSupport
import com.bybutter.sisyphus.protobuf.primitives.MethodDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.ServiceDescriptorProto
import io.grpc.MethodDescriptor
import io.grpc.Server
import io.grpc.ServerMethodDefinition
import io.grpc.ServerServiceDefinition
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

interface TranscodingRouterRuleExporter {
    fun export(
        server: Server,
        enableServices: Set<String>,
        rules: MutableList<TranscodingRouterRule>
    )
}

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class HttpTranscodingRouterRuleExporter : TranscodingRouterRuleExporter {
    override fun export(
        server: Server,
        enableServices: Set<String>,
        rules: MutableList<TranscodingRouterRule>
    ) {
        for (service in server.services) {
            if (enableServices.isEmpty() || enableServices.contains(service.serviceDescriptor.name)) {
                exportServices(service, rules)
            }
        }
    }

    private fun exportServices(service: ServerServiceDefinition, rules: MutableList<TranscodingRouterRule>) {
        val serviceProto = ProtoTypes.findServiceSupport(".${service.serviceDescriptor.name}").descriptor
        for (method in service.methods) {
            // Just support transcoding for unary gRPC calls.
            if (method.methodDescriptor.type != MethodDescriptor.MethodType.UNARY) continue
            // Ensure method proto registered.
            val methodProto = serviceProto.method.firstOrNull {
                it.name == method.methodDescriptor.fullMethodName.substringAfter('/')
            } ?: continue
            // Ensure http rule existed.
            val rule = methodProto.options?.http ?: continue

            rules += TranscodingRouterRule(service, method, serviceProto, methodProto, rule)
        }
    }
}

data class TranscodingRouterRule(
    val service: ServerServiceDefinition,
    val method: ServerMethodDefinition<*, *>,
    val serviceProto: ServiceDescriptorProto,
    val methodProto: MethodDescriptorProto,
    val http: HttpRule
)
