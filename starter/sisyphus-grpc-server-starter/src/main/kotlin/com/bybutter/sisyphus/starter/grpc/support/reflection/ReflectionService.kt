package com.bybutter.sisyphus.starter.grpc.support.reflection

import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.findFileSupport
import com.bybutter.sisyphus.protobuf.findMessageSupport
import com.bybutter.sisyphus.starter.grpc.support.reflection.v1.ExtensionNumberResponse
import com.bybutter.sisyphus.starter.grpc.support.reflection.v1.ExtensionRequest
import com.bybutter.sisyphus.starter.grpc.support.reflection.v1.FileDescriptorResponse
import com.bybutter.sisyphus.starter.grpc.support.reflection.v1.ListServiceResponse
import com.bybutter.sisyphus.starter.grpc.support.reflection.v1.ServerReflection
import com.bybutter.sisyphus.starter.grpc.support.reflection.v1.ServerReflectionRequest
import com.bybutter.sisyphus.starter.grpc.support.reflection.v1.ServerReflectionResponse
import com.bybutter.sisyphus.starter.grpc.support.reflection.v1.ServiceResponse
import io.grpc.InternalServer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

@OptIn(ExperimentalCoroutinesApi::class)
class ReflectionService : ServerReflection() {
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

                    null -> {}
                }
            }

            emit(response)
        }
    }

    private fun getFileByFileName(name: String): ServerReflectionResponse.MessageResponse.FileDescriptorResponse {
        return ServerReflectionResponse.MessageResponse.FileDescriptorResponse(
            FileDescriptorResponse {
                this.fileDescriptorProto += ProtoTypes.findFileSupport(name).descriptor.toProto()
            }
        )
    }

    private fun getFileContainingSymbol(name: String): ServerReflectionResponse.MessageResponse.FileDescriptorResponse {
        return ServerReflectionResponse.MessageResponse.FileDescriptorResponse(
            FileDescriptorResponse {
                ProtoTypes.findSupport(".$name")?.file()?.let {
                    this.fileDescriptorProto += it.descriptor.toProto()
                }
            }
        )
    }

    private fun getFileContainingExtension(request: ExtensionRequest): ServerReflectionResponse.MessageResponse.FileDescriptorResponse {
        return ServerReflectionResponse.MessageResponse.FileDescriptorResponse(
            FileDescriptorResponse {
                val message = ProtoTypes.findMessageSupport(".${request.containingType}")
                val extension = message.extensions.firstOrNull {
                    it.descriptor.number == request.extensionNumber
                }

                extension?.file()?.let {
                    this.fileDescriptorProto += it.descriptor.toProto()
                }
            }
        )
    }

    private fun getAllExtensionNumbersOfType(name: String): ServerReflectionResponse.MessageResponse.AllExtensionNumbersResponse {
        return ServerReflectionResponse.MessageResponse.AllExtensionNumbersResponse(
            ExtensionNumberResponse {
                this.baseTypeName = name
                val message = ProtoTypes.findMessageSupport(".$name")
                this.extensionNumber += message.extensions.map { it.descriptor.number }
            }
        )
    }

    private fun listService(name: String): ServerReflectionResponse.MessageResponse.ListServicesResponse {
        return ServerReflectionResponse.MessageResponse.ListServicesResponse(
            ListServiceResponse {
                this.service += InternalServer.SERVER_CONTEXT_KEY.get().services.map {
                    ServiceResponse {
                        this.name = it.serviceDescriptor.name
                    }
                }
            }
        )
    }
}
