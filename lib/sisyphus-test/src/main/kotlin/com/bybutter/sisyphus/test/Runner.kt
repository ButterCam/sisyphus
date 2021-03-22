package com.bybutter.sisyphus.test

import com.bybutter.sisyphus.dsl.cel.CelEngine
import com.bybutter.sisyphus.protobuf.ProtoTypes
import io.grpc.CallOptions
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.ServiceDescriptor
import io.grpc.kotlin.ClientCalls
import kotlin.reflect.full.memberProperties
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

fun TestCase.run() {
    for (serviceTestSet in this.serviceTestSet) {
        run(serviceTestSet)
    }
}

private val logger = LoggerFactory.getLogger(TestCase::class.java)

private fun TestCase.run(serviceTestSet: ServiceTestSet) = runBlocking {
    val service = ProtoTypes.findServiceSupport("." + serviceTestSet.service)
    val channel = ManagedChannelBuilder.forTarget(serviceTestSet.authority).usePlaintext().build()

    for (methodTest in serviceTestSet.methodTests) {
        val metadata = (this@run.metadata + serviceTestSet.metadata + methodTest.metadata).filterValues { it.isNotEmpty() }

        val mn = Metadata().apply {
            for ((k, v) in metadata) {
                this.put(Metadata.Key.of(k, Metadata.ASCII_STRING_MARSHALLER), v)
            }
        }

        val serviceDescriptor = service.javaClass.kotlin.memberProperties.first {
            it.name == "serviceDescriptor"
        }.get(service) as ServiceDescriptor
        val methodDescriptor = serviceDescriptor.methods.first {
            it.bareMethodName == methodTest.method
        } as MethodDescriptor<Any, Any>

        val result = ClientCalls.unaryRpc(channel, methodDescriptor, methodTest.input!!, CallOptions.DEFAULT, mn)
        val celEngine = CelEngine(mapOf(
                "request" to methodTest.input,
                "response" to result
        ))

        for (assert in methodTest.asserts) {
            when (celEngine.eval(assert)) {
                true -> logger.info("{} pass", methodTest.title)
                false -> {
                    logger.error("{} fail", methodTest.title)
                    throw Exception("${methodTest.title} fail")
                }
                else -> {
                    logger.info("{} unknown", methodTest.title)
                }
            }
        }
    }
}