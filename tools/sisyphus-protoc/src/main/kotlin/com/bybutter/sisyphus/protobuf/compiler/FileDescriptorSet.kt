package com.bybutter.sisyphus.protobuf.compiler

import com.google.protobuf.DescriptorProtos

class FileDescriptorSet(
    override val descriptor: DescriptorProtos.FileDescriptorSet,
    private val packageShading: Map<String, String>
) : DescriptorNode<DescriptorProtos.FileDescriptorSet> {
    val children: List<FileDescriptor> = descriptor.fileList.map {
        FileDescriptor(this, it)
    }

    fun shadePackage(packageName: String): String {
        return packageShading[packageName] ?: packageName
    }

    fun ensureMessage(proto: String): MessageDescriptor {
        return findMessage(proto) ?: throw IllegalStateException("Definition of message '$proto' not found")
    }

    fun ensureEnum(proto: String): EnumDescriptor {
        return findEnum(proto) ?: throw IllegalStateException("Definition of enum '$proto' not found")
    }

    fun ensureDescriptor(proto: String): DescriptorNode<*> {
        return findDescriptor(proto) ?: throw IllegalStateException("Descriptor of enum '$proto' not found")
    }

    fun findMessage(proto: String): MessageDescriptor? {
        return findDescriptor(proto) as? MessageDescriptor
    }

    fun findEnum(proto: String): EnumDescriptor? {
        return findDescriptor(proto) as? EnumDescriptor
    }

    fun findDescriptor(proto: String): DescriptorNode<*>? {
        val normalized = if (proto.startsWith('.')) {
            proto
        } else {
            ".$proto"
        }

        for (child in children) {
            val packageName = ".${child.descriptor.`package`}"
            if (!normalized.startsWith(packageName)) continue
            val subNames = normalized.substringAfter(packageName)
            if (subNames.isNotEmpty() && !subNames.startsWith('.')) continue
            val part = listOf(child.descriptor.`package`) + subNames.substring(1).split('.')
            return child.findDescriptor(part, 0) ?: continue
        }

        return null
    }

    private fun FileDescriptor.findDescriptor(namePart: List<String>, index: Int): DescriptorNode<*>? {
        if (this.descriptor.`package` != namePart[index]) return null

        if (index + 1 == namePart.size) return this

        for (message in messages) {
            return message.findDescriptor(namePart, index + 1) ?: continue
        }
        for (enum in enums) {
            return enum.findDescriptor(namePart, index + 1) ?: continue
        }
        for (service in services) {
            return service.findDescriptor(namePart, index + 1) ?: continue
        }
        for (extension in extensions) {
            if (extension.descriptor.name == namePart[index + 1] && namePart.size == index + 2) {
                return extension
            }
        }
        return null
    }

    private fun MessageDescriptor.findDescriptor(namePart: List<String>, index: Int): DescriptorNode<*>? {
        if (this.descriptor.name != namePart[index]) return null

        if (index + 1 == namePart.size) return this

        for (message in messages) {
            return message.findDescriptor(namePart, index + 1) ?: continue
        }
        for (enum in enums) {
            return enum.findDescriptor(namePart, index + 1) ?: continue
        }
        for (field in fields) {
            if (field.descriptor.name == namePart[index + 1] && namePart.size == index + 2) {
                return field
            }
        }
        for (oneof in oneofs) {
            if (oneof.descriptor.name == namePart[index + 1] && namePart.size == index + 2) {
                return oneof
            }
        }
        return null
    }

    private fun EnumDescriptor.findDescriptor(namePart: List<String>, index: Int): DescriptorNode<*>? {
        if (this.descriptor.name != namePart[index]) return null

        if (index + 1 == namePart.size) return this

        for (value in values) {
            if (value.descriptor.name == namePart[index + 1] && namePart.size == index + 2) {
                return value
            }
        }
        return null
    }

    private fun ServiceDescriptor.findDescriptor(namePart: List<String>, index: Int): DescriptorNode<*>? {
        if (this.descriptor.name != namePart[index]) return null

        if (index + 1 == namePart.size) return this

        for (method in methods) {
            if (method.descriptor.name == namePart[index + 1] && namePart.size == index + 2) {
                return method
            }
        }
        return null
    }
}
