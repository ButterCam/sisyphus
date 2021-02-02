package com.bybutter.sisyphus.protobuf.compiler.resourcename

import com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator
import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.beginScope
import com.bybutter.sisyphus.protobuf.compiler.companion
import com.bybutter.sisyphus.protobuf.compiler.constructor
import com.bybutter.sisyphus.protobuf.compiler.core.state.ApiFileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.MessageInterfaceGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.advance
import com.bybutter.sisyphus.protobuf.compiler.extends
import com.bybutter.sisyphus.protobuf.compiler.function
import com.bybutter.sisyphus.protobuf.compiler.getter
import com.bybutter.sisyphus.protobuf.compiler.implements
import com.bybutter.sisyphus.protobuf.compiler.kClass
import com.bybutter.sisyphus.protobuf.compiler.kInterface
import com.bybutter.sisyphus.protobuf.compiler.parameter
import com.bybutter.sisyphus.protobuf.compiler.plusAssign
import com.bybutter.sisyphus.protobuf.compiler.property
import com.google.api.pathtemplate.PathTemplate
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock

class ResourceNameGenerator : GroupedGenerator<ApiFileGeneratingState> {
    override fun generate(state: ApiFileGeneratingState): Boolean {
        for (resource in state.descriptor.resources) {
            state.target.addType(kInterface(resource.name()) {
                ResourceNameGeneratingState(state, resource, this).advance()
            })
        }
        return true
    }
}

class MessageResourceNameGenerator : GroupedGenerator<MessageInterfaceGeneratingState> {
    override fun generate(state: MessageInterfaceGeneratingState): Boolean {
        state.descriptor.resource?.let {
            state.target.addType(kInterface(it.name()) {
                ResourceNameGeneratingState(state, it, this).advance()
            })
        }
        return true
    }
}

class ResourceNameBasicGenerator : GroupedGenerator<ResourceNameGeneratingState> {
    override fun generate(state: ResourceNameGeneratingState): Boolean {
        state.target.apply {
            this implements RuntimeTypes.RESOURCE_NAME

            val templates = state.descriptor.descriptor.patternList.map {
                PathTemplate.create(it)
            }

            val commonFields = templates.firstOrNull()?.vars()?.toMutableSet() ?: mutableSetOf()
            for (template in templates) {
                commonFields.removeIf {
                    !template.vars().contains(it)
                }
            }

            for (commonField in commonFields) {
                property(commonField, String::class) {
                    getter {
                        addStatement("return this[%S] ?: %S", commonField, "?")
                    }
                }
            }

            function("support") {
                this += KModifier.OVERRIDE
                returns(state.descriptor.className().nestedClass("Companion"))

                addStatement("return %T", state.descriptor.className())
            }

            companion {
                ResourceNameCompanionGeneratingState(state, state.descriptor, this).advance()
            }
        }

        return true
    }
}

class ResourceNameImplementationGenerator : GroupedGenerator<ResourceNameGeneratingState> {
    override fun generate(state: ResourceNameGeneratingState): Boolean {
        for ((index, template) in state.descriptor.templates.withIndex()) {
            state.target.addType(kClass(state.descriptor.templateName(template)) {
                this extends RuntimeTypes.ABSTRACT_RESOURCE_NAME
                this implements state.descriptor.className()

                constructor {
                    parameter("data", MAP.parameterizedBy(String::class.asTypeName(), String::class.asTypeName()))
                }
                addSuperclassConstructorParameter("data")

                function("template") {
                    this += KModifier.OVERRIDE
                    returns(PathTemplate::class)

                    addStatement("return support().patterns[$index]")
                }

                for (field in template.vars()) {
                    if (!state.descriptor.commonFields.contains(field)) {
                        property(field, String::class) {
                            getter {
                                addStatement("return this[%S] ?: %S", field, "?")
                            }
                        }
                    }
                }
            })
        }
        return true
    }
}

class ResourceNameCompanionBasicGenerator : GroupedGenerator<ResourceNameCompanionGeneratingState> {
    override fun generate(state: ResourceNameCompanionGeneratingState): Boolean {
        state.target.apply {
            this extends RuntimeTypes.RESOURCE_NAME_SUPPORT.parameterizedBy(state.descriptor.className())

            property("type", String::class) {
                this += KModifier.OVERRIDE
                getter {
                    addStatement("return %S", state.descriptor.descriptor.type)
                }
            }

            property("patterns", LIST.parameterizedBy(PathTemplate::class.asTypeName())) {
                this += KModifier.OVERRIDE
                getter {
                    addStatement("return listOf()")
                }
            }

            property("plural", String::class) {
                this += KModifier.OVERRIDE
                getter {
                    addStatement("return %S", state.descriptor.plural)
                }
            }

            property("singular", String::class) {
                this += KModifier.OVERRIDE
                getter {
                    addStatement("return %S", state.descriptor.singular)
                }
            }

            function("tryCreate") {
                this += KModifier.OVERRIDE
                returns(state.descriptor.className().copy(true))
                parameter("name", String::class)

                addCode(buildCodeBlock {
                    beginScope("for ((index, pattern) in patterns.withIndex())") {
                        addStatement("val result = pattern.match(name) ?: continue")
                        beginScope("return when(index)") {
                            for ((index, template) in state.descriptor.templates.withIndex()) {
                                addStatement("$index -> %T(result)", state.descriptor.tempalteClassName(template))
                            }
                            addStatement("else -> null")
                        }
                    }
                    addStatement("return null")
                })
            }

            function("invoke") {
                this += KModifier.OVERRIDE
                returns(state.descriptor.className())
                parameter("name", String::class)
                addCode(buildCodeBlock {
                    addStatement("return tryCreate(name) ?: %L", TypeSpec.anonymousClassBuilder().apply {
                        this extends RuntimeTypes.UNKNOWN_RESOURCE_NAME.parameterizedBy(state.descriptor.className())
                        this implements state.descriptor.className()
                        addSuperclassConstructorParameter("name")
                    }.build())
                })
            }
        }
        return true
    }
}
