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
        return url.substringAfterLast("/")
    }

    fun getTypeUrlByProtoName(name: String, host: String = "type.bybutter.com"): String {
        return "$host/$name"
    }

    fun findSupport(name: String): ProtoSupport<*>? {
        if (name.contains('/')) {
            return protoToSupportMap[getProtoNameByTypeUrl(name)]
        }
        return protoToSupportMap[name.trim('.')]
    }
}
