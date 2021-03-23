package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.spi.ServiceLoader

object ProtoTypes {
    private val protoToSupportMap: MutableMap<String, ProtoSupport<*>> = hashMapOf()

    init {
        val supports = ServiceLoader.load(FileSupport::class.java)
        for (support in supports) {
            register(support)
        }
    }

    fun register(support: MessageSupport<*, *>) {
        support.register()
        protoToSupportMap[support.name] = support
    }

    fun register(support: EnumSupport<*>) {
        support.register()
        protoToSupportMap[support.name] = support
    }

    fun register(support: ExtensionSupport<*>) {
        support.register()
        support.extendee.registerExtension(support)
        protoToSupportMap[support.name] = support
    }

    fun register(support: ServiceSupport) {
        support.register()
        protoToSupportMap[support.name] = support
    }

    fun register(support: FileSupport) {
        support.register()
        protoToSupportMap[support.name] = support
    }

    fun getProtoNameByTypeUrl(url: String): String {
        return ".${url.substringAfterLast("/")}"
    }

    fun getTypeUrlByProtoName(name: String, host: String = "type.bybutter.com"): String {
        if (name.startsWith(".")) {
            return "$host/${name.substring(1)}"
        }
        return "$host/$name"
    }

    /**
     * Find protobuf support for file, enum, message and service.
     * 'name' must be full proto name begin with dot like
     * '.google.protobuf.Any' or type url like
     * 'types.googleapis.com/google.protobuf.Any' or file name like
     * 'google/api/annotation.proto'.
     */
    fun findSupport(name: String): ProtoSupport<*>? {
        if (name.contains('/')) {
            return protoToSupportMap[getProtoNameByTypeUrl(name)] ?: protoToSupportMap[name]
        }
        return protoToSupportMap[name]
    }

    fun findFileSupport(name: String): FileSupport {
        return findSupport(name) as? FileSupport ?: throw IllegalStateException("Can't find proto file named '$name'")
    }

    fun findMessageSupport(name: String): MessageSupport<*, *> {
        return findSupport(name) as? MessageSupport<*, *> ?: throw IllegalStateException("Can't find protobuf message named '$name'")
    }

    fun findEnumSupport(name: String): EnumSupport<*> {
        return findSupport(name) as? EnumSupport<*> ?: throw IllegalStateException("Can't find protobuf enum named '$name'")
    }

    fun findServiceSupport(name: String): ServiceSupport {
        return findSupport(name) as? ServiceSupport ?: throw IllegalStateException("Can't find gRPC service named '$name'")
    }

    fun services(): List<ServiceSupport> {
        return protoToSupportMap.values.filterIsInstance<ServiceSupport>()
    }
}
