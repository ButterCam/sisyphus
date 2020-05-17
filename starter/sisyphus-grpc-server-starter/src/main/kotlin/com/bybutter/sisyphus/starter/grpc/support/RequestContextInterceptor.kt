package com.bybutter.sisyphus.starter.grpc.support

import com.bybutter.sisyphus.middleware.grpc.MutableRequestContext
import com.bybutter.sisyphus.middleware.grpc.RequestContext
import com.bybutter.sisyphus.middleware.grpc.RequestContextProvider
import com.bybutter.sisyphus.reflect.uncheckedCast
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.Context
import io.grpc.Contexts
import io.grpc.ForwardingClientCall
import io.grpc.ForwardingClientCallListener
import io.grpc.ForwardingServerCall
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 2000)
class RequestContextInterceptor : ServerInterceptor {
    @Autowired(required = false)
    internal var providers: List<RequestContextProvider<*>> = listOf()

    val providerMap: Map<String, RequestContextProvider<*>> by lazy {
        providers.associateBy { it.metaKey.toString() }
    }

    override fun <ReqT : Any, RespT : Any> interceptCall(call: ServerCall<ReqT, RespT>, headers: Metadata, next: ServerCallHandler<ReqT, RespT>): ServerCall.Listener<ReqT> {
        var currentContext = Context.current()
        for (key in headers.keys()) {
            if (key.startsWith(RequestContext.REQUEST_CONTEXT_PREFIX)) {
                val provider = providerMap[key]
                currentContext = if (provider == null) {
                    currentContext.withValue(Context.key<String>(key), headers.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)))
                } else {
                    currentContext.withValue(provider.key.key.uncheckedCast(), headers.get(provider.metaKey))
                }
            }
        }
        return Contexts.interceptCall(currentContext, ServerRequestContextCall(this, call), headers, next)
    }

    class ServerRequestContextCall<ReqT : Any, RespT : Any>(private val interceptor: RequestContextInterceptor, call: ServerCall<ReqT, RespT>) : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
        override fun close(status: Status, trailers: Metadata) {
            for (provider in interceptor.providers) {
                val contextKey = provider.key
                if (contextKey is MutableRequestContext<*> && contextKey.changed) {
                    trailers.put(provider.metaKey, provider.key.get().uncheckedCast())
                }
            }
            super.close(status, trailers)
        }
    }
}

@Component
class RequestContextClientInterceptor : ClientInterceptor {
    @Autowired(required = false)
    internal var providers: List<RequestContextProvider<*>> = listOf()

    val providerMap: Map<String, RequestContextProvider<*>> by lazy {
        providers.associateBy { it.key.toString() }
    }

    override fun <ReqT : Any, RespT : Any> interceptCall(method: MethodDescriptor<ReqT, RespT>, callOptions: CallOptions, next: Channel): ClientCall<ReqT, RespT> {
        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            override fun start(responseListener: Listener<RespT>, headers: Metadata) {
                super.start(object : ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    override fun onClose(status: Status, trailers: Metadata) {
                        for (key in trailers.keys()) {
                            val provider = providerMap[key] ?: continue
                            val contextKey = provider.key as? MutableRequestContext<Any> ?: continue
                            val value = trailers.get(provider.metaKey) ?: continue
                            contextKey.set(value)
                        }
                        super.onClose(status, trailers)
                    }
                }, buildHeaders(headers))
            }
        }
    }

    fun buildHeaders(headers: Metadata): Metadata {
        // 将所有!=null的信息传递到下一个服务
        for (provider in providers) {
            val param = provider.key.get() ?: continue
            headers.put(provider.metaKey, param.uncheckedCast())
        }
        return headers
    }
}
