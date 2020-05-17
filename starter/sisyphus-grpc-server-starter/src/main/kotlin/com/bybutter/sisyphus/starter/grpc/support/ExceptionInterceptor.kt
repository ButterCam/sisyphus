package com.bybutter.sisyphus.starter.grpc.support

import com.bybutter.sisyphus.rpc.ClientStatusException
import com.bybutter.sisyphus.rpc.STATUS_META_KEY
import com.bybutter.sisyphus.rpc.StatusException
import com.bybutter.sisyphus.rpc.debugEnabled
import com.bybutter.sisyphus.rpc.fromGrpcStatus
import com.bybutter.sisyphus.rpc.toGrpcStatus
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.ForwardingClientCallListener
import io.grpc.ForwardingServerCall
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2000)
class ServerExceptionInterceptor : ServerInterceptor {
    override fun <ReqT : Any, RespT : Any> interceptCall(call: ServerCall<ReqT, RespT>, headers: Metadata, next: ServerCallHandler<ReqT, RespT>): ServerCall.Listener<ReqT> {
        return next.startCall(ExceptionHandlerCall(call), headers)
    }
}

class ExceptionHandlerCall<ReqT : Any, RespT : Any>(call: ServerCall<ReqT, RespT>) : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
    override fun close(status: Status, trailers: Metadata) {
        if (debugEnabled) {
            return closeWithStatus(status, trailers)
        }

        if (status.isOk) {
            return super.close(status, trailers)
        }

        return closeWithStatus(status, trailers)
    }

    private fun closeWithStatus(status: Status, trailers: Metadata) {
        val cause = status.cause
        if (cause is StatusException) {
            trailers.merge(cause.trailers)
        }

        val resolvedStatus = com.bybutter.sisyphus.rpc.Status.fromGrpcStatus(status)
        trailers.put(STATUS_META_KEY, resolvedStatus)

        super.close(resolvedStatus.toGrpcStatus(status.cause), trailers)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExceptionHandlerCall::class.java)
    }
}

@Component
class ClientExceptionInterceptor : ClientInterceptor {
    override fun <ReqT : Any, RespT : Any> interceptCall(method: MethodDescriptor<ReqT, RespT>, callOptions: CallOptions, next: Channel): ClientCall<ReqT, RespT> {
        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            override fun start(responseListener: Listener<RespT>, headers: Metadata) {
                super.start(object : ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    override fun onClose(status: Status, trailers: Metadata) {
                        if (status.isOk) {
                            super.onClose(status, trailers)
                        } else {
                            super.onClose(status.withCause(ClientStatusException(status, trailers)), trailers)
                        }
                    }
                }, headers)
            }
        }
    }
}
