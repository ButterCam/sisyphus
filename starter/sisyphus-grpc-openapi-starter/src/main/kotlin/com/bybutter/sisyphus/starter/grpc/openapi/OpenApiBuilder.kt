package com.bybutter.sisyphus.starter.grpc.openapi

import com.bybutter.sisyphus.api.HttpRule
import com.bybutter.sisyphus.api.http
import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.EnumSupport
import com.bybutter.sisyphus.protobuf.FileSupport
import com.bybutter.sisyphus.protobuf.MessageSupport
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.ServiceSupport
import com.bybutter.sisyphus.protobuf.findFileSupport
import com.bybutter.sisyphus.protobuf.findMapEntryDescriptor
import com.bybutter.sisyphus.protobuf.findMessageSupport
import com.bybutter.sisyphus.protobuf.findServiceSupport
import com.bybutter.sisyphus.protobuf.primitives.BoolValue
import com.bybutter.sisyphus.protobuf.primitives.BytesValue
import com.bybutter.sisyphus.protobuf.primitives.DescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.DoubleValue
import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.FieldMask
import com.bybutter.sisyphus.protobuf.primitives.FileDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.FloatValue
import com.bybutter.sisyphus.protobuf.primitives.Int32Value
import com.bybutter.sisyphus.protobuf.primitives.Int64Value
import com.bybutter.sisyphus.protobuf.primitives.ListValue
import com.bybutter.sisyphus.protobuf.primitives.MethodDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.ServiceDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.StringValue
import com.bybutter.sisyphus.protobuf.primitives.Struct
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.UInt32Value
import com.bybutter.sisyphus.protobuf.primitives.UInt64Value
import com.bybutter.sisyphus.protobuf.primitives.Value
import com.bybutter.sisyphus.protobuf.primitives.invoke
import com.bybutter.sisyphus.protobuf.primitives.now
import com.bybutter.sisyphus.protobuf.primitives.string
import com.bybutter.sisyphus.rpc.LocalizedMessage
import com.bybutter.sisyphus.rpc.Status
import com.bybutter.sisyphus.string.plural
import com.google.api.pathtemplate.PathTemplate
import io.grpc.ServerServiceDefinition
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.BinarySchema
import io.swagger.v3.oas.models.media.BooleanSchema
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.DateTimeSchema
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.JsonSchema
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.NumberSchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.tags.Tag

fun openApi(init: OpenApiBuilder.() -> Unit): OpenAPI {
    return OpenApiBuilder().apply(init).build()
}

class OpenApiBuilder() {
    private val openApi = OpenAPI().apply {
        paths = Paths()
        components = Components()
    }
    private val handledFiles = mutableSetOf<String>()

    fun addService(service: ServerServiceDefinition): OpenApiBuilder {
        val serviceSupport = ProtoTypes.findServiceSupport(".${service.serviceDescriptor.name}")
        openApi.addTagsItem(serviceSupport.toTag())
        serviceSupport.descriptor.method.forEach {
            addMethod(it, serviceSupport)
        }
        addFiles(serviceSupport.file())
        return this
    }

