package com.bybutter.sisyphus.protobuf.compiler

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.buildCodeBlock
import kotlin.reflect.KClass

fun CodeBlock.Builder.beginScope(controlFlow: String, vararg args: Any?, block: CodeBlock.Builder.() -> Unit): CodeBlock.Builder {
    beginControlFlow(controlFlow, *args)
    block()
    endControlFlow()
    return this
}

operator fun TypeSpec.Builder.plus(modifier: KModifier): TypeSpec.Builder {
    return this.addModifiers(modifier)
}

operator fun PropertySpec.Builder.plus(modifier: KModifier): PropertySpec.Builder {
    return this.addModifiers(modifier)
}

operator fun FunSpec.Builder.plus(modifier: KModifier): FunSpec.Builder {
    return this.addModifiers(modifier)
}

fun kClass(name: String, block: TypeSpec.Builder.() -> Unit): TypeSpec {
    return TypeSpec.classBuilder(name).apply(block).build()
}

fun kObject(name: String, block: TypeSpec.Builder.() -> Unit): TypeSpec {
    return TypeSpec.objectBuilder(name).apply(block).build()
}

fun kInterface(name: String, block: TypeSpec.Builder.() -> Unit): TypeSpec {
    return TypeSpec.interfaceBuilder(name).apply(block).build()
}

fun kFun(name: String, block: FunSpec.Builder.() -> Unit): FunSpec {
    return FunSpec.builder(name).apply(block).build()
}

fun kProperty(name: String, type: TypeName, block: PropertySpec.Builder.() -> Unit): PropertySpec {
    return PropertySpec.builder(name, type).apply(block).build()
}

fun kProperty(name: String, type: KClass<*>, block: PropertySpec.Builder.() -> Unit): PropertySpec {
    return PropertySpec.builder(name, type).apply(block).build()
}

fun TypeSpec.Builder.kCompanion(block: TypeSpec.Builder.() -> Unit) {
    this.addType(TypeSpec.companionObjectBuilder().apply(block).build())
}

fun TypeSpec.Builder.kInit(block: CodeBlock.Builder.() -> Unit) {
    this.addInitializerBlock(buildCodeBlock(block))
}

fun TypeSpec.Builder.extends(type: TypeName) {
    this.superclass(type)
}

fun TypeSpec.Builder.extends(type: KClass<*>) {
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

fun TypeSpec.Builder.constructor(block: FunSpec.Builder.() -> Unit) {
    primaryConstructor(FunSpec.constructorBuilder().apply(block).build())
}
