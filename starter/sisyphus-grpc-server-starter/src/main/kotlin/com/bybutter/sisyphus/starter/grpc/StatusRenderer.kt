package com.bybutter.sisyphus.starter.grpc

import com.bybutter.sisyphus.rpc.STATUS_META_KEY
import com.bybutter.sisyphus.rpc.StatusException
import io.grpc.Metadata
import io.grpc.Status
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

interface StatusRenderer {
    fun canRender(status: Status): Boolean

    fun render(status: Status, trailers: Metadata): Status?
}

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class SisyphusStatusRenderer : StatusRenderer {
    override fun canRender(status: Status): Boolean {
        return status.cause is StatusException
    }

    override fun render(status: Status, trailers: Metadata): Status? {
        val cause = status.cause as? StatusException ?: return null
        trailers.merge(cause.trailers)
        trailers.put(STATUS_META_KEY, cause.asStatusDetail())
        return cause.asStatus()
    }
}
