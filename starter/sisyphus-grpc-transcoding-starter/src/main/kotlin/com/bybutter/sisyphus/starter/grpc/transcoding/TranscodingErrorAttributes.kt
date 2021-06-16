package com.bybutter.sisyphus.starter.grpc.transcoding

import com.bybutter.sisyphus.rpc.Code
import com.bybutter.sisyphus.rpc.Status
import com.bybutter.sisyphus.rpc.invoke
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.attributeOrNull
import org.springframework.web.server.ServerWebExchange

/**
 * Export error attributes from gRPC exceptions.
 */
@Component
class TranscodingErrorAttributes : ErrorAttributes {
    companion object {
        val ERROR_ATTRIBUTE = "${TranscodingErrorAttributes::class.java.name}.ERROR"
        val GRPC_STATUS_ATTRIBUTE = "grpcStatus"
    }

    override fun storeErrorInformation(error: Throwable, exchange: ServerWebExchange) {
        exchange.attributes[ERROR_ATTRIBUTE] = error
    }

    override fun getError(request: ServerRequest): Throwable {
        return request.attributeOrNull(ERROR_ATTRIBUTE) as? Throwable
            ?: throw IllegalStateException("Missing exception attribute in ServerWebExchange")
    }

    override fun getErrorAttributes(request: ServerRequest, options: ErrorAttributeOptions?): MutableMap<String, Any> {
        val error = getError(request)
        val status = if (error is TranscodingNotSupportException) {
            Status {
                code = Code.NOT_FOUND.number
                message = "No handlers matched for '${request.methodName()} ${request.path()}'"
            }
        } else {
            Status(error)
        }

        return mutableMapOf(
            // Store gRPC status
            GRPC_STATUS_ATTRIBUTE to status
        )
    }
}
