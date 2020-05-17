package com.bybutter.sisyphus.rpc

import io.grpc.ServerCallHandler
import io.grpc.stub.ServerCalls
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

object ServerCallHandlers {
    fun <TRequest, TResponse> asyncUnaryCall(block: suspend (TRequest) -> TResponse): ServerCallHandler<TRequest, TResponse> {
        return ServerCalls.asyncUnaryCall { request, responseObserver ->
            GlobalScope.launch(GrpcContextCoroutineContextElement()) {
                try {
                    responseObserver.onNext(block(request))
                    responseObserver.onCompleted()
                } catch (e: Throwable) {
                    responseObserver.onError(e)
                }
            }
        }
    }

    fun <TRequest, TResponse> asyncClientStreamingCall(block: suspend (ReceiveChannel<TRequest>) -> TResponse): ServerCallHandler<TRequest, TResponse> {
        return ServerCalls.asyncClientStreamingCall {
            val request = Channel<TRequest>(Channel.UNLIMITED)
            GlobalScope.launch(GrpcContextCoroutineContextElement()) {
                try {
                    it.onNext(block(request))
                    it.onCompleted()
                } catch (e: Throwable) {
                    it.onError(e)
                }
            }
            request.asStreamObserver()
        }
    }

    fun <TRequest, TResponse> asyncServerStreamingCall(block: suspend (TRequest) -> ReceiveChannel<TResponse>): ServerCallHandler<TRequest, TResponse> {
        return ServerCalls.asyncServerStreamingCall { request, responseObserver ->
            GlobalScope.launch(GrpcContextCoroutineContextElement()) {
                try {
                    for (item in block(request)) {
                        responseObserver.onNext(item)
                    }
                    responseObserver.onCompleted()
                } catch (e: Throwable) {
                    responseObserver.onError(e)
                }
            }
        }
    }

    fun <TRequest, TResponse> asyncBidiStreamingCall(block: suspend (ReceiveChannel<TRequest>) -> ReceiveChannel<TResponse>): ServerCallHandler<TRequest, TResponse> {
        return ServerCalls.asyncBidiStreamingCall {
            val request = Channel<TRequest>(Channel.UNLIMITED)
            GlobalScope.launch(GrpcContextCoroutineContextElement()) {
                try {
                    for (item in block(request)) {
                        it.onNext(item)
                    }
                    it.onCompleted()
                } catch (e: Throwable) {
                    it.onError(e)
                }
            }
            request.asStreamObserver()
        }
    }
}
