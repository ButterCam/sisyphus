package com.bybutter.sisyphus.rpc

import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.primitives.Duration
import io.grpc.Metadata
import java.util.concurrent.TimeUnit

open class StatusException : RuntimeException {
    val status: Status
        get() {
            return Status {
                code = this@StatusException.code
                this@StatusException.message?.let { message = it }
                details += this@StatusException.details.toList()
            }
        }

    val trailers: Metadata = Metadata()

    private var code = 0

    private val details = mutableListOf<Message<*, *>>()

    constructor(code: Code, cause: Throwable) : this(code, cause.message, cause)

    constructor(code: Code, message: String? = null, cause: Throwable? = null) : super(message ?: code.name, cause) {
        this.code = code.number
    }

    constructor(status: Status) : super(status.message) {
        this.code = status.code
        this.details += status.details
    }

    fun withLocalizedMessage(locale: String, message: String): StatusException {
        details.add(LocalizedMessage {
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
        details.add(Help {
            this.links += links.toList()
        })
        return this
    }

    fun withResourceInfo(resourceType: String, resourceName: String, description: String, owner: String = ""): StatusException {
        details.add(ResourceInfo {
            this.resourceType = resourceType
            this.resourceName = resourceName
            this.description = description
            this.owner = owner
        })
        return this
    }

    fun withRequestInfo(requestId: String, servingData: String = ""): StatusException {
        details.add(RequestInfo {
            this.requestId = requestId
            this.servingData = servingData
        })
        return this
    }

    fun withBadRequest(vararg violations: BadRequest.FieldViolation): StatusException {
        details.add(BadRequest {
            this.fieldViolations += violations.toList()
        })
        return this
    }

    fun withPreconditionFailure(vararg violations: PreconditionFailure.Violation): StatusException {
        details.add(PreconditionFailure {
            this.violations += violations.toList()
        })
        return this
    }

    fun withQuotaFailure(vararg violations: QuotaFailure.Violation): StatusException {
        details.add(QuotaFailure {
            this.violations += violations.toList()
        })
        return this
    }

    fun withRetryInfo(retryDelay: Duration): StatusException {
        details.add(RetryInfo {
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
        details.add(message)
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

open class ClientStatusException(status: io.grpc.Status, val trailers: Metadata) : RuntimeException(status.description, status.cause) {
    val status: Status = trailers[STATUS_META_KEY] ?: Status.fromGrpcStatus(status)
}
