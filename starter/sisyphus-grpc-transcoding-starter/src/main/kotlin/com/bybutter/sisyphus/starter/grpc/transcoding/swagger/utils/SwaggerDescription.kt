package com.bybutter.sisyphus.starter.grpc.transcoding.swagger.utils

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.primitives.FileDescriptorProto

object SwaggerDescription {
    /**
     *  Get the corresponding proto file comment according to path.
     * */
    fun fetchDescription(path: List<Int?>, fileDescriptor: FileDescriptorProto?): String? {
        val location = fileDescriptor?.sourceCodeInfo?.location?.firstOrNull { location ->
            location.path.contentEquals(path)
        }
        location ?: return null
        return listOf(location.leadingComments, location.trailingComments).filter { it.isNotBlank() }.joinToString("\n\n")
    }
}
