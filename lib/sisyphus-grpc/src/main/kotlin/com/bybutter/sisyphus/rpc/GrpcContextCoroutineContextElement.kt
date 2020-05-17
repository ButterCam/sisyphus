package com.bybutter.sisyphus.rpc

import io.grpc.Context
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.ThreadContextElement

class GrpcContextCoroutineContextElement : ThreadContextElement<Context> {
    companion object Key : CoroutineContext.Key<GrpcContextCoroutineContextElement>

    private val grpcContext: Context = Context.current()

    override val key: CoroutineContext.Key<GrpcContextCoroutineContextElement>
        get() = Key

    override fun updateThreadContext(context: CoroutineContext): Context =
            grpcContext.attach()

    override fun restoreThreadContext(context: CoroutineContext, oldState: Context) =
            grpcContext.detach(oldState)
}
