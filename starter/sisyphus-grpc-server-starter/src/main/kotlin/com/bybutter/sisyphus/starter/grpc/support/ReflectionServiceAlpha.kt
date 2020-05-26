package com.bybutter.sisyphus.starter.grpc.support

import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.starter.grpc.support.reflection.v1alpha.ExtensionNumberResponse
import com.bybutter.sisyphus.starter.grpc.support.reflection.v1alpha.ExtensionRequest
import com.bybutter.sisyphus.starter.grpc.support.reflection.v1alpha.FileDescriptorResponse
import com.bybutter.sisyphus.starter.grpc.support.reflection.v1alpha.ListServiceResponse
import com.bybutter.sisyphus.starter.grpc.support.reflection.v1alpha.ServerReflection
import com.bybutter.sisyphus.starter.grpc.support.reflection.v1alpha.ServerReflectionRequest
import com.bybutter.sisyphus.starter.grpc.support.reflection.v1alpha.ServerReflectionResponse
import com.bybutter.sisyphus.starter.grpc.support.reflection.v1alpha.ServiceResponse
import io.grpc.InternalNotifyOnServerBuild
import io.grpc.Server
import io.grpc.ServerServiceDefinition
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

@OptIn(ExperimentalCoroutinesApi::class)
class ReflectionServiceAlpha : ServerReflection(), InternalNotifyOnServerBuild {
    private lateinit var services: List<ServerServiceDefinition>

    override fun notifyOnBuild(server: Server) {
        services = server.services
    }

    override fun serverReflectionInfo(input: Flow<ServerReflectionRequest>): Flow<ServerReflectionResponse> = flow {
        input.collect { request ->
            val response = ServerReflectionResponse {
                validHost = request.host
                originalRequest = request

                when (val messageRequest = request.messageRequest) {
                    is ServerReflectionRequest.MessageRequest.FileByFilename -> {
                        messageResponse = getFileByFileName(messageRequest.value)
                    }
                    is ServerReflectionRequest.MessageRequest.FileContainingSymbol -> {
                        messageResponse = getFileContainingSymbol(messageRequest.value)
                    }
                    is ServerReflectionRequest.MessageRequest.FileContainingExtension -> {
                        messageResponse = getFileContainingExtension(messageRequest.value)
                    }
                    is ServerReflectionRequest.MessageRequest.AllExtensionNumbersOfType -> {
                        messageResponse = getAllExtensionNumbersOfType(messageRequest.value)
                    }
                    is ServerReflectionRequest.MessageRequest.ListServices -> {
                        messageResponse = listService(messageRequest.value)
                    }
                }
            }

            emit(response)
        }
    }

    private fun getFileByFileName(name: String): ServerReflectionResponse.MessageResponse.FileDescriptorResponse {
        return ServerReflectionResponse.MessageResponse.FileDescriptorResponse(FileDescriptorResponse {
            ProtoTypes.getFileDescriptorByName(name)?.toProto()?.let {
                this.fileDescriptorProto += it
            }
        })
    }

    private fun getFileContainingSymbol(name: String): ServerReflectionResponse.MessageResponse.FileDescriptorResponse {
        return ServerReflectionResponse.MessageResponse.FileDescriptorResponse(FileDescriptorResponse {
            ProtoTypes.getFileContainingSymbol(name)?.toProto()?.let {
                this.fileDescriptorProto += it
            }
        })
    }

    private fun getFileContainingExtension(request: ExtensionRequest): ServerReflectionResponse.MessageResponse.FileDescriptorResponse {
        return ServerReflectionResponse.MessageResponse.FileDescriptorResponse(FileDescriptorResponse {
            ProtoTypes.getFileContainingExtension(request.containingType, request.extensionNumber)?.toProto()?.let {
                this.fileDescriptorProto += it
            }
        })
    }

    private fun getAllExtensionNumbersOfType(name: String): ServerReflectionResponse.MessageResponse.AllExtensionNumbersResponse {
        return ServerReflectionResponse.MessageResponse.AllExtensionNumbersResponse(ExtensionNumberResponse {
            this.baseTypeName = name
            this.extensionNumber += ProtoTypes.getTypeExtensions(name)
        })
    }

    private fun listService(name: String): ServerReflectionResponse.MessageResponse.ListServicesResponse {
        return ServerReflectionResponse.MessageResponse.ListServicesResponse(ListServiceResponse {
            this.service += services.map {
                ServiceResponse {
                    this.name = it.serviceDescriptor.name
                }
            }
        })
    }
}
