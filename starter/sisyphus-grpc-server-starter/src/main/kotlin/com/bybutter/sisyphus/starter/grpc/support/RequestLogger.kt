package com.bybutter.sisyphus.starter.grpc.support

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

    fun log(call: ServerCall<*, *>, metadata: Metadata, status: Status, trailers: Metadata, cost: Long)
}

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
open class DefaultRequestLogger : RequestLogger {
    override val id: String = RequestLogger::class.java.typeName

    override fun log(call: ServerCall<*, *>, metadata: Metadata, status: Status, trailers: Metadata, cost: Long) {
        if (status.isOk) {
            logger.info("[${status.code}] ${call.methodDescriptor.fullMethodName}(${trailers.get(SisyphusGrpcServerInterceptor.REQUEST_ID_META_KEY)}) +${getCostString(cost)}")
        } else {
            logger.error("[${status.code}] ${call.methodDescriptor.fullMethodName}(${trailers.get(SisyphusGrpcServerInterceptor.REQUEST_ID_META_KEY)}) +${getCostString(cost)}", status.cause)
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
