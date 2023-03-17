package com.bybutter.sisyphus.starter.grpc.support.metrics

import com.bybutter.sisyphus.starter.grpc.support.IncomingRequestLogger
import com.bybutter.sisyphus.starter.grpc.support.RequestInfo
import com.bybutter.sisyphus.starter.grpc.support.RequestLogger
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.Status
import io.micrometer.core.instrument.MeterRegistry
import java.time.Duration

/**
 * RequestLogger for micrometer timers, it will create a timer and a counter for sisyphus,
 * 'sisyphus_grpc_requests' will statistics all requests,
 * 'sisyphus_incoming_grpc_requests' will count all incoming requests even it not be process over.
 */
class MicrometerRequestLogger(private val registry: MeterRegistry) : IncomingRequestLogger {
    override val id: String = MicrometerRequestLogger::class.java.typeName

    override fun log(call: ServerCall<*, *>, requestInfo: RequestInfo, status: Status, cost: Long) {
        val host = requestInfo.inputHeader.get(HOST_METADATA_KEY)?.toString()
        val costDuration = Duration.ofNanos(cost)

        registry.timer(
            "sisyphus_grpc_requests",
            "service", call.methodDescriptor.serviceName,
            "method", call.methodDescriptor.fullMethodName,
            "status", status.code.name,
            "exception", status.cause?.javaClass?.name ?: "None"
        ).record(costDuration)
    }

    override fun log(call: ServerCall<*, *>, inputHeader: Metadata) {
        registry.counter(
            "sisyphus_incoming_grpc_requests",
            "service",
            call.methodDescriptor.serviceName,
            "method",
            call.methodDescriptor.fullMethodName
        )
    }

    companion object {
        val HOST_METADATA_KEY = Metadata.Key.of("host", Metadata.ASCII_STRING_MARSHALLER)
    }
}
