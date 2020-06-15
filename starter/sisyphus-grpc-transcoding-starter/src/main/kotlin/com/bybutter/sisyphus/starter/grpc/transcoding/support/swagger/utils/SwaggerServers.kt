package com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.utils

import com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.SwaggerServer
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.servers.ServerVariable
import io.swagger.v3.oas.models.servers.ServerVariables

object SwaggerServers {
    fun fetchServers(servers: List<SwaggerServer>?): List<Server> {
        return mutableListOf<Server>().apply {
            servers?.forEach {
                add(Server().apply {
                    url = it.url
                    description = it.description
                    variables(ServerVariables().apply {
                        for ((key, value) in it.serverVariables ?: mapOf()) {
                            addServerVariable(key, ServerVariable().apply {
                                default = value.default
                                enum = value.enum
                                description = value.description
                            })
                        }
                    })
                })
            }
        }
    }
}
