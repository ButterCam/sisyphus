package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.string.toPascalCase
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

open class OneOfFieldGenerator constructor(parent: MessageGenerator, val oneOfGenerator: OneOfGenerator, descriptor: DescriptorProtos.FieldDescriptorProto) : FieldGenerator(parent, descriptor) {
    val oneOfTypeName = descriptor.name.toPascalCase()

    val fullOneOfName get() = "${oneOfGenerator.fullKotlinName}.$oneOfTypeName"

    val oneOfType get() = ClassName.bestGuess(fullOneOfName)

    val oneOfValueType by lazy {
        valueType.copy(false)
    }

    override fun applyToImpl(builder: TypeSpec.Builder) {
        when (descriptor.label) {
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL -> {
                builder.addProperty(
                    PropertySpec.builder(kotlinName, valueType, KModifier.OVERRIDE)
                        .mutable()
                        .delegate("%N", oneOfGenerator.delegatePropertyName)
                        .build()
                )
                builder.addFunction(
                    FunSpec.builder(hasFunName)
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(Boolean::class)
                        .addStatement("return %N.has(this, ::%N)", oneOfGenerator.delegatePropertyName, kotlinName)
                        .build()
                )
                builder.addFunction(
                    FunSpec.builder(clearFunName)
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(valueType.copy(true))
                        .addStatement("return %N.clear<%T>(this, ::%N)", oneOfGenerator.delegatePropertyName, valueType.copy(false), kotlinName)
                        .build()
                )
            }
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED -> {
                builder.addProperty(
                    PropertySpec.builder(kotlinName, valueType, KModifier.OVERRIDE)
                        .mutable()
                        .delegate("%N", oneOfGenerator.delegatePropertyName)
                        .build()
                )
                builder.addFunction(
                    FunSpec.builder(hasFunName)
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(Boolean::class)
                        .addStatement("return %N.isNotEmpty()", kotlinName)
                        .build()
                )
                builder.addFunction(
                    FunSpec.builder(clearFunName)
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(valueType.copy(true))
                        .addStatement("return %N.clear(this, ::%N)", oneOfGenerator.delegatePropertyName, kotlinName)
                        .build()
                )
            }
            else -> throw UnsupportedOperationException("Unknown field label.")
        }
    }

    fun applyToOneOf(builder: TypeSpec.Builder) {
        builder.addType(
            TypeSpec.Companion.classBuilder(oneOfTypeName)
                // .addModifiers(KModifier.DATA) Kotlin compiler bug
                .addSuperinterface(oneOfGenerator.kotlinType.parameterizedBy(oneOfValueType))
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("value", oneOfValueType)
                        .addParameter(ParameterSpec.builder("number", Int::class).defaultValue("%L", descriptor.number).build())
                        .addParameter(ParameterSpec.builder("name", String::class).defaultValue("%S", protoName).build())
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("value", oneOfValueType, KModifier.OVERRIDE).initializer("value").build()
                )
                .addProperty(
                    PropertySpec.builder("number", Int::class, KModifier.OVERRIDE).initializer("number").build()
                )
                .addProperty(
                    PropertySpec.builder("name", String::class, KModifier.OVERRIDE).initializer("name").build()
                )
                .build()
        )
    }

    fun applyToOneOfDelegateGetter(builder: FunSpec.Builder) {
        builder.addStatement("%S -> (if(value is %T) value.value else ${defaultValue()}) as T", kotlinName, oneOfType)
    }

    fun applyToOneOfDelegateSetter(builder: FunSpec.Builder) {
        builder.beginControlFlow("%S ->", kotlinName)
        builder.addStatement("this.value = (value as %T)?.let·{·%T(it)·}", valueType, oneOfType)
        builder.addStatement("message.invalidCache()")
        builder.endControlFlow()
    }

    fun applyToOneOfDelegateClear(builder: FunSpec.Builder) {
        builder.beginControlFlow("%S ->", kotlinName)
        builder.beginControlFlow("if(result is %T)", oneOfType)
        builder.addStatement("value = null")
        builder.addStatement("message.invalidCache()")
        builder.addStatement("result.value as T")
        builder.nextControlFlow("else")
        builder.addStatement("null")
        builder.endControlFlow()
        builder.endControlFlow()
    }

    fun applyToOneOfDelegateHas(builder: FunSpec.Builder) {
        builder.addStatement("%S -> value is %T", kotlinName, oneOfType)
    }
}