    private fun addMethod(method: MethodDescriptorProto, service: ServiceSupport) {
        val http = method.options?.http ?: return
        val elementPath = service.path() + listOf(
            ServiceDescriptorProto.METHOD_FIELD_NUMBER,
            service.descriptor.method.indexOf(method)
        )
        val (kind, path) = when (val pattern = http.pattern) {
            is HttpRule.Pattern.Custom -> pattern.value.kind.lowercase() to pattern.value.path
            is HttpRule.Pattern.Delete -> "delete" to pattern.value
            is HttpRule.Pattern.Get -> "get" to pattern.value
            is HttpRule.Pattern.Patch -> "patch" to pattern.value
            is HttpRule.Pattern.Post -> "post" to pattern.value
            is HttpRule.Pattern.Put -> "put" to pattern.value
            else -> return
        }
        val (url, action) = path.substringBefore(':') to path.substringAfter(':', "")
        val template = PathTemplate.create(url)
        val inputType = ProtoTypes.findMessageSupport(method.inputType)
        val outputType = ProtoTypes.findMessageSupport(method.outputType)
        val parameters = mutableListOf<Parameter>()
        val normalized = buildString {
            template.withoutVars().toString().split('/').forEach {
                when (it) {
                    "" -> return@forEach
                    "*", "**" -> {
                        val varName = "var${parameters.size + 1}"
                        val part = "/{$varName}"
                        append(part)
                        parameters.add(
                            Parameter().apply {
                                this.name = varName
                                this.`in` = "path"
                                this.description = "Path variable `$varName`"
                                this.required = true
                                this.allowReserved = it == "**"
                            }
                        )
                    }

                    else -> {
                        append("/$it")
                    }
                }
            }
            if (this.isBlank()) {
                append("/")
            }
            if (action.isNotBlank()) {
                append(":$action")
            }
        }
        val comment = getComment(elementPath, service.file()) ?: ""
        val (summary, description) = extractSummary(comment)

        val pathItem = openApi.paths[normalized] ?: PathItem().also {
            openApi.paths.addPathItem(normalized, it)
        }
        val serviceName = service.name.trim('.')
        val operation = Operation().apply {
            this.tags = listOf(serviceName)
            this.summary = summary ?: method.name
            this.description = description
            this.operationId = "$serviceName/${method.name}"
            this.deprecated = method.options?.deprecated
            val bodyMediaType = when (http.body) {
                "" -> {
                    parameters += queryParameters(template, null, inputType)
                    null
                }

                "*" -> MediaType().apply {
                    this.schema = ObjectSchema().apply {
                        this.`$ref` = ref(inputType.name)
                    }
                }

                else -> {
                    val field = inputType.fieldInfo(http.body) ?: return
                    parameters += queryParameters(template, field, inputType)
                    MediaType().apply {
                        this.schema = field.toSchema()
                    }
                }
            }
            this.requestBody = bodyMediaType?.let {
                RequestBody().apply {
                    this.required = true
                    this.content = Content().apply {
                        addMediaType("application/json", it)
                        addMediaType("multipart/form-data", it)
                        addMediaType("application/x-www-form-urlencoded", it)
                    }
                }
            }
            val responseBodyMediaType = when (http.responseBody) {
                "", "*" -> MediaType().apply {
                    this.schema = ObjectSchema().apply {
                        this.`$ref` = ref(outputType.name)
                    }
                }

                else -> {
                    val field = outputType.fieldInfo(http.responseBody) ?: return
                    MediaType().apply {
                        this.schema = field.toSchema()
                    }
                }
            }
            this.responses = ApiResponses().apply {
                this.addApiResponse(
                    "2XX",
                    ApiResponse().apply {
                        this.content = Content().apply {
                            addMediaType("application/json", responseBodyMediaType)
                        }
                    }
                )
                this.addApiResponse(
                    "4XX",
                    ApiResponse().apply {
                        this.`$ref` = "#/components/responses/ClientError"
                    }
                )
                this.addApiResponse(
                    "5XX",
                    ApiResponse().apply {
                        this.`$ref` = "#/components/responses/ServerError"
                    }
                )
            }
            parameters.forEach {
                this.addParametersItem(it)
            }
        }
        when (kind) {
            "get" -> pathItem.get = operation
            "put" -> pathItem.put = operation
            "post" -> pathItem.post = operation
            "delete" -> pathItem.delete = operation
            "patch" -> pathItem.patch = operation
            "head" -> pathItem.head = operation
            "options" -> pathItem.options = operation
            "trace" -> pathItem.trace = operation
        }
    }

    private fun queryParameters(
        path: PathTemplate,
        body: FieldDescriptorProto?,
        inputType: MessageSupport<*, *>
    ): List<Parameter> {
        val pathParameters = path.vars()
        val inputTypePath = inputType.path()
        return inputType.descriptor.field.mapIndexedNotNull() { index, it ->
            if (it == body) return@mapIndexedNotNull null
            if (it.name in pathParameters || it.jsonName in pathParameters) return@mapIndexedNotNull null
            val fieldPath = inputTypePath + listOf(DescriptorProto.FIELD_FIELD_NUMBER, index)
            Parameter().apply {
                this.name = it.name
                this.`in` = "query"
                this.schema = it.toSchema()
                this.description = getComment(fieldPath, inputType.file())
            }
        }
    }

    private fun addFiles(file: FileSupport) {
        if (handledFiles.contains(file.name)) return

        file.children().forEach {
            when (it) {
                is MessageSupport<*, *> -> {
                    val path = listOf(
                        FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER,
                        file.descriptor.messageType.indexOf(it.descriptor)
                    )
                    addMessage(path, it)
                }

                is EnumSupport<*> -> {
                    val path = listOf(
                        FileDescriptorProto.ENUM_TYPE_FIELD_NUMBER,
                        file.descriptor.enumType.indexOf(it.descriptor)
                    )
                    addEnum(path, it)
                }
            }
        }

        file.descriptor.dependency.forEach {
            addFiles(ProtoTypes.findFileSupport(it))
        }
    }

