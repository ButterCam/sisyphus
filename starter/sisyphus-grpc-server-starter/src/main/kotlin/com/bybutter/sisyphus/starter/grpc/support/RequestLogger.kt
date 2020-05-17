package com.bybutter.sisyphus.starter.grpc.support

import io.grpc.Context
import io.grpc.Contexts
import io.grpc.ForwardingServerCall
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

private val logger = LoggerFactory.getLogger("RPC Request")

val REQUEST_TIMESTAMP_CONTEXT_KEY = Context.key<Long>("request-timestamp")

@Order(Ordered.LOWEST_PRECEDENCE - 1000)
@Component
class RequestLogInterceptor : ServerInterceptor {
    override fun <ReqT : Any, RespT : Any> interceptCall(call: ServerCall<ReqT, RespT>, headers: Metadata, next: ServerCallHandler<ReqT, RespT>): ServerCall.Listener<ReqT> {
        return Contexts.interceptCall(Context.current().withValue(REQUEST_TIMESTAMP_CONTEXT_KEY, System.nanoTime()), RequestLoggerCall(call), headers, next)
    }
}

class RequestLoggerCall<ReqT : Any, RespT : Any>(call: ServerCall<ReqT, RespT>) : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
    override fun close(status: Status, trailers: Metadata) {
        val cost = System.nanoTime() - REQUEST_TIMESTAMP_CONTEXT_KEY.get()
        val costString = when {
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

        if (status.isOk) {
            logger.info("[${status.code}] ${delegate().methodDescriptor.fullMethodName} +$costString")
        } else {
            logger.error("[${status.code}] ${delegate().methodDescriptor.fullMethodName} +$costString", status.cause)
        }

        super.close(status, trailers)
    }
}
