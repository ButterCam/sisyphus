package com.bybutter.sisyphus.protobuf

import java.util.concurrent.ConcurrentHashMap

open class LocalProtoReflection : ProtoReflection {
    private val protoToSupportMap: MutableMap<String, ProtoSupport<*>> = ConcurrentHashMap()

    override fun register(support: ProtoSupport<*>) {
        when (support) {
            is ExtensionSupport<*> -> support.extendee.registerExtension(support)
        }
        protoToSupportMap[support.name] = support
        for (child in support.children()) {
            register(child)
        }
    }

    override fun findSupport(name: String): ProtoSupport<*>? {
        if (name.contains('/')) {
            return protoToSupportMap[".${name.substringAfterLast("/")}"] ?: protoToSupportMap[name]
        }
        return protoToSupportMap[name]
    }

    override fun files(): List<FileSupport> {
        return protoToSupportMap.values.filterIsInstance<FileSupport>()
    }

    override fun message(): List<MessageSupport<*, *>> {
        return protoToSupportMap.values.filterIsInstance<MessageSupport<*, *>>()
    }

    override fun enums(): List<EnumSupport<*>> {
        return protoToSupportMap.values.filterIsInstance<EnumSupport<*>>()
    }

    override fun services(): List<ServiceSupport> {
        return protoToSupportMap.values.filterIsInstance<ServiceSupport>()
    }

    override fun extensions(): List<ExtensionSupport<*>> {
        return protoToSupportMap.values.filterIsInstance<ExtensionSupport<*>>()
    }
}
