package com.bybutter.sisyphus.protobuf.compiler.resourcename

import com.bybutter.sisyphus.protobuf.compiler.DescriptorNode
import com.bybutter.sisyphus.protobuf.compiler.DescriptorResolver
import com.bybutter.sisyphus.protobuf.compiler.FileDescriptor
import com.bybutter.sisyphus.protobuf.compiler.MessageDescriptor
import com.google.api.ResourceProto
import com.google.protobuf.DescriptorProtos

class ResourceDescriptorResolver : DescriptorResolver {
    override fun resolve(descriptor: DescriptorNode<*>, children: MutableList<DescriptorNode<*>>) {
        when (descriptor) {
            is FileDescriptor -> {
                val options = DescriptorProtos.FileOptions.parseFrom(
                    descriptor.descriptor.options.toByteArray(),
                    ResourceFields.extensionRegistry
                )
                val resourceDefinition = options.getExtension(ResourceProto.resourceDefinition)
                children += resourceDefinition.map {
                    ResourceDescriptor(descriptor, it)
                }
            }
            is MessageDescriptor -> {
                val options = DescriptorProtos.MessageOptions.parseFrom(
                    descriptor.descriptor.options.toByteArray(),
                    ResourceFields.extensionRegistry
                )
                val resource = options.getExtension(ResourceProto.resource)
                if (resource.type.isNotEmpty()) {
                    children += ResourceDescriptor(descriptor, resource)
                }
            }
        }
    }
}

val FileDescriptor.resources get() = children().filterIsInstance<ResourceDescriptor>()

val MessageDescriptor.resource: ResourceDescriptor?
    get() {
        for (child in children()) {
            if (child is ResourceDescriptor) return child
        }
        return null
    }