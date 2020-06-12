package com.bybutter.sisyphus.starter.grpc.support

import com.bybutter.sisyphus.starter.grpc.MicrometerRegistrar
import io.grpc.Context
import io.grpc.Contexts
import io.grpc.ForwardingServerCall
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Duration
import kotlin.concurrent.thread

@Component
class ServerMicrometerInterceptor : ServerInterceptor {
    @Autowired
    private lateinit var micrometerRegistrar: MicrometerRegistrar

    override fun <ReqT : Any, RespT : Any> interceptCall(call: ServerCall<ReqT, RespT>, headers: Metadata, next: ServerCallHandler<ReqT, RespT>): ServerCall.Listener<ReqT> {
        val host = headers.get(Metadata.Key.of("host", Metadata.ASCII_STRING_MARSHALLER))?.toString()
        return Contexts.interceptCall(Context.current(), ServerMicrometerCall(call, micrometerRegistrar, host), headers, next)
    }
}

class ServerMicrometerCall<ReqT : Any, RespT : Any>(call: ServerCall<ReqT, RespT>, private val micrometerRegistrar: MicrometerRegistrar, private val host: String?) : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
    override fun close(status: Status, trailers: Metadata) {
        val cost = System.nanoTime() - REQUEST_TIMESTAMP_CONTEXT_KEY.get()
        thread() {
            val costDuration = Duration.ofNanos(cost)
            micrometerRegistrar.incrAllRequest(costDuration)
            if (host != null && !host.startsWith("localhost")) {
                micrometerRegistrar.incrRemoteRequest(costDuration)
            }
        }
        super.close(status, trailers)
    }
}
