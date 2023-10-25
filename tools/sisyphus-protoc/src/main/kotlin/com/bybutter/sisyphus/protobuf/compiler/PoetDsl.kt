package com.bybutter.sisyphus.protobuf.compiler

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.buildCodeBlock
import kotlin.reflect.KClass

fun CodeBlock.Builder.beginScope(
    controlFlow: String,
    vararg args: Any?,
    block: CodeBlock.Builder.() -> Unit = {},
): CodeBlock.Builder {
    beginControlFlow(controlFlow, *args)
    block()
    endControlFlow()
    return this
}

operator fun TypeSpec.Builder.plusAssign(modifier: KModifier) {
    this.addModifiers(modifier)
}

operator fun PropertySpec.Builder.plusAssign(modifier: KModifier) {
    this.addModifiers(modifier)
}

fun FunSpec.Builder.annotation(
    annotation: ClassName,
    block: AnnotationSpec.Builder.() -> Unit = {},
) {
    this.addAnnotation(AnnotationSpec.builder(annotation).apply(block).build())
}

fun TypeSpec.Builder.annotation(
    annotation: ClassName,
    block: AnnotationSpec.Builder.() -> Unit = {},
) {
    this.addAnnotation(AnnotationSpec.builder(annotation).apply(block).build())
}

fun PropertySpec.Builder.annotation(
    annotation: ClassName,
    block: AnnotationSpec.Builder.() -> Unit = {},
) {
    this.addAnnotation(AnnotationSpec.builder(annotation).apply(block).build())
}

fun PropertySpec.Builder.getter(block: FunSpec.Builder.() -> Unit) {
    this.getter(FunSpec.getterBuilder().apply(block).build())
}

fun PropertySpec.Builder.setter(block: FunSpec.Builder.() -> Unit) {
    this.setter(FunSpec.setterBuilder().apply(block).build())
}

operator fun FunSpec.Builder.plusAssign(modifier: KModifier) {
    this.addModifiers(modifier)
}

fun kFile(
    packageName: String,
    name: String,
    block: FileSpec.Builder.() -> Unit = {},
): FileSpec {
    return FileSpec.builder(packageName, name).apply(block).build()
}

fun kClass(
    name: String,
    block: TypeSpec.Builder.() -> Unit = {},
): TypeSpec {
    return TypeSpec.classBuilder(name).apply(block).build()
}

fun kEnum(
    name: String,
    block: TypeSpec.Builder.() -> Unit = {},
): TypeSpec {
    return TypeSpec.enumBuilder(name).apply(block).build()
}

fun kObject(
    name: String,
    block: TypeSpec.Builder.() -> Unit = {},
): TypeSpec {
    return TypeSpec.objectBuilder(name).apply(block).build()
}

fun kInterface(
    name: String,
    block: TypeSpec.Builder.() -> Unit = {},
): TypeSpec {
    return TypeSpec.interfaceBuilder(name).apply(block).build()
}

fun kProperty(
    name: String,
    type: TypeName,
    block: PropertySpec.Builder.() -> Unit = {},
): PropertySpec {
    return PropertySpec.builder(name, type).apply(block).build()
}

fun kProperty(
    name: String,
    type: KClass<*>,
    block: PropertySpec.Builder.() -> Unit = {},
): PropertySpec {
    return PropertySpec.builder(name, type).apply(block).build()
}

fun kFun(
    name: String,
    block: FunSpec.Builder.() -> Unit = {},
): FunSpec {
    return FunSpec.builder(name).apply(block).build()
}

fun TypeSpec.Builder.companion(block: TypeSpec.Builder.() -> Unit = {}) {
    this.addType(TypeSpec.companionObjectBuilder().apply(block).build())
}

fun TypeSpec.Builder.kInit(block: CodeBlock.Builder.() -> Unit = {}) {
    this.addInitializerBlock(buildCodeBlock(block))
}

infix fun TypeSpec.Builder.extends(type: TypeName) {
    this.superclass(type)
}

infix fun TypeSpec.Builder.extends(type: KClass<*>) {
    this.superclass(type)
}

fun TypeSpec.Builder.implements(vararg types: TypeName) {
    for (type in types) {
        this.addSuperinterface(type)
    }
}

fun TypeSpec.Builder.implements(vararg types: KClass<*>) {
    for (type in types) {
        this.addSuperinterface(type)
    }
}

infix fun TypeSpec.Builder.implements(type: TypeName) {
    this.addSuperinterface(type)
}

infix fun TypeSpec.Builder.implements(type: KClass<*>) {
    this.addSuperinterface(type)
}

fun TypeSpec.Builder.constructor(block: FunSpec.Builder.() -> Unit = {}) {
    primaryConstructor(FunSpec.constructorBuilder().apply(block).build())
}

fun TypeSpec.Builder.property(
    name: String,
    type: TypeName,
    block: PropertySpec.Builder.() -> Unit = {},
) {
    addProperty(PropertySpec.builder(name, type).apply(block).build())
}

fun TypeSpec.Builder.property(
    name: String,
    type: KClass<*>,
    block: PropertySpec.Builder.() -> Unit = {},
) {
    addProperty(PropertySpec.builder(name, type).apply(block).build())
}

fun TypeSpec.Builder.type(
    name: String,
    block: TypeSpec.Builder.() -> Unit = {},
) {
    addType(TypeSpec.classBuilder(name).apply(block).build())
}

fun TypeSpec.Builder.function(
    name: String,
    block: FunSpec.Builder.() -> Unit = {},
) {
    addFunction(FunSpec.builder(name).apply(block).build())
}

fun FunSpec.Builder.parameter(
    name: String,
    type: KClass<*>,
    block: ParameterSpec.Builder.() -> Unit = {},
) {
    addParameter(ParameterSpec.builder(name, type).apply(block).build())
}

fun FunSpec.Builder.parameter(
    name: String,
    type: TypeName,
    block: ParameterSpec.Builder.() -> Unit = {},
) {
    addParameter(ParameterSpec.builder(name, type).apply(block).build())
}

data class WhenBranchBuilder(val branch: CodeBlock, val codeBlock: CodeBlock.Builder)
