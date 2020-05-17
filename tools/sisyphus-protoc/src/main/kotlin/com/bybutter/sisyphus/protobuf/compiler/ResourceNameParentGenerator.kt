package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.api.ResourceDescriptor
import com.bybutter.sisyphus.api.resource.PathTemplate
import com.bybutter.sisyphus.api.resource.ResourceName
import com.bybutter.sisyphus.api.resource.ResourceNameSupport
import com.bybutter.sisyphus.string.plural
import com.bybutter.sisyphus.string.singular
import com.bybutter.sisyphus.string.toCamelCase
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock

open class ResourceNameParentGenerator(override val parent: ProtobufElement, val descriptor: ResourceDescriptor) : ProtobufElement() {
    val resourceName = PathTemplate.create("{type}").match("""//${descriptor.type}""")?.getValue("type")
        ?: throw IllegalStateException()

    var parentField: Set<String> = setOf()

    val singular: String by lazy {
        if (descriptor.singular.isNotEmpty()) {
            return@lazy descriptor.singular
        }

        if (descriptor.plural.isNotEmpty()) {
            return@lazy descriptor.plural.singular()
        }

        resourceName.toCamelCase().singular()
    }

    val plural: String by lazy {
        if (descriptor.plural.isNotEmpty()) {
            return@lazy descriptor.plural
        }

        if (descriptor.singular.isNotEmpty()) {
            return@lazy descriptor.singular.plural()
        }

        resourceName.toCamelCase().plural()
    }

    override val kotlinName: String by lazy {
        when (parent) {
            is MessageGenerator -> {
                "Name"
            }
            is FileGenerator -> {
                "${resourceName}Name"
            }
            else -> throw IllegalStateException("Resource name must be a child of message or file.")
        }
    }

    override val protoName: String get() = kotlinName

    val supportName by lazy {
        "${kotlinName}Support"
    }

    val fullSupportName: String by lazy {
        when (val parent = parent) {
            is MessageGenerator -> {
                "${parent.fullSupportName}.$supportName"
            }
            is FileGenerator -> {
                "${parent.internalKotlinName}.$supportName"
            }
            else -> throw IllegalStateException("Message must be a child of message or file.")
        }
    }

    val supportType by lazy {
        ClassName.bestGuess(fullSupportName)
    }

    val kotlinType by lazy {
        ClassName.bestGuess(fullKotlinName)
    }

    override fun init() {
        parentField = PathTemplate.create(descriptor.pattern[0]).vars()
        for (pattern in descriptor.pattern) {
            parentField = parentField.intersect(PathTemplate.create(pattern).vars())
        }
        for ((index, _) in descriptor.pattern.withIndex()) {
            addElement(ResourceNameGenerator(this, descriptor, index, parentField))
        }

        when (val parent = parent) {
            is MessageGenerator -> {
                this.context().resourceNames[descriptor.type] = this
            }
        }
        super.init()
    }

    open fun generate(): TypeSpec {
        return TypeSpec.interfaceBuilder(kotlinName)
            .addSuperinterface(ResourceName::class.asTypeName())
            .addKdoc(escapeDoc(documentation))
            .apply {
                for (field in parentField) {
                    this.addProperty(PropertySpec.builder(field.toCamelCase(), String::class.asTypeName())
                        .addKdoc(escapeDoc(documentation))
                        .build())
                }
            }
            .addType(generateCompanion())
            .build()
    }

    open fun generateImpl(): List<TypeSpec> {
        return children.mapNotNull {
            if (it is ResourceNameGenerator) {
                it.generateImpl()
            } else {
                null
            }
        }
    }

    open fun generateSupport(): TypeSpec {
        return TypeSpec.classBuilder(supportName)
            .addModifiers(KModifier.ABSTRACT)
            .superclass(ResourceNameSupport::class.asTypeName().parameterizedBy(TypeVariableName("T")))
            .addTypeVariable(TypeVariableName("T", kotlinType))
            .primaryConstructor(FunSpec.constructorBuilder().addModifiers(KModifier.INTERNAL).build())
            .addProperty(
                PropertySpec
                    .builder("type", String::class.asTypeName())
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer("%S", descriptor.type)
                    .build()
            )
            .addProperty(
                PropertySpec
                    .builder("patterns", LIST.parameterizedBy(PathTemplate::class.asTypeName()))
                    .initializer(buildCodeBlock {
                        add("listOf(")
                        for ((index, pattern) in descriptor.pattern.withIndex()) {
                            if (index > 0) {
                                add(", ")
                            }
                            add("%T.create(%S)", PathTemplate::class, pattern)
                        }
                        add(")")
                    })
                    .addModifiers(KModifier.OVERRIDE)
                    .build()
            )
            .addProperty(
                PropertySpec
                    .builder("singular", String::class.asTypeName())
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer("%S", singular)
                    .build()
            )
            .addProperty(
                PropertySpec
                    .builder("plural", String::class.asTypeName())
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer("%S", plural)
                    .build())
            .addFunction(
                FunSpec.builder("invoke")
                    .addParameter("path", String::class.asTypeName())
                    .addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    .returns(TypeVariableName("T"))
                    .apply {
                        beginControlFlow("for((patternIndex, pattern) in patterns.withIndex())")
                        addStatement("val result = pattern.match(${this.parameters[0].name}) ?: continue")
                        beginControlFlow("return when(patternIndex)")
                        for (child in children) {
                            when (child) {
                                is ResourceNameGenerator -> {
                                    addStatement("%L -> %T(result) as T", child.index, child.kotlinType)
                                }
                            }
                        }
                        addStatement("else -> TODO()")
                        endControlFlow()
                        endControlFlow()
                        addStatement("return %T(${this.parameters[0].name}) as T", ResourceName::class.asTypeName().parameterizedBy(ClassName.bestGuess(fullKotlinName)))
                    }
                    .build()
            )
            .apply {
                for (child in children) {
                    when (child) {
                        is ResourceNameGenerator -> {
                            addFunction(child.generateConstructor())
                        }
                    }
                }
            }
            .build()
    }

    open fun generateCompanion(): TypeSpec {
        return TypeSpec.companionObjectBuilder()
            .superclass(supportType.parameterizedBy(TypeVariableName(kotlinName, ANY.copy(true))))
            .build()
    }
}
