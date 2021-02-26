package com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger

import com.bybutter.sisyphus.api.http
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.FileDescriptorProto
import com.bybutter.sisyphus.starter.grpc.transcoding.EmptyRouterFunction
import com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.authentication.SwaggerValidate
import com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.utils.SwaggerDescription
import com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.utils.SwaggerParams
import com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.utils.SwaggerPaths
import com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.utils.SwaggerSchema
import com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.utils.SwaggerSecuritySchemes
import com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.utils.SwaggerServers
import io.grpc.Server
import io.grpc.ServerServiceDefinition
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.tags.Tag
import java.net.URI
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

class SwaggerRouterFunction private constructor(
    private val services: List<ServerServiceDefinition>,
    private val swaggerValidate: SwaggerValidate,
    private val property: SwaggerProperty
) : RouterFunction<ServerResponse>, HandlerFunction<ServerResponse> {

    override fun route(request: ServerRequest): Mono<HandlerFunction<ServerResponse>> {
        return if (request.path() != property.path) {
            Mono.empty()
        } else {
            Mono.just(this)
        }
    }

    override fun handle(request: ServerRequest): Mono<ServerResponse> {
        // If enable validate ,verify the request is legal.
        if (property.enableValidate) swaggerValidate.validate(request, property.validateContent)
        val tags = mutableListOf<Tag>()
        val paths = Paths()
        val schemas = mutableMapOf<String, Schema<*>>()
        val params = SwaggerParams.fetchParams(property.params)

        for (service in services) {
            // Get service descriptor.
            val serviceSupport = ProtoTypes.findServiceSupport(service.serviceDescriptor.name)
            // Get service file.
            val fileSupport = serviceSupport.file()
            // Get service path,used to find notes.
            val serverPath = listOf(
                FileDescriptorProto.SERVICE_FIELD_NUMBER,
                fileSupport.descriptor.service.indexOf(serviceSupport.descriptor)
            )
            val tag = serviceSupport.name.trim('.')
            val servicePaths = Paths()

            for (method in serviceSupport.descriptor.method) {
                val httpRule = method.options?.http ?: continue
                val modelNames = mutableSetOf(method.outputType)
                // Get input type proto,used to find notes and generate request parameters.
                val inputTypeProto = ProtoTypes.findMessageSupport(method.inputType)
                val inputTypeFields = (inputTypeProto.fieldDescriptors).toMutableList()
                val field =
                    inputTypeFields.firstOrNull { fieldDescriptorProto -> fieldDescriptorProto.name == httpRule.body }
                // Get request body param fields.
                val bodyParam = when (httpRule.body) {
                    "" -> null
                    "*" -> {
                        inputTypeFields.clear()
                        modelNames.add(method.inputType)
                        method.inputType
                    }
                    else -> {
                        inputTypeFields.remove(field)
                        if (field!!.type == FieldDescriptorProto.Type.MESSAGE) modelNames.add(field.typeName)
                        field.typeName
                    }
                }
                // Generate all schema related to this method.
                while (true) {
                    val subModelNames = mutableSetOf<String>()
                    for (typeName in modelNames) {
                        if (schemas.containsKey(typeName)) continue
                        SwaggerSchema.fetchSchemaModel(typeName)?.let { schema ->
                            schemas[typeName.trim('.')] = schema.schema
                            subModelNames.addAll(schema.subTypeNames)
                        }
                    }
                    modelNames.clear()
                    modelNames.addAll(subModelNames)

                    if (modelNames.isEmpty()) break
                }

                // Get method path, used to find notes.
                val methodPath = serverPath + listOf(
                    FileDescriptorProto.PACKAGE_FIELD_NUMBER,
                    serviceSupport.descriptor.method.indexOf(method)
                )
                val operation = Operation().apply {
                    this.tags = listOf(tag)
                    summary = SwaggerDescription.fetchDescription(methodPath, fileSupport.descriptor) ?: method.name
                    if (bodyParam != null) {
                        requestBody(RequestBody().apply {
                            required = true
                            content = Content().apply {
                                val schema = if (field != null && field.type != FieldDescriptorProto.Type.MESSAGE) {
                                    SwaggerSchema.fetchSchema(field.type, bodyParam)
                                } else {
                                    ObjectSchema().`$ref`(COMPONENTS_SCHEMAS_PREFIX + bodyParam.trim('.'))
                                }
                                addMediaType("application/json", MediaType().apply {
                                    this.schema = schema
                                })
                                addMediaType("text/xml", MediaType().apply {
                                    this.schema = schema
                                })
                                addMediaType("multipart/form-data", MediaType().apply {
                                    this.schema = schema
                                })
                                addMediaType("application/x-www-form-urlencoded", MediaType().apply {
                                    this.schema = schema
                                })
                            }
                        })
                    }

                    addParametersItem(SwaggerParams.fetchGrpcServiceNameParam(service.serviceDescriptor.name))

                    params.forEach { param ->
                        addParametersItem(param)
                    }

                    if (property.securitySchemes != null) {
                        addSecurityItem(SwaggerSecuritySchemes.fetchSecurityRequirement(property.securitySchemes!!))
                    }
                    responses = ApiResponses().apply {
                        addApiResponse("200", ApiResponse().apply {
                            description = method.outputType.split(".").last()
                            content = Content().apply {
                                addMediaType("application/json", MediaType().apply {
                                    schema =
                                        ObjectSchema().`$ref`(COMPONENTS_SCHEMAS_PREFIX + method.outputType.trim('.'))
                                })
                            }
                        })
                    }
                }
                operation.operationId("${service.serviceDescriptor.name}/${method.name}")
                servicePaths.putAll(
                    SwaggerPaths.fetchPaths(
                        httpRule,
                        inputTypeProto,
                        inputTypeFields,
                        operation,
                        servicePaths
                    )
                )
            }
            if (servicePaths.isNotEmpty()) {
                tags.add(Tag().apply {
                    name = tag
                    description = SwaggerDescription.fetchDescription(serverPath, fileSupport.descriptor)
                })
                paths.putAll(servicePaths)
            }
        }

        val openApi = OpenAPI().apply {
            info = Info().apply {
                title = property.info?.title
                version = property.info?.version
                description = property.info?.description
                contact = Contact().apply {
                    email = property.info?.contact?.email
                    url = property.info?.contact?.url
                    name = property.info?.contact?.name
                }
            }
            this.tags = tags
            this.paths = paths
            servers = listOf(io.swagger.v3.oas.models.servers.Server().apply {
                url =
                    URI(request.uri().scheme, null, request.uri().host, request.uri().port, null, null, null).toString()
                description = "Default Server"
            }) + SwaggerServers.fetchServers(property.servers)
            components = Components().apply {
                this.schemas = schemas
                if (property.securitySchemes != null) securitySchemes =
                    SwaggerSecuritySchemes.fetchSecuritySchemes(property.securitySchemes)
            }
        }
        return ServerResponse.ok().bodyValue(Json.mapper().writeValueAsString(openApi))
    }

    companion object {
        const val COMPONENTS_SCHEMAS_PREFIX = "#/components/schemas/"
        operator fun invoke(
            server: Server,
            enableServices: Collection<String> = listOf(),
            swaggerAuthentication: SwaggerValidate,
            swaggerProperty: SwaggerProperty
        ): RouterFunction<ServerResponse> {
            val enableServicesSet = enableServices.toSet()
            val enableServicesDefinition = mutableListOf<ServerServiceDefinition>()
            server.services.forEach {
                if (enableServicesSet.isEmpty() || enableServicesSet.contains(it.serviceDescriptor.name)) {
                    enableServicesDefinition.add(it)
                }
            }
            if (enableServicesDefinition.isEmpty()) return EmptyRouterFunction
            return SwaggerRouterFunction(enableServicesDefinition, swaggerAuthentication, swaggerProperty)
        }
    }
}
