package com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.utils

import com.bybutter.sisyphus.starter.grpc.transcoding.TranscodingServiceRouterFunction
import com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.Param
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.CookieParameter
import io.swagger.v3.oas.models.parameters.HeaderParameter
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.PathParameter
import io.swagger.v3.oas.models.parameters.QueryParameter

object SwaggerParams {
    fun fetchParams(params: List<Param>?): List<Parameter> {
        params ?: return listOf()
        return params.map { param ->
            when (param.position) {
                "header" -> HeaderParameter()
                "path" -> PathParameter()
                "query" -> QueryParameter()
                "cookie" -> CookieParameter()
                else -> throw NoSuchElementException("Unknown parameter location: ${param.position}. The value of parameter 'position' must be one of path, query, header or cookie")
            }.apply {
                name = param.name
                required = param.required
                description = param.description
                if (param.schema != null) {
                    schema = Schema<Any>().apply {
                        setDefault(param.schema.default)
                        enum = param.schema.enumValues
                        `$ref` = param.schema.ref
                    }
                }
            }
        }
    }

    fun fetchGrpcServiceNameParam(serviceName: String): HeaderParameter {
        return HeaderParameter().apply {
            name = TranscodingServiceRouterFunction.GRPC_SERVICE_NAME_HEADER
            required = false
            schema = StringSchema().apply {
                setDefault(serviceName)
            }
        }
    }
}
