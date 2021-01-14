package com.bybutter.sisyphus.protobuf.compiler.generator.basic

import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.constructor
import com.bybutter.sisyphus.protobuf.compiler.function
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.FieldImplementationGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MutableOneofGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.OneofGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.OneofImplementationGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.clearFunction
import com.bybutter.sisyphus.protobuf.compiler.generating.defaultValue
import com.bybutter.sisyphus.protobuf.compiler.generating.fieldType
import com.bybutter.sisyphus.protobuf.compiler.generating.hasFunction
import com.bybutter.sisyphus.protobuf.compiler.generating.mutableFieldType
import com.bybutter.sisyphus.protobuf.compiler.generating.name
import com.bybutter.sisyphus.protobuf.compiler.generating.oneOfClassName
import com.bybutter.sisyphus.protobuf.compiler.generating.type
import com.bybutter.sisyphus.protobuf.compiler.generator.SortableGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.UniqueGenerator
import com.bybutter.sisyphus.protobuf.compiler.getter
import com.bybutter.sisyphus.protobuf.compiler.implements
import com.bybutter.sisyphus.protobuf.compiler.kInterface
import com.bybutter.sisyphus.protobuf.compiler.plusAssign
import com.bybutter.sisyphus.protobuf.compiler.property
import com.bybutter.sisyphus.protobuf.compiler.setter
import com.bybutter.sisyphus.protobuf.compiler.type
import com.bybutter.sisyphus.string.toCamelCase
import com.bybutter.sisyphus.string.toPascalCase
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.buildCodeBlock

open class OneOfGenerator : UniqueGenerator<OneofGeneratingState> {
    override fun generate(state: OneofGeneratingState): Boolean {
        state.target.property(
            state.descriptor.name.toCamelCase(),
            state.oneOfClassName().parameterizedBy(TypeVariableName("*")).copy(true)
        )

        state.target.addType(kInterface(state.descriptor.name.toPascalCase()) {
            this implements RuntimeTypes.ONE_OF_VALUE.parameterizedBy(TypeVariableName("T"))
            this.addTypeVariable(TypeVariableName("T"))

            for (field in state.parent.descriptor.fieldList) {
                if (field.hasOneofIndex() && field.oneofIndex == state.parent.descriptor.oneofDeclList.indexOf(state.descriptor)) {
                    type(field.name.toPascalCase()) {
                        this += KModifier.DATA
                        this implements state.oneOfClassName().parameterizedBy(state.type(field))
                        constructor {
                            addParameter("value", state.type(field))
                        }
                        property("value", state.type(field)) {
                            this += KModifier.OVERRIDE
                            initializer("value")
                        }
                    }
                }
            }
        })
        return true
    }
}

open class MutableOneOfGenerator : UniqueGenerator<MutableOneofGeneratingState> {
    override fun generate(state: MutableOneofGeneratingState): Boolean {
        state.target.property(
            state.descriptor.name.toCamelCase(),
            state.oneOfClassName().parameterizedBy(TypeVariableName("*")).copy(true)
        ) {
            this += KModifier.OVERRIDE
            mutable()
        }
        return true
    }
}

open class OneOfImplementationGenerator : UniqueGenerator<OneofImplementationGeneratingState> {
    override fun generate(state: OneofImplementationGeneratingState): Boolean {
        state.target.property(
            state.descriptor.name.toCamelCase(),
            state.oneOfClassName().parameterizedBy(TypeVariableName("*")).copy(true)
        ) {
            this += KModifier.OVERRIDE
            mutable()
            initializer("null")
        }
        return true
    }
}

open class OneofFieldImplementationInterceptorGenerator : UniqueGenerator<FieldImplementationGeneratingState>,
    SortableGenerator<FieldImplementationGeneratingState> {
    override val order: Int = -1000

    override fun generate(state: FieldImplementationGeneratingState): Boolean {
        if (!state.descriptor.hasOneofIndex()) return false

        val oneOf = state.parent.descriptor.oneofDeclList[state.descriptor.oneofIndex]

        state.target.property(state.name(), state.mutableFieldType()) {
            this += KModifier.OVERRIDE
            mutable()
            getter {
                if (state.mutableFieldType().isNullable) {
                    addStatement(
                        "return (%N as? %T)?.value",
                        oneOf.name.toCamelCase(),
                        state.oneOfClassName(oneOf)
                    )
                } else {
                    addStatement(
                        "return (%N as? %T)?.value ?: %L",
                        oneOf.name.toCamelCase(),
                        state.oneOfClassName(oneOf),
                        state.defaultValue()
                    )
                }
            }
            setter {
                addParameter("value", state.mutableFieldType())
                addCode(buildCodeBlock {
                    if (state.mutableFieldType().isNullable) {
                        addStatement("%N = value?.let { %T(it) }", oneOf.name.toCamelCase(), state.oneOfClassName(oneOf))
                    } else {
                        addStatement("%N = %T(value)", oneOf.name.toCamelCase(), state.oneOfClassName(oneOf))
                    }
                })
            }
        }

        state.target.function(state.hasFunction()) {
            this += KModifier.OVERRIDE
            returns(Boolean::class.java)
            addStatement("return %N is %T", oneOf.name.toCamelCase(), state.oneOfClassName(oneOf))
        }

        state.target.function(state.clearFunction()) {
            this += KModifier.OVERRIDE
            returns(state.fieldType().copy(true))
            beginControlFlow("return (%N as? %T)?.value?.also", oneOf.name.toCamelCase(), state.oneOfClassName(oneOf))
            addStatement("%N = null", oneOf.name.toCamelCase())
            endControlFlow()
        }

        return true
    }
}