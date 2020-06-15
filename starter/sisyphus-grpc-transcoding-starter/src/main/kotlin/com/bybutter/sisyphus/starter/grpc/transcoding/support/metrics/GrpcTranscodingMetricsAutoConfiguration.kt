package com.bybutter.sisyphus.starter.grpc.transcoding.support.metrics

import com.bybutter.sisyphus.api.http
import com.bybutter.sisyphus.api.resource.PathTemplate
import com.bybutter.sisyphus.protobuf.primitives.MethodDescriptorProto
import com.bybutter.sisyphus.starter.grpc.transcoding.TranscodingFunctions
import io.grpc.MethodDescriptor
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.metrics.web.reactive.WebFluxMetricsAutoConfiguration
import org.springframework.boot.actuate.metrics.web.reactive.server.WebFluxTagsContributor
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.server.ServerWebExchange

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(MetricsAutoConfiguration::class, SimpleMetricsExportAutoConfiguration::class)
@AutoConfigureBefore(WebFluxMetricsAutoConfiguration::class)
@ConditionalOnBean(MeterRegistry::class)
class GrpcTranscodingMetricsAutoConfiguration(private val properties: MetricsProperties) {
    @Bean
    fun webFluxTagsProvider(): WebFluxTagsContributor {
        return GrpcTranscodingWebFluxTagsContributor(properties.web.server.request.isIgnoreTrailingSlash)
    }
}

class GrpcTranscodingWebFluxTagsContributor(private val ignoreTrailingSlash: Boolean) : WebFluxTagsContributor {
    override fun httpRequestTags(exchange: ServerWebExchange, ex: Throwable): MutableIterable<Tag> {
        val methodDescriptor = exchange.getAttribute<MethodDescriptor<*, *>>(TranscodingFunctions.METHOD_DESCRIPTOR_ATTRIBUTE)
            ?: return mutableListOf()
        val pathTemplate = exchange.getAttribute<PathTemplate>(TranscodingFunctions.MATCHING_PATH_TEMPLATE_ATTRIBUTE)
            ?: return mutableListOf()

        return Tags.of(
            "method", methodDescriptor.fullMethodName,
            "service", methodDescriptor.serviceName,
            "uri", pathTemplate.toString()
        )
    }
}