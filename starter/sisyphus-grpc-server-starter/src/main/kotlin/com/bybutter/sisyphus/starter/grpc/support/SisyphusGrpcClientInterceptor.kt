package com.bybutter.sisyphus.starter.grpc.support

import com.bybutter.sisyphus.rpc.ClientStatusException
import com.bybutter.sisyphus.rpc.Debug
import com.bybutter.sisyphus.rpc.trailers
import com.bybutter.sisyphus.starter.grpc.support.SisyphusGrpcServerInterceptor.Companion.DEBUG_META_KEY
import com.bybutter.sisyphus.starter.grpc.support.SisyphusGrpcServerInterceptor.Companion.REQUEST_ID_META_KEY
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.ForwardingClientCallListener
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.Status
import org.springframework.stereotype.Component

@Component
class SisyphusGrpcClientInterceptor : ClientInterceptor {
    override fun <ReqT : Any, RespT : Any> interceptCall(method: MethodDescriptor<ReqT, RespT>, callOptions: CallOptions, next: Channel): ClientCall<ReqT, RespT> {
        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            override fun start(responseListener: Listener<RespT>, headers: Metadata) {
                trailers(REQUEST_ID_META_KEY)?.let {
                    headers.put(REQUEST_ID_META_KEY, it)
                }

                if (Debug.debugEnabled) {
                    headers.put(DEBUG_META_KEY, "sisyphus")
                }

                super.start(object : ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    override fun onClose(status: Status, trailers: Metadata) {
                        if (status.isOk) {
                            super.onClose(status, trailers)
                        } else {
                            super.onClose(status.withCause(ClientStatusException(status, trailers)), trailers)
                        }
                    }
                }, headers)
            }
        }
    }
}
