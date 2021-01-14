package com.bybutter.sisyphus.protobuf.compiler.generator

import com.bybutter.sisyphus.protobuf.compiler.generating.Generating
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.ApiFileGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.EnumGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.EnumSupportGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.EnumValueGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.ExtensionFieldGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.ExtensionFieldSupportGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.FieldGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.FieldImplementationGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.ImplementationFileGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageApiGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageClearFieldInCurrentFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageClearFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageCloneMutableFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageComputeHashCodeFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageEqualsMessageFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageFieldClearFieldInCurrentFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageFieldComputeHashCodeFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageFieldEqualsFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageFieldGetFieldInCurrentFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageFieldGetPropertyFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageFieldHasFieldInCurrentFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageFieldReadFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageFieldSetFieldInCurrentFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageFieldWriteFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageGetFieldInCurrentFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageGetPropertyFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageHasFieldInCurrentFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageImplementationGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageMergeWithFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageReadFieldFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageSetFieldInCurrentFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageSupportFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageSupportGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MessageWriteFieldsFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MutableFieldGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MutableMessageGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.MutableOneOfGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.OneOfGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.OneOfImplementationGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.OneofFieldImplementationInterceptorGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.basic.RegisterGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.rpc.CoroutineClientMethodGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.rpc.CoroutineServiceGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.rpc.CoroutineServiceMethodGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.rpc.CoroutineServiceRegisterGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.rpc.CoroutineServiceSupportGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.rpc.CoroutineServiceSupportMethodGenerator
import com.bybutter.sisyphus.reflect.getTypeArgument
import com.bybutter.sisyphus.reflect.uncheckedCast
import com.bybutter.sisyphus.spi.ServiceLoader
import java.lang.reflect.ParameterizedType

interface CodeGenerator<T : Generating<*, *, *>> {
    fun generate(state: T): Boolean
}

interface SortableGenerator<T : Generating<*, *, *>> : CodeGenerator<T> {
    val order: Int
}

interface UniqueGenerator<T : Generating<*, *, *>> : CodeGenerator<T> {
    val name: String
        get() = this.javaClass.canonicalName
}

class CodeGenerators {
    private val generators = mutableListOf<CodeGenerator<*>>()

    fun register(generator: CodeGenerator<*>): CodeGenerators {
        return register(listOf(generator))
    }

    fun register(vararg generators: CodeGenerator<*>): CodeGenerators {
        return register(generators.toList())
    }

    fun register(generators: Iterable<CodeGenerator<*>>): CodeGenerators {
        this.generators += generators
        this.generators.sortBy {
            (it as? SortableGenerator<*>)?.order ?: 0
        }
        val existedGenerators = mutableSetOf<String>()
        this.generators.filter {
            if (it is UniqueGenerator<*>) {
                (it.name !in existedGenerators).apply {
                    existedGenerators += it.name
                }
            } else {
                true
            }
        }
        return this
    }

    fun clear(): CodeGenerators {
        generators.clear()
        return this
    }

    fun basic(): CodeGenerators {
        register(EnumGenerator())
        register(EnumSupportGenerator())
        register(EnumValueGenerator())
        register(ExtensionFieldGenerator())
        register(ExtensionFieldSupportGenerator())
        register(FieldGenerator())
        register(MutableFieldGenerator())
        register(FieldImplementationGenerator())
        register(ApiFileGenerator())
        register(ImplementationFileGenerator())
        register(MessageSupportFunctionGenerator())
        register(MessageMergeWithFunctionGenerator())
        register(MessageCloneMutableFunctionGenerator())
        register(MessageClearFunctionGenerator())
        register(MessageClearFieldInCurrentFunctionGenerator())
        register(MessageFieldClearFieldInCurrentFunctionGenerator())
        register(MessageGetFieldInCurrentFunctionGenerator())
        register(MessageFieldGetFieldInCurrentFunctionGenerator())
        register(MessageGetPropertyFunctionGenerator())
        register(MessageFieldGetPropertyFunctionGenerator())
        register(MessageSetFieldInCurrentFunctionGenerator())
        register(MessageFieldSetFieldInCurrentFunctionGenerator())
        register(MessageHasFieldInCurrentFunctionGenerator())
        register(MessageFieldHasFieldInCurrentFunctionGenerator())
        register(MessageEqualsMessageFunctionGenerator())
        register(MessageFieldEqualsFunctionGenerator())
        register(MessageComputeHashCodeFunctionGenerator())
        register(MessageFieldComputeHashCodeFunctionGenerator())
        register(MessageWriteFieldsFunctionGenerator())
        register(MessageFieldWriteFunctionGenerator())
        register(MessageReadFieldFunctionGenerator())
        register(MessageFieldReadFunctionGenerator())
        register(MessageApiGenerator())
        register(MutableMessageGenerator())
        register(MessageImplementationGenerator())
        register(MessageSupportGenerator())
        register(OneOfGenerator())
        register(MutableOneOfGenerator())
        register(OneOfImplementationGenerator())
        register(OneofFieldImplementationInterceptorGenerator())
        register(RegisterGenerator())
        return this
    }

    fun resourceName(): CodeGenerators {
        return this
    }

    fun coroutineService(): CodeGenerators {
        register(CoroutineServiceGenerator())
        register(CoroutineServiceMethodGenerator())
        register(CoroutineClientMethodGenerator())
        register(CoroutineServiceSupportGenerator())
        register(CoroutineServiceSupportMethodGenerator())
        register(CoroutineServiceRegisterGenerator())
        return this
    }

    fun spi(): CodeGenerators {
        register(ServiceLoader.load(CodeGenerator::class.java))
        return this
    }

    fun advance(state: Generating<*, *, *>) {
        for (generator in generators) {
            val targetState = when (val type = generator.javaClass.getTypeArgument(CodeGenerator::class.java, 0)) {
                is Class<*> -> type
                is ParameterizedType -> type.rawType as Class<*>
                else -> TODO()
            }
            if (!targetState.isInstance(state)) continue
            if (generator.uncheckedCast<CodeGenerator<Generating<*, *, *>>>().generate(state)) {
                break
            }
        }
    }
}