package com.bybutter.sisyphus.starter.grpc.transcoding.swagger.utils

import com.bybutter.sisyphus.api.HttpRule
import com.bybutter.sisyphus.api.resource.PathTemplate
import com.bybutter.sisyphus.protobuf.ProtoSupport
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import com.google.protobuf.DescriptorProtos
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
    fun fetchPaths(httpRule: HttpRule, inputTypeProto: ProtoSupport<*, *>, inputTypeFields: MutableList<FieldDescriptorProto>, operation: Operation): Paths {
        return Paths().apply {
            when (httpRule.pattern) {
                is HttpRule.Pattern.Get -> {
                    val requestUrlMap = fetchRequestUrl(httpRule.get)
                    this[requestUrlMap.keys.first()] = PathItem().get(fetchOperation(httpRule.get, requestUrlMap.values.first(), inputTypeProto, inputTypeFields, operation))
                }
                is HttpRule.Pattern.Post -> {
                    val requestUrlMap = fetchRequestUrl(httpRule.post)
                    this[requestUrlMap.keys.first()] = PathItem().post(fetchOperation(httpRule.post, requestUrlMap.values.first(), inputTypeProto, inputTypeFields, operation))
                }
                is HttpRule.Pattern.Put -> {
                    val requestUrlMap = fetchRequestUrl(httpRule.put)
                    this[requestUrlMap.keys.first()] = PathItem().put(fetchOperation(httpRule.put, requestUrlMap.values.first(), inputTypeProto, inputTypeFields, operation))
                }
                is HttpRule.Pattern.Patch -> {
                    val requestUrlMap = fetchRequestUrl(httpRule.patch)
                    this[requestUrlMap.keys.first()] = PathItem().patch(fetchOperation(httpRule.patch, requestUrlMap.values.first(), inputTypeProto, inputTypeFields, operation))
                }
                is HttpRule.Pattern.Delete -> {
                    val requestUrlMap = fetchRequestUrl(httpRule.delete)
                    this[requestUrlMap.keys.first()] = PathItem().delete(fetchOperation(httpRule.delete, requestUrlMap.values.first(), inputTypeProto, inputTypeFields, operation))
                }
                else -> throw UnsupportedOperationException("Unknown http rule pattern")
            }
        }
    }

    /***
     * Get the url and path parameters needed by swagger according to the request url.
     * Path parameters are named in the order of var1, var2, var3 ...
     * Several conversion forms of url:
     *  1. /v1/sisyphus/users --> /v1/sisyphus/users
     *  2. /v1/sisyphus/{name=users\*} --> /v1/sisyphus/users/{var1}
     *  3. /v1/sisyphus/{name=users\*\*} --> /v1/sisyphus/users/{var1}/{var2}
     *  4. /v1/sisyphus/{name=users\**} --> /v1/sisyphus/users/{var1}
     * */
    private fun fetchRequestUrl(url: String): Map<String, List<String>> {
        val params = mutableListOf<String>()
        val requestUrl = buildString {
            for (s in PathTemplate.create(url).withoutVars().toString().split("/")) {
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
        }
        return mapOf(requestUrl to params)
    }

    /**
     *  Add path params and query params to operation.
     * */
    private fun fetchOperation(url: String, pathParamList: List<String>, inputTypeProto: ProtoSupport<*, *>, fieldDescriptionList: List<FieldDescriptorProto>, operation: Operation): Operation {
        val fileDescriptor = ProtoTypes.getFileContainingSymbol(inputTypeProto.fullName)
        val path = listOf(DescriptorProtos.FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER,
                fileDescriptor?.messageType?.indexOf(inputTypeProto.descriptor),
                DescriptorProtos.FileDescriptorProto.PACKAGE_FIELD_NUMBER)

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
                        description = SwaggerDescription.fetchDescription(path + inputTypeProto.descriptor.field.indexOf(field), fileDescriptor)
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
