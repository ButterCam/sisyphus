package com.bybutter.sisyphus.starter.grpc.support

import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.rpc.Debug
import com.bybutter.sisyphus.rpc.STATUS_META_KEY
import com.bybutter.sisyphus.rpc.StatusException
import com.bybutter.sisyphus.rpc.Trailers
import com.bybutter.sisyphus.rpc.fromGrpcStatus
import com.bybutter.sisyphus.rpc.toGrpcStatus
import com.bybutter.sisyphus.string.randomString
import io.grpc.Context
import io.grpc.Contexts
import io.grpc.ForwardingServerCall
import io.grpc.ForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1000)
class SisyphusGrpcServerInterceptor : ServerInterceptor {
    @Autowired(required = false)
    private var loggers: List<RequestLogger> = listOf()

    private val uniqueLoggers: List<RequestLogger> by lazy {
        val addedLogger = mutableSetOf<String>()
        loggers.mapNotNull {
            if (it.id.isNotEmpty() && addedLogger.contains(it.id)) return@mapNotNull null
            it
        }
    }

    override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val requestId = headers.get(REQUEST_ID_META_KEY) ?: randomString(12)

        var context = Trailers.initTrailers(Context.current()) {
            put(REQUEST_ID_META_KEY, requestId)
        }

        if (headers.get(DEBUG_META_KEY) != null) {
            context = Debug.initDebug(context)
        }

        context = context.withValue(RequestLogger.REQUEST_CONTEXT_KEY, RequestInfo(headers))

        return try {
            Contexts.interceptCall(context, SisyphusGrpcServerCall(call, uniqueLoggers), headers) { call, headers ->
                SisyphusGrpcServerCallListener(next.startCall(call, headers))
            }
        } catch (e: Exception) {
            Trailers.CUSTOM_TAILS_KEY.get(context)?.let {
                call.sendHeaders(it)
            }
            throw e
        }
    }

    private class SisyphusGrpcServerCall<ReqT, RespT>(
        call: ServerCall<ReqT, RespT>,
        private val loggers: List<RequestLogger>
    ) : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
        private val requestNanoTime = System.nanoTime()

        override fun close(status: Status, trailers: Metadata) {
            Trailers.CUSTOM_TAILS_KEY.get()?.let { trailers.merge(it) }

            val cause = status.cause
            if (cause is StatusException) {
                trailers.merge(cause.trailers)
            }

            RequestLogger.REQUEST_CONTEXT_KEY.get().apply {
                outputTrailers = trailers
                logRequest(status, this)
            }

            if (Debug.debugEnabled) {
                return closeWithStatus(status, trailers)
            }

            if (status.isOk) {
                return super.close(status, trailers)
            }

            return closeWithStatus(status, trailers)
        }

        override fun sendMessage(message: RespT) {
            RequestLogger.REQUEST_CONTEXT_KEY.get()?.outputMessage?.apply {
                this += message as Message<*, *>
            }
            super.sendMessage(message)
        }

        private fun logRequest(status: Status, requestInfo: RequestInfo) {
            val cost = System.nanoTime() - requestNanoTime

            try {
                for (logger in loggers) {
                    logger.log(this, requestInfo, status, cost)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

        private fun closeWithStatus(status: Status, trailers: Metadata) {
            val resolvedStatus = com.bybutter.sisyphus.rpc.Status.fromGrpcStatus(status)
            trailers.put(STATUS_META_KEY, resolvedStatus)

            super.close(resolvedStatus.toGrpcStatus(status.cause), trailers)
        }
    }

    private class SisyphusGrpcServerCallListener<T>(listener: ServerCall.Listener<T>) :
        ForwardingServerCallListener.SimpleForwardingServerCallListener<T>(listener) {
        override fun onMessage(message: T) {
            RequestLogger.REQUEST_CONTEXT_KEY.get()?.inputMessage?.apply {
                this += message as Message<*, *>
            }
            super.onMessage(message)
        }
    }

    companion object {
        val REQUEST_ID_META_KEY: Metadata.Key<String> =
            Metadata.Key.of("X-Request-Id", Metadata.ASCII_STRING_MARSHALLER)
        val DEBUG_META_KEY: Metadata.Key<String> = Metadata.Key.of("X-Debug-Token", Metadata.ASCII_STRING_MARSHALLER)
    }
}
