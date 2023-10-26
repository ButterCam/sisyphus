package com.bybutter.sisyphus.rpc

import com.salesforce.rxgrpc.stub.ClientCalls
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.MethodDescriptor
import io.grpc.stub.AbstractStub
import io.reactivex.Flowable
import io.reactivex.Single

abstract class AbstractReactiveStub<T : AbstractReactiveStub<T>>(
    channel: Channel,
    optionsInterceptors: Iterable<CallOptionsInterceptor>,
    options: CallOptions,
) : AbstractStub<T>(channel, options) {
    private val _optionsInterceptors: MutableList<CallOptionsInterceptor> = optionsInterceptors.toMutableList()

    val optionsInterceptors: List<CallOptionsInterceptor> get() = _optionsInterceptors

    fun withOptionsInterceptor(interceptor: CallOptionsInterceptor): T {
        return build(channel, optionsInterceptors + interceptor, callOptions)
    }

    fun withOptionsInterceptors(interceptors: Iterable<CallOptionsInterceptor>): T {
        return build(channel, optionsInterceptors + interceptors, callOptions)
    }

    override fun build(
        channel: Channel,
        callOptions: CallOptions,
    ): T {
        return build(channel, optionsInterceptors, callOptions)
    }

    abstract fun build(
        channel: Channel,
        optionsInterceptors: Iterable<CallOptionsInterceptor>,
        callOptions: CallOptions,
    ): T

    protected fun buildOption(method: MethodDescriptor<*, *>): CallOptions {
        return optionsInterceptors.fold(callOptions) { options, interceptor ->
            interceptor.intercept(method, options)
        }
    }

    protected fun <TReq, TRes> unaryCall(
        method: MethodDescriptor<TReq, TRes>,
        input: TReq,
    ): Single<TRes> {
        val options = buildOption(method)
        return ClientCalls.oneToOne<TReq, TRes>(
            Single.just(input),
            { req, res ->
                io.grpc.stub.ClientCalls.asyncUnaryCall(
                    channel.newCall(method, options),
                    req,
                    res,
                )
            },
            options,
        )
    }

    protected fun <TReq, TRes> serverStreaming(
        method: MethodDescriptor<TReq, TRes>,
        input: TReq,
    ): Flowable<TRes> {
        val options = buildOption(method)
        return ClientCalls.oneToMany<TReq, TRes>(
            Single.just(input),
            { req, res ->
                io.grpc.stub.ClientCalls.asyncServerStreamingCall(
                    channel.newCall(method, options),
                    req,
                    res,
                )
            },
            options,
        )
    }

    protected fun <TReq, TRes> clientStreaming(
        method: MethodDescriptor<TReq, TRes>,
        input: Flowable<TReq>,
    ): Single<TRes> {
        val options = buildOption(method)
        return ClientCalls.manyToOne(
            input,
            {
                io.grpc.stub.ClientCalls.asyncClientStreamingCall(
                    channel.newCall(method, options),
                    it,
                )
            },
            options,
        )
    }

    protected fun <TReq, TRes> bidiStreaming(
        method: MethodDescriptor<TReq, TRes>,
        input: Flowable<TReq>,
    ): Flowable<TRes> {
        val options = buildOption(method)
        return ClientCalls.manyToMany(
            input,
            {
                io.grpc.stub.ClientCalls.asyncClientStreamingCall(
                    channel.newCall(method, options),
                    it,
                )
            },
            options,
        )
    }
}
