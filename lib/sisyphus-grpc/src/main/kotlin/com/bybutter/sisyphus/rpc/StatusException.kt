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

    constructor(code: Status.Code, message: String? = null, cause: Throwable? = null) : super(message ?: code.name, cause) {
        this._code = code.value()
    }

    fun withLocalizedMessage(locale: String, message: String): StatusException {
        withDetails(LocalizedMessage {
            this.locale = locale
            this.message = message
        })
        return this
    }

    fun withLocalizedMessage(message: String): StatusException {
        withLocalizedMessage("zh-CN", message)
        return this
    }

    fun withHelps(vararg links: Help.Link): StatusException {
        withDetails(Help {
            this.links += links.toList()
        })
        return this
    }

    fun withResourceInfo(resourceType: String, resourceName: String, description: String, owner: String = ""): StatusException {
        withDetails(ResourceInfo {
            this.resourceType = resourceType
            this.resourceName = resourceName
            this.description = description
            this.owner = owner
        })
        return this
    }

    fun withRequestInfo(requestId: String, servingData: String = ""): StatusException {
        withDetails(RequestInfo {
            this.requestId = requestId
            this.servingData = servingData
        })
        return this
    }

    fun withBadRequest(vararg violations: BadRequest.FieldViolation): StatusException {
        withDetails(BadRequest {
            this.fieldViolations += violations.toList()
        })
        return this
    }

    fun withPreconditionFailure(vararg violations: PreconditionFailure.Violation): StatusException {
        withDetails(PreconditionFailure {
            this.violations += violations.toList()
        })
        return this
    }

    fun withQuotaFailure(vararg violations: QuotaFailure.Violation): StatusException {
        withDetails(QuotaFailure {
            this.violations += violations.toList()
        })
        return this
    }

    fun withRetryInfo(retryDelay: Duration): StatusException {
        withDetails(RetryInfo {
            this.retryDelay = retryDelay
        })
        return this
    }

    fun withRetryInfo(number: Long, unit: TimeUnit = TimeUnit.SECONDS): StatusException {
        withRetryInfo(Duration {
            seconds = unit.toSeconds(number)
            nanos = (unit.toNanos(number) - TimeUnit.SECONDS.toNanos(seconds)).toInt()
        })
        return this
    }

    fun withDetails(message: Message<*, *>): StatusException {
        _details += message
        return this
    }

    fun <T> withTrailer(key: Metadata.Key<T>, value: T): StatusException {
        trailers.put(key, value)
        return this
    }

    fun withTrailers(trailers: Metadata): StatusException {
        this.trailers.merge(trailers)
        return this
    }
}

open class ClientStatusException(val status: Status, val trailers: Metadata) : RuntimeException(status.description, status.cause)