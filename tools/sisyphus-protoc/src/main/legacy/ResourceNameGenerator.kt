package com.bybutter.sisyphus.protobuf.legacy

import com.bybutter.sisyphus.api.ResourceDescriptor
import com.bybutter.sisyphus.api.resource.AbstractResourceName
import com.bybutter.sisyphus.api.resource.PathTemplate
import com.bybutter.sisyphus.string.toCamelCase
import com.bybutter.sisyphus.string.toPascalCase
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock

open class ResourceNameGenerator(override val parent: ResourceNameParentGenerator, val descriptor: ResourceDescriptor, val index: Int, private val parentField: Set<String>) : ProtobufElement() {
    val template: PathTemplate by lazy {
        PathTemplate.create(descriptor.pattern[index])
    }

    val uniqueFields: Set<String> by lazy {
        template.vars() - parentField
    }

    override val kotlinName: String by lazy {
        if (uniqueFields.isEmpty()) {
            "${parent.kotlinName}Base"
        } else {
            "${parent.kotlinName}With${uniqueFields.joinToString("And") { it.toPascalCase() }}"
        }
    }

    override val fullKotlinName: String by lazy {
        "${parent.parent.fullKotlinName}.$kotlinName"
    }

    val kotlinType by lazy {
        ClassName.bestGuess(fullKotlinName)
    }

    override val protoName: String get() = kotlinName

    open fun generateImpl(): TypeSpec {
        return TypeSpec.classBuilder(kotlinName)
            .primaryConstructor(FunSpec.constructorBuilder()
                .addParameter("data", MAP.parameterizedBy(String::class.asTypeName(), String::class.asTypeName()))
                .build())
            .superclass(AbstractResourceName::class)
            .addSuperclassConstructorParameter("data")
            .addSuperclassConstructorParameter("%T.patterns[%L]", parent.kotlinType, index)
            .addSuperclassConstructorParameter("%T", parent.kotlinType)
            .addSuperinterface(parent.kotlinType)
            .apply {
                val fields = PathTemplate.create(descriptor.pattern[index]).vars().toMutableSet()
                for (field in fields) {
                    this.addProperty(
                        PropertySpec.builder(field.toCamelCase(), String::class.asTypeName())
                            .apply {
                                if (field in parentField) {
                                    addModifiers(KModifier.OVERRIDE)
                                }
                            }
                            .initializer("data[\"$field\"] ?: throw %T(%S)", NullPointerException::class.java, "'$field' not existed in $kotlinName.")
                            .build()
                    )
                }
            }
            .addFunction(
                FunSpec.builder("plus")
                    .addModifiers(KModifier.OPERATOR, KModifier.OVERRIDE)
                    .addParameter("map", MAP.parameterizedBy(STRING, STRING))
                    .addStatement("return %T(this.toMap() + map)", kotlinType)
                    .build()
            )
            .build()
    }

    open fun generateConstructor(): FunSpec {
        val consFunName = if (uniqueFields.isEmpty()) {
            "of"
        } else {
            "of${uniqueFields.joinToString("And") { it.toPascalCase() }}"
        }

        return FunSpec.builder(consFunName)
            .apply {
                for (field in template.vars()) {
                    addParameter(field, String::class)
                }
            }
            .apply {
                addCode(buildCodeBlock {
                    add("return %T(mapOf(", kotlinType)
                    for ((index, field) in template.vars().withIndex()) {
                        if (index > 0) {
                            add(", ")
                        }
                        add("%S to %L", field, field)
                    }
                    add("))")
                })
            }
            .build()
    }
}
