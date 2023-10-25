package com.bybutter.sisyphus.rpc

import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.primitives.Duration
import io.grpc.Metadata
import io.grpc.Status
import java.util.concurrent.TimeUnit

open class StatusException : RuntimeException {
    private var _code = 0

    private val _details = mutableListOf<Message<*, *>>()

    val code: Int get() = _code

    val details: List<Message<*, *>> get() = _details

    val trailers: Metadata = Metadata()

    constructor(status: Status) : this(status.code, status.description, status.cause)

    constructor(code: Code, cause: Throwable) : this(code, cause.message, cause)

    constructor(code: Code, message: String? = null, cause: Throwable? = null) : super(message ?: code.name, cause) {
        this._code = code.number
    }

    constructor(code: Status.Code, cause: Throwable) : this(code, cause.message, cause)

    constructor(code: Status.Code, message: String? = null, cause: Throwable? = null) : super(
        message ?: code.name,
        cause,
    ) {
        this._code = code.value()
    }

    fun withLocalizedMessage(
        locale: String,
        message: String,
    ): StatusException {
        withDetails(LocalizedMessage(locale, message))
        return this
    }

    fun withLocalizedMessage(message: String): StatusException {
        withDetails(LocalizedMessage(message))
        return this
    }

    fun withHelps(vararg links: Help.Link): StatusException {
        withDetails(Help(*links))
        return this
    }

    fun withResourceInfo(
        resourceType: String,
        resourceName: String,
        description: String,
        owner: String = "",
    ): StatusException {
        withDetails(ResourceInfo(resourceType, resourceName, description, owner))
        return this
    }

    fun withRequestInfo(
        requestId: String,
        servingData: String = "",
    ): StatusException {
        withDetails(RequestInfo(requestId, servingData))
        return this
    }

    fun withBadRequest(vararg violations: BadRequest.FieldViolation): StatusException {
        withDetails(BadRequest(*violations))
        return this
    }

    fun withPreconditionFailure(vararg violations: PreconditionFailure.Violation): StatusException {
        withDetails(PreconditionFailure(*violations))
        return this
    }

    fun withQuotaFailure(vararg violations: QuotaFailure.Violation): StatusException {
        withDetails(QuotaFailure(*violations))
        return this
    }

    fun withRetryInfo(retryDelay: Duration): StatusException {
        withDetails(RetryInfo(retryDelay))
        return this
    }

    fun withRetryInfo(
        number: Long,
        unit: TimeUnit = TimeUnit.SECONDS,
    ): StatusException {
        withDetails(RetryInfo(number, unit))
        return this
    }

    fun withDetails(message: Message<*, *>): StatusException {
        _details += message
        return this
    }

    fun withDetails(vararg messages: Message<*, *>): StatusException {
        _details += messages.toList()
        return this
    }

    fun withDetails(messages: Iterable<Message<*, *>>): StatusException {
        _details += messages
        return this
    }

    fun <T> withTrailer(
        key: Metadata.Key<T>,
        value: T,
    ): StatusException {
        trailers.put(key, value)
        return this
    }

    fun withTrailers(trailers: Metadata): StatusException {
        this.trailers.merge(trailers)
        return this
    }

    fun asStatus(): Status {
        return Status.fromCodeValue(code)
            .withDescription(message)
            .withCause(this)
    }

    fun asStatusDetail(): com.bybutter.sisyphus.rpc.Status {
        return Status {
            this.code = this@StatusException.code
            this.message = this@StatusException.message ?: "Unknown"
            this.details += this@StatusException.details
        }
    }
}
