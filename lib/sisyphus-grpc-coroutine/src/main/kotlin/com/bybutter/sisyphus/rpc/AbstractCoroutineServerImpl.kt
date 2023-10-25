package com.bybutter.sisyphus.rpc

import com.bybutter.sisyphus.protobuf.ServiceSupport
import io.grpc.kotlin.AbstractCoroutineServerImpl
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

abstract class AbstractCoroutineServerImpl(
    context: CoroutineContext = EmptyCoroutineContext,
) : AbstractCoroutineServerImpl(context) {
    abstract fun support(): ServiceSupport
}
