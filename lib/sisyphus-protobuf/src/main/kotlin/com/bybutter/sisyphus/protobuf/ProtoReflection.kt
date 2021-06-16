package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.protobuf.primitives.DescriptorProto

interface ProtoReflection {
    fun register(support: ProtoSupport<*>)

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

    companion object : ProtoReflection {
        fun current(): ProtoReflection {
            return threadStore.get() ?: ProtoTypes
        }

        override fun register(support: ProtoSupport<*>) {
            current().register(support)
        }

        override fun files(): List<FileSupport> {
            return current().files()
        }

        override fun message(): List<MessageSupport<*, *>> {
            return current().message()
        }

        override fun enums(): List<EnumSupport<*>> {
            return current().enums()
        }

        override fun services(): List<ServiceSupport> {
            return current().services()
        }

        override fun extensions(): List<ExtensionSupport<*>> {
            return current().extensions()
        }

        override fun findSupport(name: String): ProtoSupport<*>? {
            return current().findSupport(name)
        }
    }
}

private val threadStore = ThreadLocal<ProtoReflection>()

operator fun <T> ProtoReflection.invoke(block: () -> T): T {
    if (this == ProtoReflection) return block()
    val old = threadStore.get()
    threadStore.set(this)
    val result = block()
    threadStore.set(old)
    return result
}

fun ProtoReflection.findFileSupport(name: String): FileSupport {
    return findSupport(name) as? FileSupport
        ?: throw IllegalStateException("Can't find proto file named '$name'")
}

fun ProtoReflection.findMessageSupport(name: String): MessageSupport<*, *> {
    return findSupport(name) as? MessageSupport<*, *>
        ?: throw IllegalStateException("Can't find protobuf message named '$name'")
}

fun ProtoReflection.findEnumSupport(name: String): EnumSupport<*> {
    return findSupport(name) as? EnumSupport<*>
        ?: throw IllegalStateException("Can't find protobuf enum named '$name'")
}

fun ProtoReflection.findServiceSupport(name: String): ServiceSupport {
    return findSupport(name) as? ServiceSupport
        ?: throw IllegalStateException("Can't find gRPC service named '$name'")
}

fun ProtoReflection.findMapEntryDescriptor(name: String): DescriptorProto? {
    val parentMessage = name.substringBeforeLast('.')
    val entryName = name.substringAfterLast('.')
    val messageSupport = findSupport(parentMessage) as? MessageSupport<*, *> ?: return null
    return messageSupport.descriptor.nestedType.firstOrNull {
        it.name == entryName && it.options?.mapEntry == true
    }
}
