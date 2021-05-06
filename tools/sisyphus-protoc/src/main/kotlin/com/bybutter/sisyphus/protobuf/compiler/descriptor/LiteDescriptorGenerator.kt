package com.bybutter.sisyphus.protobuf.compiler.descriptor

import com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.state.DescriptorGeneratingState
import com.google.protobuf.DescriptorProtos

class LiteDescriptorGenerator : GroupedGenerator<DescriptorGeneratingState> {
    override fun generate(state: DescriptorGeneratingState): Boolean {
        state.target.clearSourceCodeInfo()
        state.target.clearOptions()
        return true
    }

    private fun handle(file: DescriptorProtos.FileDescriptorProto.Builder) {
        file.clearDependency()
        file.clearPublicDependency()
        file.clearWeakDependency()
        file.messageTypeBuilderList.forEach {
            handle(it)
        }
        file.enumTypeBuilderList.forEach {
            handle(it)
        }
        file.serviceBuilderList.forEach {
            handle(it)
        }
        file.extensionBuilderList.forEach {
            handle(it)
        }
        file.clearOptions()
        file.clearSourceCodeInfo()
    }

    private fun handle(message: DescriptorProtos.DescriptorProto.Builder) {
        message.fieldBuilderList.forEach {
            handle(it)
        }
        message.extensionBuilderList.forEach {
            handle(it)
        }
        message.nestedTypeBuilderList.forEach {
            handle(it)
        }
        message.enumTypeBuilderList.forEach {
            handle(it)
        }
        message.oneofDeclBuilderList.forEach {
            handle(it)
        }
        message.clearExtensionRange()
        message.clearReservedRange()
        message.clearReservedName()
        message.clearOptions()
    }

    private fun handle(field: DescriptorProtos.FieldDescriptorProto.Builder) {
        field.clearDefaultValue()
        field.clearOptions()
    }

    private fun handle(oneof: DescriptorProtos.OneofDescriptorProto.Builder) {
        oneof.clearOptions()
    }

    private fun handle(enum: DescriptorProtos.EnumDescriptorProto.Builder) {
        enum.valueBuilderList.forEach {
            handle(it)
        }
        enum.clearOptions()
        enum.clearReservedRange()
        enum.clearReservedName()
    }

    private fun handle(enumValue: DescriptorProtos.EnumValueDescriptorProto.Builder) {
        enumValue.clearOptions()
    }

    private fun handle(service: DescriptorProtos.ServiceDescriptorProto.Builder) {
        service.methodBuilderList.forEach {
            handle(it)
        }
        service.clearOptions()
    }

    private fun handle(method: DescriptorProtos.MethodDescriptorProto.Builder) {
        method.clearOptions()
    }
}
