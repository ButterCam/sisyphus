package com.bybutter.sisyphus.middleware.sentinel.interceptor

import com.alibaba.csp.sentinel.Entry
import com.alibaba.csp.sentinel.EntryType
import com.alibaba.csp.sentinel.SphU
import com.alibaba.csp.sentinel.Tracer
import com.alibaba.csp.sentinel.adapter.grpc.SentinelGrpcServerInterceptor
import com.alibaba.csp.sentinel.slots.block.BlockException
import io.grpc.ForwardingServerCall
import io.grpc.ForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.Status
import io.grpc.StatusRuntimeException
import java.util.concurrent.atomic.AtomicReference

class SisyphusSentinelGrpcServerInterceptor(fallbackMessage: String) : SentinelGrpcServerInterceptor() {
    private val status = Status.UNAVAILABLE.withDescription(
            fallbackMessage)

    private val statusRuntimeException = StatusRuntimeException(Status.CANCELLED)

    override fun <ReqT : Any, RespT : Any> interceptCall(call: ServerCall<ReqT, RespT>, headers: Metadata, next: ServerCallHandler<ReqT, RespT>): ServerCall.Listener<ReqT> {
        val fullMethodName = call.methodDescriptor.fullMethodName
        // Remote address: serverCall.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        var entry: Entry? = null
        return try {
            entry = SphU.asyncEntry(fullMethodName, EntryType.IN)
            val atomicReferenceEntry = AtomicReference<Entry?>(entry)
            // Allow access, forward the call.
            object : ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                    next.startCall(
                            object : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
                                override fun close(status: Status, trailers: Metadata) {
                                    val entry = atomicReferenceEntry.get()
                                    if (entry != null) {
                                        // Record the exception metrics.
                                        if (!status.isOk) {
                                            Tracer.traceEntry(status.asRuntimeException(), entry)
                                        }
                                        // entry exit when the call be closed
                                        entry.exit()
                                    }
                                    super.close(status, trailers)
                                }
                            }, headers)) {
                /**
                 * If call was canceled, onCancel will be called. and the close will not be called
                 * so the server is encouraged to abort processing to save resources by onCancel
                 * @see ServerCall.Listener.onCancel
                 */
                override fun onCancel() {
                    val entry = atomicReferenceEntry.get()
                    if (entry != null) {
                        Tracer.traceEntry(statusRuntimeException, entry)
                        entry.exit()
                        atomicReferenceEntry.set(null)
                    }
                    super.onCancel()
                }
            }
        } catch (e: BlockException) {
            call.close(status, Metadata())
            object : ServerCall.Listener<ReqT>() {}
        } catch (e: RuntimeException) {
            // Catch the RuntimeException startCall throws, entry is guaranteed to exit.
            if (entry != null) {
                Tracer.traceEntry(e, entry)
                entry.exit()
            }
            throw e
        }
    }
}
