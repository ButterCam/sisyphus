package com.bybutter.sisyphus.starter.grpc.support.health.v1

import com.bybutter.sisyphus.rpc.Code
import com.bybutter.sisyphus.rpc.StatusException
import io.grpc.Server
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HealthService : Health() {
    private var server: Server? = null

    fun initServer(server: Server) {
        if (this.server != null && this.server != server) {
            throw IllegalArgumentException("Server is already set")
        }
        this.server = server
    }

    override suspend fun check(input: HealthCheckRequest): HealthCheckResponse {
        return HealthCheckResponse {
            status = checkService(input.service) ?: throw StatusException(Code.NOT_FOUND, "${input.service} ")
        }
    }

    override fun watch(input: HealthCheckRequest): Flow<HealthCheckResponse> =
        flow {
            emit(
                HealthCheckResponse {
                    status = checkService(input.service) ?: throw StatusException(Code.UNIMPLEMENTED)
                },
            )
        }

    private fun checkService(service: String): HealthCheckResponse.ServingStatus? {
        val server = this.server ?: return null
        if (server.services.all { it.serviceDescriptor.name != service }) return null
        if (server.isShutdown) return HealthCheckResponse.ServingStatus.NOT_SERVING
        if (server.isTerminated) return HealthCheckResponse.ServingStatus.NOT_SERVING
        return HealthCheckResponse.ServingStatus.SERVING
    }
}
