package com.bybutter.sisyphus.starter.grpc.transcoding

import com.bybutter.sisyphus.rpc.Status
import com.bybutter.sisyphus.starter.grpc.transcoding.util.toHttpStatus
import com.bybutter.sisyphus.starter.webflux.DetectBodyInserter
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

/**
 * Convert all exception in HTTP server and not in gRPC server to gRPC-style error message.
 */
@Component
@Suppress("LeakingThis")
class TranscodingErrorWebExceptionHandler(
    errorAttributes: ErrorAttributes,
    properties: WebProperties,
    applicationContext: ApplicationContext,
    serverCodecConfigurer: ServerCodecConfigurer
) : AbstractErrorWebExceptionHandler(errorAttributes, properties.resources, applicationContext) {

    init {
        // Safe for use `this` in init function, because the handler is a [Component] and just spring need it be open.
        setMessageReaders(serverCodecConfigurer.readers)
        setMessageWriters(serverCodecConfigurer.writers)
    }

    override fun getRoutingFunction(errorAttributes: ErrorAttributes): RouterFunction<ServerResponse> {
        return router {
            RequestPredicates.all().invoke {
                val error = errorAttributes.getErrorAttributes(
                    it,
                    ErrorAttributeOptions.of(ErrorAttributeOptions.Include.STACK_TRACE)
                )
                val status = error[TranscodingErrorAttributes.GRPC_STATUS_ATTRIBUTE] as? Status
                    ?: throw IllegalStateException("Missing gRPC status in error attributes.")

                ServerResponse.status(status.toHttpStatus())
                    .body(DetectBodyInserter(status))
            }
        }
    }

    override fun isTraceEnabled(request: ServerRequest): Boolean {
        return false
    }
}
