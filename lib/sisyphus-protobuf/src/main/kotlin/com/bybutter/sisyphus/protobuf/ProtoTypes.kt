package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.protobuf.primitives.DescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.EnumDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.FileDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.MethodDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.ServiceDescriptorProto
import com.bybutter.sisyphus.spi.ServiceLoader
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

object ProtoTypes {
    private val protoToClassMap: BiMap<String, Class<*>> = HashBiMap.create()
    private val protoToServiceMap: BiMap<String, Class<*>> = HashBiMap.create()

    // HashMap is faster than mutableMapOf(LinkedHashMap)
    private val fileInfoMap: MutableMap<String, FileDescriptorProto> = hashMapOf()
    private val symbolMap: MutableMap<String, DescriptorInfo> = hashMapOf()
    private val extensionMap: MutableMap<String, MutableMap<Int, DescriptorInfo>> = hashMapOf()

    init {
        ServiceLoader.load(ProtoFileMeta::class.java)
    }

    fun registerFileMeta(file: ProtoFileMeta) {
        registerFileSymbol(file.descriptor)
    }

    fun registerProtoType(protoType: String, kotlinType: Class<*>) {
        protoToClassMap[protoType] = kotlinType
    }

    fun registerProtoType(protoType: String, kotlinType: KClass<*>) {
        protoToClassMap[protoType] = kotlinType.java
    }

    fun registerService(protoType: String, kotlinType: Class<*>) {
        protoToServiceMap[protoType] = kotlinType
    }

    fun registerService(protoType: String, kotlinType: KClass<*>) {
        protoToServiceMap[protoType] = kotlinType.java
    }

    private fun registerFileSymbol(file: FileDescriptorProto) {
        val packageName = file.`package`
        fileInfoMap[file.name] = file

        for (descriptor in file.messageType) {
            registerSymbol(file, packageName, descriptor)
        }

        for (descriptor in file.enumType) {
            registerSymbol(file, packageName, descriptor)
        }

        for (descriptor in file.service) {
            registerSymbol(file, packageName, descriptor)
        }

        for (descriptor in file.extension) {
            registerExtension(file, descriptor)
        }
    }

    private fun registerSymbol(file: FileDescriptorProto, prefix: String, descriptor: DescriptorProto) {
        val prefix = "$prefix.${descriptor.name}"
        symbolMap[prefix] = DescriptorInfo(file, descriptor)

        for (descriptor in descriptor.nestedType) {
            registerSymbol(file, prefix, descriptor)
        }

        for (descriptor in descriptor.enumType) {
            registerSymbol(file, prefix, descriptor)
        }

        for (descriptor in file.extension) {
            registerExtension(file, descriptor)
        }
    }

    private fun registerSymbol(file: FileDescriptorProto, prefix: String, descriptor: EnumDescriptorProto) {
        val prefix = "$prefix.${descriptor.name}"
        symbolMap[prefix] = DescriptorInfo(file, descriptor)
    }

    private fun registerSymbol(file: FileDescriptorProto, prefix: String, descriptor: ServiceDescriptorProto) {
        val prefix = "$prefix.${descriptor.name}"
        symbolMap[prefix] = DescriptorInfo(file, descriptor)

        for (descriptor in descriptor.method) {
            registerSymbol(file, prefix, descriptor)
        }
    }

    private fun registerSymbol(file: FileDescriptorProto, prefix: String, descriptor: MethodDescriptorProto) {
        val info = DescriptorInfo(file, descriptor)
        symbolMap["$prefix/${descriptor.name}"] = info
        symbolMap["$prefix.${descriptor.name}"] = info
    }

    private fun registerExtension(file: FileDescriptorProto, descriptor: FieldDescriptorProto) {
        val map = extensionMap.getOrPut(descriptor.extendee.trim('.')) {
            mutableMapOf()
        }

        map[descriptor.number] = DescriptorInfo(file, descriptor)
    }

    fun getProtoNameByTypeUrl(url: String): String {
        return url.substringAfter("/")
    }

    fun getTypeUrlByProtoName(name: String, host: String = "type.bybutter.com"): String {
        return "$host/$name"
    }

    fun getClassByProtoName(name: String): Class<*>? {
        return protoToClassMap[name.trim('.')]
    }

    fun ensureClassByProtoName(name: String): Class<*> {
        return getClassByProtoName(name)
            ?: throw UnsupportedOperationException("Message '$name' not defined in current context.")
    }

    fun getClassByTypeUrl(url: String): Class<*>? {
        return getClassByProtoName(getProtoNameByTypeUrl(url))
    }

    fun ensureClassByTypeUrl(url: String): Class<*> {
        return getClassByTypeUrl(url)
            ?: throw UnsupportedOperationException("Message '$url' not defined in current context.")
    }

    fun getSupportByProtoName(name: String): ProtoSupport<*, *>? {
        return getClassByProtoName(name)?.kotlin?.companionObjectInstance as? ProtoSupport<*, *>
    }

    fun ensureSupportByProtoName(name: String): ProtoSupport<*, *> {
        return getSupportByProtoName(name)
            ?: throw UnsupportedOperationException("Message '$name' not defined in current context.")
    }

    fun getSupportByTypeUrl(url: String): ProtoSupport<*, *>? {
        return getSupportByProtoName(getProtoNameByTypeUrl(url))
    }

    fun ensureSupportByTypeUrl(url: String): ProtoSupport<*, *> {
        return getSupportByTypeUrl(url)
            ?: throw UnsupportedOperationException("Message '$url' not defined in current context.")
    }

    fun getProtoNameByClass(clazz: Class<*>): String? {
        return protoToClassMap.inverse()[clazz]
    }

    fun getDescriptorByProtoName(name: String): Any? {
        return symbolMap[name.trim('.')]?.descriptor
    }

    fun getDescriptorByTypeUrl(url: String): Any? {
        return getDescriptorByProtoName(getProtoNameByTypeUrl(url))
    }

    fun getDescriptorByClass(clazz: Class<*>): Any? {
        return getDescriptorByProtoName(getProtoNameByClass(clazz)!!)
    }

    fun getFileDescriptorByName(name: String): FileDescriptorProto? {
        return fileInfoMap[name]
    }

    fun getFileContainingSymbol(name: String): FileDescriptorProto? {
        return symbolMap[name.trim('.')]?.file
    }

    fun getDescriptorBySymbol(name: String): Any? {
        return symbolMap[name.trim('.')]?.descriptor
    }

    fun getFileContainingExtension(name: String, number: Int): FileDescriptorProto? {
        val extensions = extensionMap[name.trim('.')] ?: mutableMapOf()
        return extensions[number]?.file
    }

    fun getExtensionDescriptor(name: String, number: Int): FieldDescriptorProto? {
        val extensions = extensionMap[name.trim('.')] ?: mutableMapOf()
        return extensions[number]?.descriptor as? FieldDescriptorProto
    }

    fun getTypeExtensions(name: String): Set<Int> {
        val extensions = extensionMap[name.trim('.')] ?: mutableMapOf()
        return extensions.keys
    }

    fun getRegisteredServices(): Set<String> {
        return symbolMap.mapNotNull { (key, value) ->
            if(value.descriptor is ServiceDescriptorProto){
                key
            }else {
                null
            }
        }.toSet()
    }

    fun getProtoToServiceMap(key: String):Class<*>?{
        return protoToServiceMap[key]
    }
}

private data class DescriptorInfo(val file: FileDescriptorProto, val descriptor: Any)
