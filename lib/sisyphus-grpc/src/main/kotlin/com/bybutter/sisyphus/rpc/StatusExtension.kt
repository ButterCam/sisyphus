package com.bybutter.sisyphus.rpc

import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.invoke
import io.grpc.Metadata
import io.grpc.StatusException
import io.grpc.StatusRuntimeException

val STATUS_META_KEY: Metadata.Key<Status> = Metadata.Key.of("grpc-status-details-bin", Status)

fun Status.Companion.fromThrowable(e: Throwable): Status {
    return when (e) {
        is StatusException -> {
            e.trailers?.get(STATUS_META_KEY) ?: Status {
                code = e.status.code.value()
                message = e.status.description ?: ""
                extractStatusDetails(details, e)
            }
        }
        is StatusRuntimeException -> {
            e.trailers?.get(STATUS_META_KEY) ?: Status {
                code = e.status.code.value()
                message = e.status.description ?: ""
                extractStatusDetails(details, e)
            }
        }
        is com.bybutter.sisyphus.rpc.StatusException -> e.status {
            extractStatusDetails(details, e)
        }
        else -> Status {
            code = io.grpc.Status.Code.INTERNAL.value()
            message = e.message ?: ""
            extractStatusDetails(details, e)
        }
    }
}

fun Status.Companion.fromGrpcStatus(status: io.grpc.Status): Status {
    return Status {
        val cause = status.cause
        when (cause) {
            is com.bybutter.sisyphus.rpc.StatusException -> {
                code = cause.status.code
                message = cause.status.message
                details += cause.status.details
            }
            null -> {
                code = status.code.value()
                message = status.description ?: "Uncaught unknown internal exception occurred."
            }
            else -> {
                code = status.code.value()
                message = cause.message ?: "Uncaught '${cause.javaClass.canonicalName}' exception occurred."
            }
        }
        extractStatusDetails(details, status.cause)
    }
}

fun Status.toGrpcStatus(cause: Throwable? = null): io.grpc.Status {
    return io.grpc.Status.fromCodeValue(this.code).withDescription(this.message).withCause(cause)
}

private fun extractStatusDetails(details: MutableList<Message<*, *>>, throwable: Throwable? = null) {
    details += debugInfo
    throwable?.extractStatusDetails(details)
}

private fun Throwable.extractStatusDetails(list: MutableList<Message<*, *>>) {
    if (debugEnabled) {
        list += DebugInfo {
            detail = "${this@extractStatusDetails.javaClass}(${this@extractStatusDetails.message})"
            stackEntries += this@extractStatusDetails.stackTrace.map { it.toString() }
        }
    }

    cause?.extractStatusDetails(list)
}
