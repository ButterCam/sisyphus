package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.protobuf.OneOfDelegate
import com.bybutter.sisyphus.protobuf.OneOfValue
import com.bybutter.sisyphus.string.toCamelCase
import com.bybutter.sisyphus.string.toPascalCase
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.KProperty

class OneOfGenerator(override val parent: MessageGenerator, val descriptor: DescriptorProtos.OneofDescriptorProto) : ProtobufElement() {
    override val kotlinName: String = descriptor.name.toPascalCase()
    override val protoName: String = descriptor.name

    val propertyName = descriptor.name.toCamelCase()

    val delegateName = "${kotlinName}Delegate"

    val delegatePropertyName = "_${propertyName}Delegate"

    val delegateType by lazy {
        ClassName.bestGuess("${parent.implType}.$delegateName")
    }

    val hasFunName by lazy {
        "has$kotlinName"
    }

    val clearFunName by lazy {
        "clear$kotlinName"
    }

    val kotlinType get() = ClassName.bestGuess(fullKotlinName)

    val valueType get() = kotlinType.parameterizedBy(TypeVariableName("*")).copy(true)

    fun applyToMessage(builder: TypeSpec.Builder) {
        builder.addProperty(
            PropertySpec.builder(propertyName, valueType)
                .addKdoc(escapeDoc(documentation))
                .build()
        )
        builder.addFunction(
            FunSpec.builder(hasFunName)
                .addModifiers(KModifier.ABSTRACT)
                .returns(Boolean::class.java)
                .build()
        )
        builder.addType(
            TypeSpec.interfaceBuilder(kotlinName)
                .addSuperinterface(OneOfValue::class.asTypeName().parameterizedBy(TypeVariableName("T")))
                .addTypeVariable(TypeVariableName("T"))
                .apply {
                    for (child in children) {
                        when (child) {
                            is OneOfFieldGenerator -> {
                                child.applyToOneOf(this)
                            }
                        }
                    }
                }
                .build()
        )
    }

    fun applyToMutable(builder: TypeSpec.Builder) {
        builder.addProperty(
            PropertySpec.builder(propertyName, valueType)
                .mutable()
                .addModifiers(KModifier.OVERRIDE)
                .build()
        )
        builder.addFunction(
            FunSpec.builder(clearFunName)
                .addModifiers(KModifier.ABSTRACT)
                .returns(valueType)
                .build()
        )
    }

    fun applyToImpl(builder: TypeSpec.Builder) {
        builder.addProperty(
            PropertySpec.builder(delegatePropertyName, delegateType)
                .addModifiers(KModifier.PRIVATE)
                .initializer("%T()", delegateType)
                .build()
        )
        builder.addProperty(
            PropertySpec.builder(propertyName, valueType)
                .mutable()
                .addModifiers(KModifier.OVERRIDE)
                .delegate("%N", delegatePropertyName)
                .build()
        )
        builder.addFunction(
            FunSpec.builder(hasFunName)
                .addModifiers(KModifier.OVERRIDE)
                .returns(Boolean::class.java)
                .addStatement("return %N.has(this, ::%N)", delegatePropertyName, propertyName)
                .build()
        )
        builder.addFunction(
            FunSpec.builder(clearFunName)
                .addModifiers(KModifier.OVERRIDE)
                .returns(valueType)
                .addStatement("return %N.clear(this, ::%N)", delegatePropertyName, propertyName)
                .build()
        )
        builder.addType(generateDelegate())
    }

    fun generateDelegate(): TypeSpec {
        return TypeSpec.classBuilder(delegateName)
            .addModifiers(KModifier.INTERNAL)
            .addSuperinterface(OneOfDelegate::class.asTypeName().parameterizedBy(valueType.copy(false), parent.implType))
            .addProperty(PropertySpec.builder("value", valueType, KModifier.OVERRIDE).mutable().initializer("null").build())
            .addFunction(FunSpec.builder("getValue")
                .addModifiers(KModifier.OVERRIDE)
                .addTypeVariable(TypeVariableName.T)
                .addParameter("message", parent.implType)
                .addParameter("property", KProperty::class.asTypeName().parameterizedBy(TypeVariableName.STAR))
                .returns(TypeVariableName.T)
                .addStatement("val value = value")
                .beginControlFlow("return when(property.name)")
                .apply {
                    for (child in children) {
                        when (child) {
                            is OneOfFieldGenerator -> {
                                child.applyToOneOfDelegateGetter(this)
                            }
                        }
                    }
                }
                .addStatement("%S -> value as T", propertyName)
                .addStatement("else -> throw %T(\"Message·not·contains·field·definition·of·'\${property.name}'\")", IllegalArgumentException::class)
                .endControlFlow()
                .build())
            .addFunction(FunSpec.builder("setValue")
                .addModifiers(KModifier.OVERRIDE)
                .addTypeVariable(TypeVariableName.T)
                .addParameter("message", parent.implType)
                .addParameter("property", KProperty::class.asTypeName().parameterizedBy(TypeVariableName.STAR))
                .addParameter("value", TypeVariableName.T)
                .beginControlFlow("when(property.name)")
                .apply {
                    for (child in children) {
                        when (child) {
                            is OneOfFieldGenerator -> {
                                child.applyToOneOfDelegateSetter(this)
                            }
                        }
                    }
                }
                .beginControlFlow("%S ->", propertyName)
                .addStatement("this.value = value as? %T", valueType)
                .addStatement("message.invalidCache()")
                .endControlFlow()
                .addStatement("else -> throw %T(\"Message·not·contains·field·definition·of·'\${property.name}'\")", IllegalArgumentException::class)
                .endControlFlow()
                .build())
            .addFunction(FunSpec.builder("clear")
                .addModifiers(KModifier.OVERRIDE)
                .addTypeVariable(TypeVariableName.T)
                .addParameter("message", parent.implType)
                .addParameter("property", KProperty::class.asTypeName().parameterizedBy(TypeVariableName.STAR))
                .returns(TypeVariableName.T.copy(true))
                .addStatement("val result = value ?: return null")
                .beginControlFlow("return when(property.name)")
                .apply {
                    for (child in children) {
                        when (child) {
                            is OneOfFieldGenerator -> {
                                child.applyToOneOfDelegateClear(this)
                            }
                        }
                    }
                }
                .beginControlFlow("%S ->", propertyName)
                .addStatement("value·=·null")
                .addStatement("message.invalidCache()")
                .addStatement("result as T")
                .endControlFlow()
                .addStatement("else -> throw %T(\"Message·not·contains·field·definition·of·'\${property.name}'\")", IllegalArgumentException::class)
                .endControlFlow()
                .build())
            .addFunction(FunSpec.builder("has")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("message", parent.implType)
                .addParameter("property", KProperty::class.asTypeName().parameterizedBy(TypeVariableName.STAR))
                .returns(Boolean::class)
                .beginControlFlow("return when(property.name)")
                .apply {
                    for (child in children) {
                        when (child) {
                            is OneOfFieldGenerator -> {
                                child.applyToOneOfDelegateHas(this)
                            }
                        }
                    }
                }
                .addStatement("%S -> value != null", propertyName)
                .addStatement("else -> throw %T(\"Message·not·contains·field·definition·of·'\${property.name}'\")", IllegalArgumentException::class)
                .endControlFlow()
                .build())
            .build()
    }
}

val TypeVariableName.Companion.T by lazy { TypeVariableName("T") }

val TypeVariableName.Companion.STAR by lazy { TypeVariableName("*") }