    private fun addMessage(path: List<Int>, message: MessageSupport<*, *>) {
        val comment = getComment(path, message.file())
        val schema = when (message.name) {
            com.bybutter.sisyphus.protobuf.primitives.Any.name -> ObjectSchema().apply {
                this.addProperty("@type", StringSchema().example("types.googleapis.com/${message.name.trim('.')}"))
            }

            FieldMask.name -> StringSchema().example("*")
            Timestamp.name -> DateTimeSchema().example(Timestamp.now().string())
            Duration.name -> StringSchema().example(Duration.invoke(100).string())
            Struct.name -> ObjectSchema()
            Value.name -> JsonSchema()
            ListValue.name -> ArraySchema()
            DoubleValue.name -> NumberSchema().format("double").example(1.0)
            FloatValue.name -> NumberSchema().format("float").example(1.0)
            Int64Value.name, UInt64Value.name -> NumberSchema().format("int64").example(0)

            Int32Value.name, UInt32Value.name -> NumberSchema().format("int32").example(0)

            BoolValue.name -> BooleanSchema()
            StringValue.name -> StringSchema()
            BytesValue.name -> BinarySchema().format("base64")
            ListValue.name -> ArraySchema()
            else -> ObjectSchema().apply {
                message.descriptor.field.forEach {
                    val fieldPath = path + listOf(
                        DescriptorProto.FIELD_FIELD_NUMBER,
                        message.descriptor.field.indexOf(it)
                    )
                    this.addProperty(
                        it.jsonName,
                        Schema<Any>().apply {
                            this.description = getComment(fieldPath, message.file())
                            this.deprecated = it.options?.deprecated
                            this.addAllOfItem(it.toSchema())
                        }
                    )
                }
            }
        }

        openApi.components.addSchemas(
            message.name.trim('.'),
            schema.apply {
                this.name = message.descriptor.name
                this.description = comment
                this.deprecated = message.descriptor.options?.deprecated
            }
        )

        message.children().forEach {
            when (it) {
                is MessageSupport<*, *> -> {
                    val childPath = path + listOf(
                        DescriptorProto.NESTED_TYPE_FIELD_NUMBER,
                        message.descriptor.nestedType.indexOf(it.descriptor)
                    )
                    addMessage(childPath, it)
                }

                is EnumSupport<*> -> {
                    val childPath = path + listOf(
                        DescriptorProto.ENUM_TYPE_FIELD_NUMBER,
                        message.descriptor.enumType.indexOf(it.descriptor)
                    )
                    addEnum(childPath, it)
                }
            }
        }

        message.descriptor.nestedType.forEach {
            if (it.options?.mapEntry != true) return@forEach
            addMap(message, it)
        }
    }

    private fun addMap(message: MessageSupport<*, *>, entry: DescriptorProto) {
        openApi.components.addSchemas(
            "${message.name.trim('.')}.${entry.name}-MAP",
            ObjectSchema().apply {
                this.name = entry.name.plural()
                this.additionalProperties = entry.field[1].toSchema()
            }
        )
    }

    private fun addEnum(path: List<Int>, enum: EnumSupport<*>) {
        val comment = getComment(path, enum.file())
        openApi.components.addSchemas(
            enum.name.trim('.'),
            StringSchema().apply {
                this.name = enum.descriptor.name
                this.description = comment
                enum.descriptor.value.forEach {
                    this.addEnumItem(it.name)
                }
            }
        )
    }

    private fun ServiceSupport.toTag(): Tag {
        return Tag().apply {
            this.name = this@toTag.name.trim('.')
            this.description = getComment(path(), file())
        }
    }

    private fun ServiceSupport.path(): List<Int> {
        return listOf(
            FileDescriptorProto.SERVICE_FIELD_NUMBER,
            this.file().descriptor.service.indexOf(this.descriptor)
        )
    }

    private fun MessageSupport<*, *>.path(): List<Int> {
        when (val parent = this.parent) {
            is FileSupport -> {
                return listOf(
                    FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER,
                    parent.descriptor.messageType.indexOf(this.descriptor)
                )
            }

            is MessageSupport<*, *> -> {
                return parent.path() + listOf(
                    DescriptorProto.NESTED_TYPE_FIELD_NUMBER,
                    parent.descriptor.nestedType.indexOf(this.descriptor)
                )
            }

            else -> throw IllegalStateException()
        }
    }

