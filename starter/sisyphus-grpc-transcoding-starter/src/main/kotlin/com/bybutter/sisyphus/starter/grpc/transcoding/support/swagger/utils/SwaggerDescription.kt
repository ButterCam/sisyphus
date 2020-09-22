package com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.utils

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.primitives.FileDescriptorProto
import java.util.regex.Pattern

object SwaggerDescription {

    private val pattern: Pattern by lazy {
        Pattern.compile("""(\(-- api-linter:.[\s\S]+? --\))""")
    }
    /**
     *  Get the corresponding proto file comment according to path.
     * */
    fun fetchDescription(path: List<Int?>, fileDescriptor: FileDescriptorProto?): String? {
        val location = fileDescriptor?.sourceCodeInfo?.location?.firstOrNull { location ->
            location.path.contentEquals(path)
        }
        location ?: return null
        return pattern.matcher(listOf(location.leadingComments, location.trailingComments).filter { it.isNotBlank() }.joinToString("\n\n")).replaceAll("").trim()
    }
}
