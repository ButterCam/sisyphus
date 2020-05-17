package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.primitives.EnumValueOptions
import com.bybutter.sisyphus.protobuf.string
import com.bybutter.sisyphus.string.toScreamingSnakeCase
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.TypeSpec

class EnumValueGenerator(override val parent: EnumGenerator, val descriptor: DescriptorProtos.EnumValueDescriptorProto) : ProtobufElement() {
    override val kotlinName: String = descriptor.name.substringAfter("${parent.kotlinName.toScreamingSnakeCase()}_")
    override val protoName: String = descriptor.name

    val stringValue: String = EnumValueOptions.parse(descriptor.options.toByteArray()).string

    var path: List<Int> = listOf()
        private set

    override val documentation: String by lazy {
        val location = ensureParent<FileGenerator>().descriptor.sourceCodeInfo.locationList.firstOrNull {
            it.pathList.contentEquals(path)
        } ?: return@lazy ""

        listOf(location.leadingComments, location.trailingComments).filter { it.isNotBlank() }.joinToString("\n\n")
    }

    override fun init() {
        super.init()
        val parent = parent

        path = parent.path + listOf(DescriptorProtos.EnumDescriptorProto.VALUE_FIELD_NUMBER, parent.descriptor.valueList.indexOf(descriptor))
    }

    fun generate(): TypeSpec {
        return TypeSpec.anonymousClassBuilder()
            .addKdoc(escapeDoc(documentation))
            .addSuperclassConstructorParameter("%L", descriptor.number)
            .addSuperclassConstructorParameter("%S", protoName)
            .apply {
                if (stringValue.isNotEmpty() && parent.isStringEnum) {
                    addSuperclassConstructorParameter("%S", stringValue)
                }
            }
            .build()
    }
}
