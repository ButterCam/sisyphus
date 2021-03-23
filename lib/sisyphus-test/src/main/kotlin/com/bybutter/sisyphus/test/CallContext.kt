package com.bybutter.sisyphus.test

import com.bybutter.sisyphus.protobuf.Message
import io.grpc.CallOptions
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.Status

data class CallContext(
    val case: TestCase,
    val step: TestStep,
    var options: CallOptions,
    val method: MethodDescriptor<Message<*, *>, Message<*, *>>,
    val headers: Metadata,
    val input: Message<*, *>,
    var status: Status? = null,
    var trailers: Metadata? = null,
    var output: Message<*, *>? = null
) {
    fun toCelContext(): Map<String, Any?> {
        return mutableMapOf<String, Any?>().apply {
            this["step"] = this@CallContext.step
            this["case"] = this@CallContext.case
            this["method"] = this@CallContext.method.fullMethodName
            this["status"] = this@CallContext.status?.code?.value()?.toLong() ?: 0L
            this["message"] = this@CallContext.status?.description ?: ""
            this["input"] = this@CallContext.input
            this["output"] = this@CallContext.output
            this["headers"] = this@CallContext.headers.toMap()
            this["trailers"] = this@CallContext.trailers?.toMap()
        }
    }

    fun toTestResult(): TestResult {
        return TestResult {
            this.step = this@CallContext.step
            this.case = this@CallContext.case
            this.method = this@CallContext.method.fullMethodName
            this.status = this@CallContext.status?.code?.value() ?: 0
            this.message = this@CallContext.status?.description ?: ""
            this.input = this@CallContext.input
            this.output = this@CallContext.output
            this.headers += this@CallContext.headers.toMap()
            this@CallContext.trailers?.toMap()?.let {
                this.trailers += it
            }
        }
    }
}
