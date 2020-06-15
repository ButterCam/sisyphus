package com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.utils

import com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.Scheme
import com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.SwaggerOAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.Scopes
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme

object SwaggerSecuritySchemes {
    fun fetchSecurityRequirement(schemes: Map<String, Scheme>): SecurityRequirement {
        return SecurityRequirement().apply {
            schemes.forEach {
                addList(it.key)
            }
        }
    }

    fun fetchSecuritySchemes(schemes: Map<String, Scheme>): Map<String, SecurityScheme> {
        val securitySchemes = mutableMapOf<String, SecurityScheme>()
        for ((key, value) in schemes) {
            val scheme = SecurityScheme().apply {
                description = value.description
                when (value.type) {
                    SecurityScheme.Type.APIKEY.toString() -> {
                        type = SecurityScheme.Type.APIKEY
                        name = value.name
                        `in` = when (value.position) {
                            SecurityScheme.In.QUERY.toString() -> SecurityScheme.In.QUERY
                            SecurityScheme.In.HEADER.toString() -> SecurityScheme.In.HEADER
                            SecurityScheme.In.COOKIE.toString() -> SecurityScheme.In.COOKIE
                            else -> throw NoSuchElementException("Unknown security scheme parameter location: ${value.position},The value of parameter 'position' must be one of query, header or cookie")
                        }
                    }
                    SecurityScheme.Type.HTTP.toString() -> {
                        type = SecurityScheme.Type.HTTP
                        scheme = value.scheme
                        bearerFormat = value.bearerFormat
                    }
                    SecurityScheme.Type.OAUTH2.toString() -> {
                        type = SecurityScheme.Type.OAUTH2
                        if (value.oAuthFlows != null) {
                            flows = OAuthFlows().apply {
                                implicit = fetchOAuthFlow(value.oAuthFlows.implicit)
                                password = fetchOAuthFlow(value.oAuthFlows.password)
                                clientCredentials = fetchOAuthFlow(value.oAuthFlows.clientCredentials)
                                authorizationCode = fetchOAuthFlow(value.oAuthFlows.authorizationCode)
                            }
                        }
                    }
                    SecurityScheme.Type.OPENIDCONNECT.toString() -> {
                        type = SecurityScheme.Type.OPENIDCONNECT
                        openIdConnectUrl = value.openIdConnectUrl
                    }
                }
            }
            securitySchemes[key] = scheme
        }
        return securitySchemes
    }

    private fun fetchOAuthFlow(swaggerOAuthFlow: SwaggerOAuthFlow?): OAuthFlow? {
        swaggerOAuthFlow ?: return null
        return OAuthFlow().apply {
            authorizationUrl = swaggerOAuthFlow.authorizationUrl
            tokenUrl = swaggerOAuthFlow.tokenUrl
            refreshUrl = swaggerOAuthFlow.refreshUrl
            scopes = Scopes().apply {
                for ((scopesKey, scopesValue) in swaggerOAuthFlow.scopes ?: mapOf()) {
                    addString(scopesKey, scopesValue)
                }
            }
        }
    }
}
