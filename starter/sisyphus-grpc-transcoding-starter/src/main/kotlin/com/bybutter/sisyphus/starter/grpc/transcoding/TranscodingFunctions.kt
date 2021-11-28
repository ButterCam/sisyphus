package com.bybutter.sisyphus.starter.grpc.transcoding

import io.grpc.Channel
import org.springframework.web.server.ServerWebExchange

object TranscodingFunctions {
    /**
     * Name of the [attribute][ServerWebExchange.getAttributes] that
     * contains the matching path template, as a [PathTemplate].
     */
    val MATCHING_PATH_TEMPLATE_ATTRIBUTE = TranscodingFunctions::class.java.name + ".pathTemplate"

    /**
     * Name of the [attribute][ServerWebExchange.getAttributes] that
     * contains the target gRpc server channel, as a [Channel].
     */
    val GRPC_PROXY_CHANNEL_ATTRIBUTE = TranscodingFunctions::class.java.name + ".proxyChannel"

    /**
     * Name of the [attribute][ServerWebExchange.getAttributes] that
     * contains the current transcoding rule, as a [TranscodingRouterRule].
     */
    val TRANSCODING_RULE_ATTRIBUTE = TranscodingFunctions::class.java.name + ".rule"

    val HEADER_EXPORTER_ATTRIBUTE = TranscodingHeaderExporter::class.java.name + ".exporters"
}
