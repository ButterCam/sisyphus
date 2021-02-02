package com.bybutter.sisyphus.starter.grpc.support

import com.bybutter.sisyphus.protobuf.Message
import io.grpc.Context
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.Status
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

interface RequestLogger {
    val id: String

    fun log(call: ServerCall<*, *>, requestInfo: RequestInfo, status: Status, cost: Long)

    companion object {
        val REQUEST_CONTEXT_KEY: Context.Key<RequestInfo> = Context.key("sisyphus-request")
    }
}

data class RequestInfo(
    var inputHeader: Metadata,
    var outputHeader: Metadata? = null,
    val inputMessage: MutableList<Message<*, *>> = mutableListOf(),
    val outputMessage: MutableList<Message<*, *>> = mutableListOf(),
    var outputTrailers: Metadata? = null
)

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class DefaultRequestLogger : RequestLogger {
    override val id: String = RequestLogger::class.java.typeName

    override fun log(call: ServerCall<*, *>, requestInfo: RequestInfo, status: Status, cost: Long) {
        if (status.isOk) {
            logger.info("[${status.code}] ${call.methodDescriptor.fullMethodName} +${getCostString(cost)}")
        } else {
            logger.error("[${status.code}] ${call.methodDescriptor.fullMethodName} +${getCostString(cost)}", status.cause)
        }
    }

    protected fun getCostString(cost: Long): String {
        return when {
            cost < TimeUnit.MICROSECONDS.toNanos(1) -> {
                "${cost}ns"
            }
            cost < TimeUnit.MILLISECONDS.toNanos(1) -> {
                "${cost / 1000.0}µs"
            }
            cost < TimeUnit.SECONDS.toNanos(1) -> {
                String.format("%.3fms", cost / 1000000.0)
            }
            else -> {
                String.format("%.3fs", cost / 1000000000.0)
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger("RPC Request")
    }
}
