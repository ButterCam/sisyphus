package com.bybutter.sisyphus.rpc

import com.bybutter.sisyphus.protobuf.ServiceSupport
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.kotlin.ClientCalls
import kotlinx.coroutines.flow.Flow

abstract class AbstractCoroutineStub<T : AbstractCoroutineStub<T>>(
    channel: Channel,
    optionsInterceptors: Iterable<CallOptionsInterceptor>,
    options: CallOptions
) : io.grpc.kotlin.AbstractCoroutineStub<T>(channel, options) {

    private val _optionsInterceptors: MutableList<CallOptionsInterceptor> = optionsInterceptors.toMutableList()

    val optionsInterceptors: List<CallOptionsInterceptor> get() = _optionsInterceptors

    abstract fun support(): ServiceSupport

    fun withOptionsInterceptor(interceptor: CallOptionsInterceptor): T {
        return build(channel, optionsInterceptors + interceptor, callOptions)
    }

    fun withOptionsInterceptors(interceptors: Iterable<CallOptionsInterceptor>): T {
        return build(channel, optionsInterceptors + interceptors, callOptions)
    }

    override fun build(channel: Channel, callOptions: CallOptions): T {
        return build(channel, optionsInterceptors, callOptions)
    }

    abstract fun build(channel: Channel, optionsInterceptors: Iterable<CallOptionsInterceptor>, callOptions: CallOptions): T

    protected fun buildOption(method: MethodDescriptor<*, *>): CallOptions {
        return optionsInterceptors.fold(callOptions) { options, interceptor ->
            interceptor.intercept(method, options)
        }
    }

    protected suspend fun <TReq, TRes> unaryCall(method: MethodDescriptor<TReq, TRes>, input: TReq, metadata: Metadata): TRes {
        return ClientCalls.unaryRpc(channel, method, input, buildOption(method), metadata)
    }

    protected fun <TReq, TRes> serverStreaming(method: MethodDescriptor<TReq, TRes>, input: TReq, metadata: Metadata): Flow<TRes> {
        return ClientCalls.serverStreamingRpc(channel, method, input, buildOption(method), metadata)
    }

    protected suspend fun <TReq, TRes> clientStreaming(method: MethodDescriptor<TReq, TRes>, input: Flow<TReq>, metadata: Metadata): TRes {
        return ClientCalls.clientStreamingRpc(channel, method, input, buildOption(method), metadata)
    }

    protected fun <TReq, TRes> bidiStreaming(method: MethodDescriptor<TReq, TRes>, input: Flow<TReq>, metadata: Metadata): Flow<TRes> {
        return ClientCalls.bidiStreamingRpc(channel, method, input, buildOption(method), metadata)
    }
}
