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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

val REQUEST_TIMESTAMP_CONTEXT_KEY = Context.key<Long>("request-timestamp")

@Order(Ordered.LOWEST_PRECEDENCE - 1000)
@Component
class RequestLogInterceptor : ServerInterceptor {
    @Autowired(required = false)
    private var loggers: List<RequestLogger> = listOf()

    private val uniqueLoggers: List<RequestLogger> by lazy {
        val addedLogger = mutableSetOf<String>()
        loggers.mapNotNull {
            if (it.id.isNotEmpty() && addedLogger.contains(it.id)) return@mapNotNull null
            it
        }
    }

    override fun <ReqT : Any, RespT : Any> interceptCall(call: ServerCall<ReqT, RespT>, headers: Metadata, next: ServerCallHandler<ReqT, RespT>): ServerCall.Listener<ReqT> {
        return Contexts.interceptCall(Context.current().withValue(REQUEST_TIMESTAMP_CONTEXT_KEY, System.nanoTime()), RequestLoggingCall(call, uniqueLoggers), headers, next)
    }
}

class RequestLoggingCall<ReqT : Any, RespT : Any>(call: ServerCall<ReqT, RespT>, private val loggers: List<RequestLogger>) : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
    override fun close(status: Status, trailers: Metadata) {
        val cost = System.nanoTime() - REQUEST_TIMESTAMP_CONTEXT_KEY.get()

        try {
            for (logger in loggers) {
                logger.log(this, status, trailers, cost)
            }
        } catch (e: Exception) {
            // Ignore
        }

        super.close(status, trailers)
    }
}

interface RequestLogger {
    val id: String

    fun log(call: ServerCall<*, *>, status: Status, trailers: Metadata, cost: Long)
}

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
open class DefaultRequestLogger : RequestLogger {
    override val id: String = RequestLogger::class.java.typeName

    override fun log(call: ServerCall<*, *>, status: Status, trailers: Metadata, cost: Long) {
        if (status.isOk) {
            logger.info("[${status.code}] ${call.methodDescriptor.fullMethodName}(${REQUEST_ID_CONTEXT_KEY.get()}) +${getCostString(cost)}")
        } else {
            logger.error("[${status.code}] ${call.methodDescriptor.fullMethodName}(${REQUEST_ID_CONTEXT_KEY.get()}) +${getCostString(cost)}", status.cause)
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
