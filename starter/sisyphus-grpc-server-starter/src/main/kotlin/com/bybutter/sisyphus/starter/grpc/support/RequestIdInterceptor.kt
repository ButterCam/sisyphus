package com.bybutter.sisyphus.starter.grpc.support

import com.bybutter.sisyphus.rpc.debugEnabled
import com.bybutter.sisyphus.rpc.initDebug
import com.bybutter.sisyphus.string.randomString
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.Context
import io.grpc.Contexts
import io.grpc.ForwardingClientCall
import io.grpc.ForwardingServerCall
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

val REQUEST_ID_META_KEY: Metadata.Key<String> = Metadata.Key.of("X-Request-Id", Metadata.ASCII_STRING_MARSHALLER)
val REQUEST_ID_CONTEXT_KEY: Context.Key<String> = Context.key(REQUEST_ID_META_KEY.name())

val DEBUG_META_KEY: Metadata.Key<String> = Metadata.Key.of("X-Debug-Token", Metadata.ASCII_STRING_MARSHALLER)

@Order(Ordered.HIGHEST_PRECEDENCE + 1000)
@Component
class ServerRequestIdInterceptor : ServerInterceptor {
    override fun <ReqT : Any, RespT : Any> interceptCall(call: ServerCall<ReqT, RespT>, headers: Metadata, next: ServerCallHandler<ReqT, RespT>): ServerCall.Listener<ReqT> {
        val requestId = headers.get(REQUEST_ID_META_KEY) ?: randomString(12)

        var context = Context.current().withValue(REQUEST_ID_CONTEXT_KEY, requestId)

        if (headers.get(DEBUG_META_KEY) != null) {
            context = initDebug(context)
        }

        return Contexts.interceptCall(context, ServerRequestIdCall(call), headers, next)
    }
}

class ServerRequestIdCall<ReqT : Any, RespT : Any>(call: ServerCall<ReqT, RespT>) : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
    override fun close(status: Status, trailers: Metadata) {
        REQUEST_ID_CONTEXT_KEY.get()?.let {
            trailers.put(REQUEST_ID_META_KEY, it)
        }
        super.close(status, trailers)
    }
}

@Component
class ClientRequestIdInterceptor : ClientInterceptor {
    override fun <ReqT : Any, RespT : Any> interceptCall(method: MethodDescriptor<ReqT, RespT>, callOptions: CallOptions, next: Channel): ClientCall<ReqT, RespT> {
        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            override fun start(responseListener: ClientCall.Listener<RespT>, headers: Metadata) {
                if (REQUEST_ID_CONTEXT_KEY.get() != null) {
                    headers.put(REQUEST_ID_META_KEY, REQUEST_ID_CONTEXT_KEY.get())
                }
                if (debugEnabled) {
                    headers.put(DEBUG_META_KEY, "sisyphus")
                }
                super.start(responseListener, headers)
            }
        }
    }
}
