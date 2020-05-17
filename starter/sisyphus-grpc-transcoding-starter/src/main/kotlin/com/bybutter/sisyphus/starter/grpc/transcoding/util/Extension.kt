package com.bybutter.sisyphus.starter.grpc.transcoding.util

import com.bybutter.sisyphus.rpc.Code
import io.grpc.Status
import org.springframework.http.HttpStatus

fun Status.Code.toHttpStatus(): Int {
    return com.bybutter.sisyphus.rpc.Status.toHttpStatus(this.value())
}

fun com.bybutter.sisyphus.rpc.Status.toHttpStatus(): Int {
    return com.bybutter.sisyphus.rpc.Status.toHttpStatus(this.code)
}

fun com.bybutter.sisyphus.rpc.Status.Companion.toHttpStatus(code: Int): Int {
    return when (code) {
        Status.Code.OK.value() -> HttpStatus.OK.value()
        Status.Code.CANCELLED.value() -> 499
        Status.Code.UNKNOWN.value() -> HttpStatus.INTERNAL_SERVER_ERROR.value()
        Status.Code.INVALID_ARGUMENT.value() -> HttpStatus.BAD_REQUEST.value()
        Status.Code.DEADLINE_EXCEEDED.value() -> HttpStatus.GATEWAY_TIMEOUT.value()
        Status.Code.NOT_FOUND.value() -> HttpStatus.NOT_FOUND.value()
        Status.Code.ALREADY_EXISTS.value() -> HttpStatus.CONFLICT.value()
        Status.Code.PERMISSION_DENIED.value() -> HttpStatus.FORBIDDEN.value()
        Status.Code.RESOURCE_EXHAUSTED.value() -> HttpStatus.TOO_MANY_REQUESTS.value()
        Status.Code.FAILED_PRECONDITION.value() -> HttpStatus.BAD_REQUEST.value()
        Status.Code.ABORTED.value() -> HttpStatus.CONFLICT.value()
        Status.Code.OUT_OF_RANGE.value() -> HttpStatus.BAD_REQUEST.value()
        Status.Code.UNIMPLEMENTED.value() -> HttpStatus.NOT_IMPLEMENTED.value()
        Status.Code.INTERNAL.value() -> HttpStatus.INTERNAL_SERVER_ERROR.value()
        Status.Code.UNAVAILABLE.value() -> HttpStatus.SERVICE_UNAVAILABLE.value()
        Status.Code.DATA_LOSS.value() -> HttpStatus.INTERNAL_SERVER_ERROR.value()
        Status.Code.UNAUTHENTICATED.value() -> HttpStatus.UNAUTHORIZED.value()
        else -> HttpStatus.INTERNAL_SERVER_ERROR.value()
    }
}
