package com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger

import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.http.HttpHeaders

/**
 *  Swagger configuration.
 * */
@ConfigurationProperties("swagger")
data class SwaggerProperty(
    var path: String = "/api-docs",
    var enableValidate: Boolean = true,
    var validateContent: Map<String, String> = mapOf(HttpHeaders.AUTHORIZATION to "BCC383E0E10F1F67"),
    @NestedConfigurationProperty
    var info: Info? = null,
    @NestedConfigurationProperty
    val params: List<Param>? = null,
    @NestedConfigurationProperty
    val servers: List<SwaggerServer>? = null,
    @NestedConfigurationProperty
    val securitySchemes: Map<String, Scheme>? = null
)
/**
 * Swagger params.
 * The value of parameter 'position' must be one of path, query, header or cookie.
 *@link https://swagger.io/specification/#parameterObject
 * */
data class Param(
    val name: String,
    val description: String?,
    val required: Boolean = false,
    val position: String,
    @NestedConfigurationProperty
    val schema: ParamSchema?
)

data class ParamSchema(
    val default: String?,
    val enumValues: List<String>?,
    val ref: String?
)

/**
 *  Swagger info .
 *  Used to build Swagger Info.
 *  @link https://swagger.io/specification/#infoObject
 * */
data class Info(
    val title: String = "Sisyphus Apis",
    val version: String = "1.0",
    val description: String? = null,
    @NestedConfigurationProperty
    val contact: Contact?
)
data class Contact(
    val name: String? = null,
    val url: String? = null,
    val email: String? = null
)
/**
 * Server info.
 * Used to build Swagger server.
 * @link https://swagger.io/specification/#serverObject
 * */
data class SwaggerServer(
    val url: String,
    val description: String?,
    @NestedConfigurationProperty
    val serverVariables: Map<String, ServerVariable>?
)

data class ServerVariable(
    val enum: List<String>?,
    val default: String,
    val description: String?
)
/**
 * Security scheme.
 * Used to build Swagger security scheme.
 * type value enum [SecurityScheme.Type].
 * position value enum [SecurityScheme.In].
 * @link https://swagger.io/specification/#securitySchemeObject
 * */
data class Scheme(
    val type: String,
    val description: String?,
    val position: String?,
    val name: String?,
    val scheme: String?,
    val bearerFormat: String?,
    val openIdConnectUrl: String?,
    @NestedConfigurationProperty
    val oAuthFlows: SwaggerOAuthFlows?
)

data class SwaggerOAuthFlows(
    @NestedConfigurationProperty
    val implicit: SwaggerOAuthFlow?,
    @NestedConfigurationProperty
    val password: SwaggerOAuthFlow?,
    @NestedConfigurationProperty
    val clientCredentials: SwaggerOAuthFlow?,
    @NestedConfigurationProperty
    val authorizationCode: SwaggerOAuthFlow?
)

data class SwaggerOAuthFlow(
    val authorizationUrl: String?,
    val tokenUrl: String?,
    val refreshUrl: String?,
    val scopes: Map<String, String>?
)
