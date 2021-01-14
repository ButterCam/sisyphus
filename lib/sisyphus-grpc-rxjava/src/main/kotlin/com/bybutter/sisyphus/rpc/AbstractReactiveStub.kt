package com.bybutter.sisyphus.rpc

import com.salesforce.rxgrpc.stub.ClientCalls
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.Status
import io.grpc.stub.AbstractStub
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single

abstract class AbstractReactiveStub<T : AbstractReactiveStub<T>>(
    channel: Channel,
    optionsInterceptors: Iterable<CallOptionsInterceptor>,
    options: CallOptions
) : AbstractStub<T>(channel, options) {

    private val _optionsInterceptors: MutableList<CallOptionsInterceptor> = optionsInterceptors.toMutableList()

    val optionsInterceptors: List<CallOptionsInterceptor> get() = _optionsInterceptors

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

    protected fun <TReq, TRes> unaryCall(method: MethodDescriptor<TReq, TRes>, input: TReq, metadata: Metadata): Single<TRes> {
        val options = buildOption(method)
        val call = channel.newCall(method, options)

        return Single.create { emitter ->
            emitter.setCancellable {
                call.cancel("Client cancelled", ClientStatusException(Status.CANCELLED, Metadata()))
            }

            call.start(object : ClientCall.Listener<TRes>() {
                override fun onMessage(message: TRes) {
                    emitter.onSuccess(message)
                }

                override fun onClose(status: Status, trailers: Metadata) {
                    if (!status.isOk) {
                        emitter.onError(ClientStatusException(status, trailers))
                    }
                }
            }, metadata)

            try {
                call.sendMessage(input)
                call.halfClose()
                call.request(2)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    protected fun <TReq, TRes> serverStreaming(method: MethodDescriptor<TReq, TRes>, input: TReq, metadata: Metadata): Flowable<TRes> {
        val options = buildOption(method)
        val call = channel.newCall(method, options)

        return Flowable.create({ emitter ->
            emitter.setCancellable {
                call.cancel("Client cancelled", ClientStatusException(Status.CANCELLED, Metadata()))
            }

            call.start(object : ClientCall.Listener<TRes>() {
                override fun onMessage(message: TRes) {
                    emitter.onNext(message)
                    call.request(1)
                }

                override fun onClose(status: Status, trailers: Metadata) {
                    if (!status.isOk) {
                        emitter.onError(ClientStatusException(status, trailers))
                    }
                }
            }, metadata)

            io.grpc.stub.ClientCalls.asyncServerStreamingCall()

            try {
                call.sendMessage(input)
                call.halfClose()
                call.request(10000)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }, BackpressureStrategy.BUFFER)
    }

    protected fun <TReq, TRes> clientStreaming(method: MethodDescriptor<TReq, TRes>, input: Flowable<TReq>, metadata: Metadata): Single<TRes> {
        TODO()
    }

    protected fun <TReq, TRes> bidiStreaming(method: MethodDescriptor<TReq, TRes>, input: Flowable<TReq>, metadata: Metadata): Flowable<TRes> {
        TODO()
    }
}