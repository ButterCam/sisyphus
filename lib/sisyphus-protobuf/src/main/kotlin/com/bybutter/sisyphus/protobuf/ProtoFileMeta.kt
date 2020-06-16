package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.io.replaceExtensionName
import com.bybutter.sisyphus.protobuf.primitives.FileDescriptorProto

interface ProtoFileMeta {
    val name: String
    val descriptor: FileDescriptorProto

    fun readDescriptor(): FileDescriptorProto {
        val descriptorFile = name.replaceExtensionName("proto", "pb")
        this.javaClass.classLoader.getResourceAsStream(descriptorFile)?.use {
            return FileDescriptorProto.parse(it)
        } ?: throw IllegalStateException("Proto file descriptor '$descriptorFile' not found, check your classpath or recompile proto files.")
    }
}
