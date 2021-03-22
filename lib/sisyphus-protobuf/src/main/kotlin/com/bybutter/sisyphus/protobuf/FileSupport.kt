package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.protobuf.primitives.FileDescriptorProto

abstract class FileSupport : ProtoSupport<FileDescriptorProto> {
    override val parent: ProtoSupport<*>
        get() = this

    fun readDescriptor(fileName: String): FileDescriptorProto {
        this.javaClass.classLoader.getResourceAsStream(fileName)?.use {
            return FileDescriptorProto.parse(it, it.available())
        }
            ?: throw IllegalStateException("Proto file descriptor '$fileName' not found, check your classpath or recompile proto files.")
    }
}
