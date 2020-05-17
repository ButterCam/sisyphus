package com.bybutter.sisyphus.rpc

import io.grpc.ClientCall
import io.grpc.ForwardingClientCall
import io.grpc.ForwardingClientCallListener
import io.grpc.Metadata
import io.grpc.Status

fun <ReqT, RespT> ClientCall<ReqT, RespT>.withHeader(headers: Metadata): ClientCall<ReqT, RespT> {
    return ClientCallWithHeader(this, headers)
}

fun <ReqT, RespT> ClientCall<ReqT, RespT>.withListener(listener: ClientCall.Listener<RespT>): ClientCall<ReqT, RespT> {
    return ClientCallWithListener(this, listener)
}

operator fun <RespT> ClientCall.Listener<RespT>.plus(other: ClientCall.Listener<RespT>): ClientCall.Listener<RespT> {
    return ClientCallMergedListener(this, other)
}

private class ClientCallWithHeader<ReqT, RespT>(delegate: ClientCall<ReqT, RespT>, private val metadata: Metadata) : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(delegate) {
    override fun start(responseListener: Listener<RespT>, headers: Metadata) {
        headers.merge(metadata)
        super.start(responseListener, headers)
    }
}

private class ClientCallWithListener<ReqT, RespT>(delegate: ClientCall<ReqT, RespT>, private val listener: Listener<RespT>) : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(delegate) {
    override fun start(responseListener: Listener<RespT>, headers: Metadata) {
        super.start(listener + responseListener, headers)
    }
}

private class ClientCallMergedListener<RespT>(delegate: ClientCall.Listener<RespT>, private val delegate2: ClientCall.Listener<RespT>) : ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(delegate) {
    override fun onHeaders(headers: Metadata) {
        super.onHeaders(headers)
        delegate2.onHeaders(headers)
    }

    override fun onClose(status: Status, trailers: Metadata) {
        super.onClose(status, trailers)
        delegate2.onClose(status, trailers)
    }

    override fun onMessage(message: RespT) {
        super.onMessage(message)
        delegate2.onMessage(message)
    }

    override fun onReady() {
        super.onReady()
        delegate2.onReady()
    }
}