    private fun MessageSupport<*, *>.resolveField(field: String): Pair<MessageSupport<*, *>, FieldDescriptorProto>? {
        val fields = field.split('.', limit = 2)
        val fieldInfo = if (this.descriptor.options?.mapEntry == true) {
            this.fieldInfo("value")
        } else {
            this.fieldInfo(fields[0])
        } ?: return null

        if (fields.size == 1) {
            return this to fieldInfo
        }

        if (fieldInfo.type != FieldDescriptorProto.Type.MESSAGE) return null
        val target = ProtoTypes.findSupport(fieldInfo.typeName) as MessageSupport<*, *>
        return target.resolveField(fields[1])
    }

    private fun getComment(path: List<Int>, file: FileSupport): String? {
        val location = file.descriptor.sourceCodeInfo?.location?.firstOrNull { location ->
            location.path.contentEquals(path)
        }
        location ?: return null
        return listOf(location.leadingComments, location.trailingComments).filter { it.isNotBlank() }
            .joinToString("\n\n").replace(regex, "").trim()
    }

    private fun extractSummary(comment: String): Pair<String?, String?> {
        if (comment.isBlank()) return null to null

        val lines = comment.lines()
        if (lines.size == 1) {
            return if (lines[0].length > 30) {
                null to lines[0]
            } else {
                lines[0] to null
            }
        }
        return if (lines[0].length > 30) {
            null to comment.trim()
        } else {
            lines[0] to lines.drop(1).joinToString("\n").trim()
        }
    }

    private fun ref(name: String): String {
        return "#/components/schemas/${name.trim('.')}"
    }

    private fun FieldDescriptorProto.toSchema(): Schema<*> {
        val schema = when (type) {
            FieldDescriptorProto.Type.INT32, FieldDescriptorProto.Type.SINT32, FieldDescriptorProto.Type.UINT32, FieldDescriptorProto.Type.FIXED32, FieldDescriptorProto.Type.SFIXED32 -> IntegerSchema().format(
                "int32"
            )

            FieldDescriptorProto.Type.INT64, FieldDescriptorProto.Type.SINT64, FieldDescriptorProto.Type.UINT64, FieldDescriptorProto.Type.FIXED64, FieldDescriptorProto.Type.SFIXED64 -> IntegerSchema().format(
                "int64"
            )

            FieldDescriptorProto.Type.STRING -> StringSchema()
            FieldDescriptorProto.Type.BYTES -> BinarySchema().contentEncoding("base64")
            FieldDescriptorProto.Type.DOUBLE -> NumberSchema().format("double")
            FieldDescriptorProto.Type.FLOAT -> NumberSchema().format("float")
            FieldDescriptorProto.Type.BOOL -> BooleanSchema()
            FieldDescriptorProto.Type.ENUM -> {
                Schema<Any>().apply {
                    this.`$ref` = ref(typeName)
                }
            }

            FieldDescriptorProto.Type.MESSAGE -> {
                Schema<Any>().apply {
                    this.`$ref` = ref(typeName)
                }
            }

            FieldDescriptorProto.Type.GROUP -> TODO()
        }

        return when (this.label) {
            FieldDescriptorProto.Label.REPEATED -> {
                if (type == FieldDescriptorProto.Type.MESSAGE) {
                    val entry = ProtoTypes.findMapEntryDescriptor(typeName)
                    if (entry != null) {
                        return Schema<Any>().apply {
                            this.`$ref` = ref("$typeName-MAP")
                        }
                    }
                }
                ArraySchema().items(schema)
            }

            else -> schema
        }
    }

    private val regex = """(\(-- api-linter:.[\s\S]+? --\))""".toRegex()

    fun build(): OpenAPI {
        addFiles(Status.file())
        addFiles(LocalizedMessage.file())
        openApi.components.addResponses(
            "ClientError",
            ApiResponse().apply {
                this.content = Content().apply {
                    addMediaType(
                        "application/json",
                        MediaType().apply {
                            this.schema = ObjectSchema().apply {
                                this.`$ref` = ref(Status.name)
                            }
                        }
                    )
                }
            }
        )
        openApi.components.addResponses(
            "ServerError",
            ApiResponse().apply {
                this.content = Content().apply {
                    addMediaType(
                        "application/json",
                        MediaType().apply {
                            this.schema = ObjectSchema().apply {
                                this.`$ref` = ref(Status.name)
                            }
                        }
                    )
                }
            }
        )
        return openApi
    }
}
