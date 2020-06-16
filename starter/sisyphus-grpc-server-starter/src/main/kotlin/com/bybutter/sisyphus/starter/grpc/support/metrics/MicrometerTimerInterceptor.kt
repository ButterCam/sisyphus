package com.bybutter.sisyphus.starter.grpc.support.metrics

import com.bybutter.sisyphus.starter.grpc.support.REQUEST_TIMESTAMP_CONTEXT_KEY
import io.grpc.Context
import io.grpc.Contexts
import io.grpc.ForwardingServerCall
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import io.micrometer.core.instrument.MeterRegistry
import java.time.Duration

/**
 * ServerInterceptor for micrometer timers, it will create two times for sisyphus,
 * 'sisyphus_grpc_requests' will statistics all requests,
 * 'sisyphus_grpc_incoming_requests' will statistics all incoming requests which has 'host' header and not be 'localhost'.
 */
class MicrometerTimerInterceptor(private val registry: MeterRegistry) : ServerInterceptor {
    override fun <ReqT : Any, RespT : Any> interceptCall(call: ServerCall<ReqT, RespT>, headers: Metadata, next: ServerCallHandler<ReqT, RespT>): ServerCall.Listener<ReqT> {
        val host = headers.get(Metadata.Key.of("host", Metadata.ASCII_STRING_MARSHALLER))?.toString()
        return Contexts.interceptCall(Context.current(), ServerMicrometerCall(call, registry, host), headers, next)
    }

    private class ServerMicrometerCall<ReqT : Any, RespT : Any>(call: ServerCall<ReqT, RespT>, private val registry: MeterRegistry, private val host: String?) : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
        override fun close(status: Status, trailers: Metadata) {
            val costDuration = Duration.ofNanos(System.nanoTime() - REQUEST_TIMESTAMP_CONTEXT_KEY.get())

            registry.timer("sisyphus_grpc_requests",
                    "service", delegate().methodDescriptor.serviceName,
                    "method", delegate().methodDescriptor.fullMethodName,
                    "status", status.code.name,
                    "exception", status.cause?.javaClass?.name ?: "None"
            ).record(costDuration)

            if (host != null && !host.startsWith("localhost")) {
                registry.timer("sisyphus_grpc_incoming_requests",
                        "service", delegate().methodDescriptor.serviceName,
                        "method", delegate().methodDescriptor.fullMethodName,
                        "status", status.code.name,
                        "exception", status.cause?.javaClass?.name ?: "None"
                ).record(costDuration)
            }

            super.close(status, trailers)
        }
    }
}
