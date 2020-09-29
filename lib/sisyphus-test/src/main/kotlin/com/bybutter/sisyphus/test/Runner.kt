package com.bybutter.sisyphus.test

import com.bybutter.sisyphus.cel.CelEngine
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.rpc.ServiceSupport
import io.grpc.CallOptions
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.kotlin.ClientCalls
import kotlin.reflect.full.companionObjectInstance
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

fun TestCase.run() {
    for (serviceTestSet in this.serviceTestSet) {
        run(serviceTestSet)
    }
}

private fun TestCase.run(serviceTestSet: ServiceTestSet) = runBlocking {
    val service = ProtoTypes.getRegisterService(serviceTestSet.service)?.kotlin?.companionObjectInstance as ServiceSupport
    val channel = ManagedChannelBuilder.forTarget(serviceTestSet.authority).usePlaintext().build()

    for (methodTest in serviceTestSet.methodTests) {
        val metadata = (this@run.metadata + serviceTestSet.metadata + methodTest.metadata).filterValues { it.isNotEmpty() }
        val method = service.serviceDescriptor.methods.first { it.fullMethodName == "${serviceTestSet.service}/${methodTest.method}" }
        val mn = Metadata().apply {
            for ((k, v) in metadata) {
                this.put(Metadata.Key.of(k, Metadata.ASCII_STRING_MARSHALLER), v)
            }
        }

        val result = ClientCalls.unaryRpc(channel, method as MethodDescriptor<Message<*, *>, Message<*, *>>, methodTest.input!!, CallOptions.DEFAULT, mn)
        val celEngine = CelEngine(mapOf(
                "request" to methodTest.input,
                "response" to result
        ))

        for (assert in methodTest.asserts) {

            val logger = LoggerFactory.getLogger(this::class.java)

            if (celEngine.eval(assert) != false) {
                return@runBlocking logger.info("Pass")
            } else {
                return@runBlocking logger.error("Fail")
            }
        }
    }
}