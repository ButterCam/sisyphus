package com.bybutter.sisyphus.rpc

import io.grpc.stub.StreamObserver
import kotlinx.coroutines.channels.ReceiveChannel

class ManyToManyCall<in TRequest, out TResponse>(
    private val request: StreamObserver<TRequest>,
    private val response: ReceiveChannel<TResponse>
) : Sender<TRequest> by request.asSender(),
        ReceiveChannel<TResponse> by response
