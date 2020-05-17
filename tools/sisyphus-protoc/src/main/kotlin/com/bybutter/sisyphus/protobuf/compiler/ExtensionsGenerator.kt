package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.protobuf.ExtensionSupport
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import com.bybutter.sisyphus.reflect.Reflect
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock

class ExtensionsGenerator(parent: ProtobufElement, val messageGenerator: MessageGenerator, val fields: List<DescriptorProtos.FieldDescriptorProto>) : MessageGenerator(parent, messageGenerator.descriptor) {
    override val kotlinName: String = when (parent) {
        is FileGenerator -> {
            "${descriptor.name}ExtensionsIn${parent.kotlinFileNameWithoutExtension}"
        }
        is MessageGenerator -> {
            "${descriptor.name}Extensions"
        }
        else -> throw IllegalStateException("Extensions must be a child of message or file.")
    }
    override val protoName: String = descriptor.name

    override val documentation: String = ""

    override fun init() {
        for (field in fields) {
            addElement(ExtensionFieldGenerator(this, field))
        }
    }

    override fun generate(): TypeSpec {
        return super.generate().toBuilder()
            .addModifiers(KModifier.INTERNAL)
            .build()
    }

    override fun generateImpl(): TypeSpec {
        return super.generateImpl().toBuilder()
            .addModifiers(KModifier.INTERNAL)
            .build()
    }

    override fun generateMutable(): TypeSpec {
        return super.generateMutable().toBuilder()
            .addModifiers(KModifier.INTERNAL)
            .build()
    }

    override fun generateSupport(): TypeSpec {
        return super.generateSupport().toBuilder()
            .apply {
                Reflect.setPrivateField(this, "superclass", ExtensionSupport::class.asTypeName().parameterizedBy(kotlinType, mutableType))
                this.superclassConstructorParameters.clear()
            }
            .addModifiers(KModifier.INTERNAL)
            .addProperty(
                PropertySpec.builder("extendedFields", LIST.parameterizedBy(FieldDescriptorProto::class.asTypeName()))
                    .addModifiers(KModifier.OVERRIDE)
                    .delegate(buildCodeBlock {
                        beginControlFlow("lazy")
                        when (parent) {
                            is FileGenerator -> {
                                addStatement("%T.descriptor.extension.filter { it.extendee == %S }", parent.fileMetaTypeName, fields[0].extendee)
                            }
                            is MessageGenerator -> {
                                addStatement("%T.descriptor.extension.filter { it.extendee == %S }", parent.supportType, fields[0].extendee)
                            }
                        }
                        endControlFlow()
                    })
                    .build()
            )
            .build()
    }

    override fun prepareGenerating() {
    }

    fun applyToFile(builder: FileSpec.Builder) {
        for (child in children) {
            when (child) {
                is ExtensionFieldGenerator -> {
                    builder.addProperty(child.generateMessageExtensionProperty())
                    builder.addProperty(child.generateMutableExtensionProperty())
                    builder.addFunction(child.generateMessageExtensionHasFun())
                    builder.addFunction(child.generateMutableExtensionClearFun())
                }
            }
        }
    }

    fun applyToType(builder: TypeSpec.Builder) {
        for (child in children) {
            when (child) {
                is ExtensionFieldGenerator -> {
                    builder.addProperty(child.generateMessageExtensionProperty())
                    builder.addProperty(child.generateMutableExtensionProperty())
                    builder.addFunction(child.generateMessageExtensionHasFun())
                    builder.addFunction(child.generateMutableExtensionClearFun())
                }
            }
        }
    }
}

class ExtensionFieldGenerator(override val parent: ExtensionsGenerator, descriptor: DescriptorProtos.FieldDescriptorProto) : FieldGenerator(parent, descriptor) {
    fun generateMessageExtensionProperty(): PropertySpec {
        return PropertySpec.builder(kotlinName, valueType)
            .receiver(parent.messageGenerator.kotlinType)
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("return this[%L]", descriptor.number)
                    .build()
            )
            .build()
    }

    fun generateMessageExtensionHasFun(): FunSpec {
        return FunSpec.builder(hasFunName)
            .receiver(parent.messageGenerator.kotlinType)
            .returns(Boolean::class)
            .addStatement("return this.has(%L)", descriptor.number)
            .build()
    }

    fun generateMutableExtensionProperty(): PropertySpec {
        return PropertySpec.builder(kotlinName, valueType)
            .mutable()
            .receiver(parent.messageGenerator.mutableType)
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("return this[%L]", descriptor.number)
                    .build()
            )
            .setter(
                FunSpec.setterBuilder()
                    .addParameter("value", valueType)
                    .addStatement("this[%L] = value", descriptor.number)
                    .build()
            )
            .build()
    }

    fun generateMutableExtensionClearFun(): FunSpec {
        return FunSpec.builder(clearFunName)
            .receiver(parent.messageGenerator.mutableType)
            .returns(valueType.copy(true))
            .addStatement("return this.clear(%L) as %T", descriptor.number, valueType.copy(true))
            .build()
    }
}
