package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.EnumSupport
import com.bybutter.sisyphus.protobuf.ProtoEnum
import com.bybutter.sisyphus.protobuf.ProtoEnumDsl
import com.bybutter.sisyphus.protobuf.ProtoStringEnum
import com.bybutter.sisyphus.protobuf.ProtoStringEnumDsl
import com.bybutter.sisyphus.protobuf.primitives.EnumDescriptorProto
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock

class EnumGenerator(override val parent: ProtobufElement, val descriptor: DescriptorProtos.EnumDescriptorProto) : ProtobufElement() {
    override val kotlinName: String = descriptor.name
    override val protoName: String = descriptor.name

    val kotlinType get() = ClassName.bestGuess(fullKotlinName)

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

        path = when (parent) {
            is MessageGenerator -> {
                parent.path + listOf(DescriptorProtos.DescriptorProto.ENUM_TYPE_FIELD_NUMBER, parent.descriptor.enumTypeList.indexOf(descriptor))
            }
            is FileGenerator -> {
                listOf(DescriptorProtos.FileDescriptorProto.ENUM_TYPE_FIELD_NUMBER, parent.descriptor.enumTypeList.indexOf(descriptor))
            }
            else -> throw IllegalStateException("Enum must be a child of message or file.")
        }

        for (value in descriptor.valueList) {
            addElement(EnumValueGenerator(this, value))
        }
    }

    val isStringEnum: Boolean
        get() {
            return children.mapNotNull {
                it as? EnumValueGenerator
            }.all {
                it.stringValue.isNotEmpty()
            }
        }

    fun generate(): TypeSpec {
        val builder = TypeSpec.enumBuilder(kotlinName)
            .addKdoc(escapeDoc(documentation))
            .addType(
                TypeSpec.companionObjectBuilder()
                    .superclass(EnumSupport::class.asTypeName().parameterizedBy(kotlinType))
                    .apply {
                        if (isStringEnum) {
                            addSuperinterface(ProtoStringEnumDsl::class.asTypeName().parameterizedBy(kotlinType), buildCodeBlock {
                                addStatement("ProtoStringEnumDsl(%T::class.java)", kotlinType)
                            })
                        } else {
                            addSuperinterface(ProtoEnumDsl::class.asTypeName().parameterizedBy(kotlinType), buildCodeBlock {
                                addStatement("ProtoEnumDsl(%T::class.java)", kotlinType)
                            })
                        }
                    }
                    .addProperty(
                        PropertySpec.builder("descriptor", EnumDescriptorProto::class)
                            .addModifiers(KModifier.OVERRIDE)
                            .delegate(
                                buildCodeBlock {
                                    beginControlFlow("%M", MemberName("kotlin", "lazy"))
                                    val parent = parent
                                    when (parent) {
                                        is FileGenerator -> {
                                            addStatement("%T.descriptor.enumType.first{ it.name == %S }", parent.fileMetaTypeName, protoName)
                                        }
                                        is MessageGenerator -> {
                                            addStatement("%T.descriptor.enumType.first{ it.name == %S }", parent.kotlinType, protoName)
                                        }
                                        else -> throw IllegalStateException("Message must be a child of message or file.")
                                    }
                                    endControlFlow()
                                }
                            )
                            .build()
                    )
                    .build())
            .apply {
                if (isStringEnum) {
                    addSuperinterface(ProtoStringEnum::class)
                } else {
                    addSuperinterface(ProtoEnum::class)
                }
            }
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("number", Int::class)
                    .addParameter("proto", String::class)
                    .apply {
                        if (isStringEnum) {
                            addParameter("value", String::class)
                        }
                    }
                    .build()
            )
            .addProperty(
                PropertySpec.builder("number", Int::class)
                    .initializer("number")
                    .addModifiers(KModifier.OVERRIDE)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("proto", String::class)
                    .initializer("proto")
                    .addModifiers(KModifier.OVERRIDE)
                    .build()
            )
            .apply {
                if (isStringEnum) {
                    addProperty(
                        PropertySpec.builder("value", String::class)
                            .initializer("value")
                            .addModifiers(KModifier.OVERRIDE)
                            .build()
                    )
                }
            }

        for (child in children) {
            when (child) {
                is EnumValueGenerator -> {
                    builder.addEnumConstant(child.kotlinName, child.generate())
                }
            }
        }

        return builder.build()
    }
}
