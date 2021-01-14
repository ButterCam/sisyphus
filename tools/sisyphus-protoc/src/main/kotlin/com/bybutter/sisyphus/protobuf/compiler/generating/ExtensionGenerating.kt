package com.bybutter.sisyphus.protobuf.compiler.generating

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.compiler.util.escapeDoc
import com.bybutter.sisyphus.string.toPascalCase
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member

fun ExtensionFieldGenerating<*, *>.supportName(): String {
    val descriptor = compiler().protoDescriptor(descriptor.extendee)
    return "${this.descriptor.jsonName.toPascalCase()}ExtensionSupportFor${descriptor.name}"
}

fun ExtensionFieldGenerating<*, *>.supportClassName(): ClassName {
    return when (val parent = parent) {
        is FileGenerating<*, *> -> ClassName(parent.internalPackageName(), supportName())
        is MessageGenerating<*, *> -> parent.supportClassName().nestedClass(supportName())
        else -> TODO()
    }
}

fun ExtensionFieldGenerating<*, *>.propertyMemberName(): MemberName {
    return when (val parent = parent) {
        is FileGenerating<*, *> -> MemberName(parent.packageName(), descriptor.jsonName)
        is MessageGenerating<*, *> -> parent.className().member(descriptor.jsonName)
        else -> TODO()
    }
}

fun ExtensionFieldGenerating<*, *>.extendeeClassName(): ClassName {
    return compiler().protoClassName(descriptor.extendee)
}

fun ExtensionFieldGenerating<*, *>.extendeeMutableClassName(): ClassName {
    return compiler().protoMutableClassName(descriptor.extendee)
}

fun ExtensionFieldGenerating<*, *>.document(): String {
    return escapeDoc(file().descriptor.sourceCodeInfo?.locationList?.firstOrNull {
        it.pathList.contentEquals(path())
    }?.leadingComments ?: "")
}

fun ExtensionFieldGenerating<*, *>.path(): List<Int> {
    return when (val parent = parent) {
        is FileGenerating<*, *> -> listOf<Int>() + DescriptorProtos.FileDescriptorProto.EXTENSION_FIELD_NUMBER + parent.descriptor.extensionList.indexOf(
            descriptor
        )
        is MessageGenerating<*, *> -> parent.path() + DescriptorProtos.DescriptorProto.EXTENSION_FIELD_NUMBER + parent.descriptor.extensionList.indexOf(
            descriptor
        )
        else -> TODO()
    }
}