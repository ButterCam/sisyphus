package com.bybutter.sisyphus.rpc

import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Deferred

class ManyToOneCall<in TRequest, out TResponse>(
    private val request: StreamObserver<TRequest>,
    private val response: Deferred<TResponse>
) : Sender<TRequest> by request.asSender(),
        Deferred<TResponse> by response
