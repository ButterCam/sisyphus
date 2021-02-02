package com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.utils

import com.bybutter.sisyphus.api.HttpRule
import com.bybutter.sisyphus.protobuf.MessageSupport
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.FileDescriptorProto
import com.google.api.pathtemplate.PathTemplate
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.PathParameter
import io.swagger.v3.oas.models.parameters.QueryParameter

object SwaggerPaths {

    /**
     * Build Paths based on request method.
     * */
    fun fetchPaths(
        httpRule: HttpRule,
        inputTypeProto: MessageSupport<*, *>,
        inputTypeFields: MutableList<FieldDescriptorProto>,
        operation: Operation,
        paths: Paths
    ): Paths {
        when (httpRule.pattern) {
            is HttpRule.Pattern.Get -> {
                val requestUrlMap = fetchRequestUrl(httpRule.get)
                val key = requestUrlMap.keys.first()
                val item = fetchOperation(
                    httpRule.get,
                    requestUrlMap.values.first(),
                    inputTypeProto,
                    inputTypeFields,
                    operation
                )
                if (paths[key] == null) {
                    paths[key] = PathItem().get(item)
                } else {
                    paths[key].apply {
                        this!!.get = item
                    }
                }
            }
            is HttpRule.Pattern.Post -> {
                val requestUrlMap = fetchRequestUrl(httpRule.post)
                val key = requestUrlMap.keys.first()
                val item = fetchOperation(
                    httpRule.post,
                    requestUrlMap.values.first(),
                    inputTypeProto,
                    inputTypeFields,
                    operation
                )
                if (paths[key] == null) {
                    paths[key] = PathItem().post(item)
                } else {
                    paths[key].apply {
                        this!!.post = item
                    }
                }
            }
            is HttpRule.Pattern.Put -> {
                val requestUrlMap = fetchRequestUrl(httpRule.put)
                val key = requestUrlMap.keys.first()
                val item = fetchOperation(
                    httpRule.put,
                    requestUrlMap.values.first(),
                    inputTypeProto,
                    inputTypeFields,
                    operation
                )
                if (paths[key] == null) {
                    paths[key] = PathItem().put(item)
                } else {
                    paths[key].apply {
                        this!!.put = item
                    }
                }
            }
            is HttpRule.Pattern.Patch -> {
                val requestUrlMap = fetchRequestUrl(httpRule.patch)
                val key = requestUrlMap.keys.first()
                val item = fetchOperation(
                    httpRule.patch,
                    requestUrlMap.values.first(),
                    inputTypeProto,
                    inputTypeFields,
                    operation
                )
                if (paths[key] == null) {
                    paths[key] = PathItem().patch(item)
                } else {
                    paths[key].apply {
                        this!!.patch = item
                    }
                }
            }
            is HttpRule.Pattern.Delete -> {
                val requestUrlMap = fetchRequestUrl(httpRule.delete)
                val key = requestUrlMap.keys.first()
                val item = fetchOperation(
                    httpRule.delete,
                    requestUrlMap.values.first(),
                    inputTypeProto,
                    inputTypeFields,
                    operation
                )
                if (paths[key] == null) {
                    paths[key] = PathItem().delete(item)
                } else {
                    paths[key].apply {
                        this!!.delete = item
                    }
                }
            }
            else -> throw UnsupportedOperationException("Unknown http rule pattern")
        }
        return paths
    }

    /***
     * Get the url and path parameters needed by swagger according to the request url.
     * Path parameters are named in the order of var1, var2, var3 ...
     * Several conversion forms of url:
     *  1. /v1/sisyphus/users --> /v1/sisyphus/users
     *  2. /v1/sisyphus/{name=users\*} --> /v1/sisyphus/users/{var1}
     *  3. /v1/sisyphus/{name=users\*\*} --> /v1/sisyphus/users/{var1}/{var2}
     *  4. /v1/sisyphus/{name=users\**} --> /v1/sisyphus/users/{var1}
     *  5. /v1/sisyphus/{name=users\*}:archive --> /v1/sisyphus/users/{var1}:archive
     * */
    private fun fetchRequestUrl(url: String): Map<String, List<String>> {
        val params = mutableListOf<String>()
        // According to: segmentation, determine whether to include a custom request method.
        val urlFragments = url.split(":")
        val requestUrl = buildString {
            for (s in PathTemplate.create(urlFragments[0]).withoutVars().toString().split("/")) {
                if (s.isNotEmpty()) {
                    append("/")
                    when (s) {
                        "*", "**" -> {
                            val param = "var${params.size + 1}"
                            params.add(param)
                            append("{")
                            append(param)
                            append("}")
                        }
                        else -> append(s)
                    }
                }
            }
            if (urlFragments.size == 2) {
                append(":")
                append(urlFragments[1])
            }
        }
        return mapOf(requestUrl to params)
    }

    /**
     *  Add path params and query params to operation.
     * */
    private fun fetchOperation(
        url: String,
        pathParamList: List<String>,
        inputTypeProto: MessageSupport<*, *>,
        fieldDescriptionList: List<FieldDescriptorProto>,
        operation: Operation
    ): Operation {
        val fileSupport = inputTypeProto.file()
        val path = listOf(
            FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER,
            fileSupport.descriptor.messageType.indexOf(inputTypeProto.descriptor)
        )

        pathParamList.forEach { param ->
            operation.addParametersItem(PathParameter().apply {
                name = param
                required = true
                schema = StringSchema()
                allowReserved = true
                description = param
            })
        }
        return operation.apply {
            val pathParams = PathTemplate.create(url).vars()
            fieldDescriptionList.forEach { field ->
                if (pathParams.find { param -> param == field.name || param == field.jsonName } == null) {
                    this.addParametersItem(QueryParameter().apply {
                        description = SwaggerDescription.fetchDescription(
                            path + inputTypeProto.descriptor.field.indexOf(field),
                            fileSupport.descriptor
                        )
                        name = field.jsonName
                        required = false
                        schema = if (field.label == FieldDescriptorProto.Label.REPEATED) {
                            ArraySchema().items(SwaggerSchema.fetchSchema(field.type, field.typeName))
                        } else {
                            SwaggerSchema.fetchSchema(field.type, field.typeName)
                        }
                    })
                }
            }
        }
    }
}
