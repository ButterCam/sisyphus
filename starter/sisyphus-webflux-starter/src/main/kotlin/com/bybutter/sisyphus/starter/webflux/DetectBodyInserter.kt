package com.bybutter.sisyphus.starter.webflux

import com.bybutter.sisyphus.reflect.uncheckedCast
import org.springframework.core.ResolvableType
import org.springframework.http.MediaType
import org.springframework.http.ReactiveHttpOutputMessage
import org.springframework.http.codec.HttpMessageWriter
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.UnsupportedMediaTypeException
import reactor.core.publisher.Mono

/**
 * [BodyInserter] based on `accept` request headers. It convert response body to supported format, if no `accept` header
 * provided or all formats not be supported, response body will convert to json format.
 * @param T the type of body
 * @property value T the response body
 */
class DetectBodyInserter<T>(private val value: T) : BodyInserter<T, ReactiveHttpOutputMessage> {
    private var bodyWriter: HttpMessageWriter<*>? = null
    private var contentType: MediaType? = null

    override fun insert(outputMessage: ReactiveHttpOutputMessage, context: BodyInserter.Context): Mono<Void> {
        // Get all writers from context.
        val writers = context.messageWriters()
        // Get the resolvable type of body.
        val bodyType = ResolvableType.forInstance(value)
        // Get the set body mime type from `content-type` response header, it is high priority to handle.
        contentType = outputMessage.headers.contentType

        // If `content-type` response header not set, we need choose one from `accept` request header.
        if (contentType == null) {
            // List of accepted types.
            val acceptTypes = context.serverRequest()
                .orElse(null)?.headers?.accept ?: listOf()

            type@ for (acceptType in acceptTypes) {
                // Skip for wildcard types.
                if (acceptType.isWildcardType || acceptType.isWildcardSubtype) {
                    continue
                }

                // If any write can write the body with accept type, choose and save it to [bodyWrite].
                for (writer in writers) {
                    if (writer.canWrite(bodyType, acceptType)) {
                        bodyWriter = writer
                        contentType = acceptType
                        break@type
                    }
                }
            }
        }

        // If we can not confirm the mime type to write, we will fallback to json.
        contentType = contentType ?: MediaType.APPLICATION_JSON

        // If the [bodyWrite] not be set, choose a writer which can write it.
        if (bodyWriter == null) {
            for (writer in writers) {
                if (writer.canWrite(bodyType, contentType)) {
                    bodyWriter = writer
                    break
                }
            }
        }

        // Ensure [bodyWrite] is not null, throw a [UnsupportedMediaTypeException] if we can't resolve it.
        val bodyWriter = bodyWriter?.uncheckedCast<HttpMessageWriter<T>>()
            ?: throw UnsupportedMediaTypeException(
                contentType,
                context.messageWriters().flatMap { it.writableMediaTypes },
                bodyType
            )

        // Get http request or null. Copy from the default body inserter of spring.
        // I don't known why to do it, it seems the [BodyWriter] can write it with more feature when provided http request.
        val request = context.serverRequest().orElse(null)

        // Set the final context mime type.
        outputMessage.headers.contentType = contentType
        return if (request == null) {
            bodyWriter.write(Mono.just(value.uncheckedCast()), bodyType, contentType, outputMessage, context.hints())
        } else {
            bodyWriter.write(
                Mono.just(value.uncheckedCast()),
                bodyType,
                bodyType,
                contentType,
                request,
                outputMessage as ServerHttpResponse,
                context.hints()
            )
        }
    }
}
