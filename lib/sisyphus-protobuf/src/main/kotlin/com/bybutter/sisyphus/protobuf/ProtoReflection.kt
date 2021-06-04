package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.protobuf.primitives.DescriptorProto

interface ProtoReflection {
    fun files(): List<FileSupport>

    fun message(): List<MessageSupport<*, *>>

    fun enums(): List<EnumSupport<*>>

    fun services(): List<ServiceSupport>

    fun extensions(): List<ExtensionSupport<*>>

    /**
     * Find protobuf support for file, enum, message and service.
     * 'name' must be full proto name begin with dot like
     * '.google.protobuf.Any' or type url like
     * 'types.googleapis.com/google.protobuf.Any' or file name like
     * 'google/api/annotation.proto'.
     */
    fun findSupport(name: String): ProtoSupport<*>?

    companion object {
        fun getProtoNameByTypeUrl(url: String): String {
            return ".${url.substringAfterLast("/")}"
        }

        fun getTypeUrlByProtoName(name: String, host: String = "type.bybutter.com"): String {
            if (name.startsWith(".")) {
                return "$host/${name.substring(1)}"
            }
            return "$host/$name"
        }
    }
}

fun ProtoReflection.findFileSupport(name: String): FileSupport {
    return ProtoTypes.findSupport(name) as? FileSupport
        ?: throw IllegalStateException("Can't find proto file named '$name'")
}

fun ProtoReflection.findMessageSupport(name: String): MessageSupport<*, *> {
    return ProtoTypes.findSupport(name) as? MessageSupport<*, *>
        ?: throw IllegalStateException("Can't find protobuf message named '$name'")
}

fun ProtoReflection.findEnumSupport(name: String): EnumSupport<*> {
    return ProtoTypes.findSupport(name) as? EnumSupport<*>
        ?: throw IllegalStateException("Can't find protobuf enum named '$name'")
}

fun ProtoReflection.findServiceSupport(name: String): ServiceSupport {
    return ProtoTypes.findSupport(name) as? ServiceSupport
        ?: throw IllegalStateException("Can't find gRPC service named '$name'")
}

fun ProtoReflection.findMapEntryDescriptor(name: String): DescriptorProto? {
    val parentMessage = name.substringBeforeLast('.')
    val entryName = name.substringAfterLast('.')
    val messageSupport = ProtoTypes.findSupport(parentMessage) as? MessageSupport<*, *> ?: return null
    return messageSupport.descriptor.nestedType.firstOrNull {
        it.name == entryName && it.options?.mapEntry == true
    }
}
