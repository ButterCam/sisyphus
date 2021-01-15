package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.protobuf.compiler.core.generator.ApiFileGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.EnumApiGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.EnumBasicGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.EnumParentRegisterGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.EnumRegisterGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.EnumSupportBasicGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.EnumSupportGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.ExtensionApiGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.ExtensionDefinitionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.ExtensionParentRegisterGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.ExtensionRegisterGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.ExtensionSupportBasicGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.ExtensionSupportGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.FileSupportGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.InternalFileGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageApiGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageClearFieldInCurrentFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageClearFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageCloneMutableFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageComputeHashCodeFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageEqualsMessageFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageFieldClearFieldInCurrentFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageFieldComputeHashCodeFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageFieldEqualsFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageFieldGetFieldInCurrentFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageFieldGetPropertyFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageFieldHasFieldInCurrentFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageFieldReadFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageFieldSetFieldInCurrentFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageFieldWriteFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageGetFieldInCurrentFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageGetPropertyFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageHasFieldInCurrentFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageImplementationBasicGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageImplementationFieldBasicGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageImplementationFieldGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageInterfaceBasicGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageInterfaceFieldGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageInternalGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageMergeWithFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageParentRegisterGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageReadFieldFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageRegisterGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageSetFieldInCurrentFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageSupportBasicGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageSupportFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MessageWriteFieldsFunctionGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MutableMessageInterfaceBasicGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.MutableMessageInterfaceFieldGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.NestEnumParentRegisterGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.NestExtensionParentRegisterGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.NestMessageParentRegisterGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.NestedEnumGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.NestedEnumSupportGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.NestedExtensionApiGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.NestedExtensionSupportGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.OneOfImplementationGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.OneOfInterfaceGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.OneOfMutableInterfaceGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.OneofFieldImplementationInterceptorGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.generator.OneofValueBasicGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.state.GeneratingState
import com.bybutter.sisyphus.protobuf.compiler.rpc.CoroutineClientBasicGenerator
import com.bybutter.sisyphus.protobuf.compiler.rpc.CoroutineClientMethodGenerator
import com.bybutter.sisyphus.protobuf.compiler.rpc.CoroutineServiceBasicGenerator
import com.bybutter.sisyphus.protobuf.compiler.rpc.CoroutineServiceGenerator
import com.bybutter.sisyphus.protobuf.compiler.rpc.CoroutineServiceMethodGenerator
import com.bybutter.sisyphus.protobuf.compiler.rpc.CoroutineServiceParentRegisterGenerator
import com.bybutter.sisyphus.protobuf.compiler.rpc.CoroutineServiceRegisterGenerator
import com.bybutter.sisyphus.protobuf.compiler.rpc.CoroutineServiceSupportBasicGenerator
import com.bybutter.sisyphus.protobuf.compiler.rpc.CoroutineServiceSupportGenerator
import com.bybutter.sisyphus.protobuf.compiler.rpc.CoroutineServiceSupportMethodGenerator
import com.bybutter.sisyphus.reflect.getTypeArgument
import com.bybutter.sisyphus.reflect.uncheckedCast
import com.bybutter.sisyphus.spi.ServiceLoader
import java.lang.reflect.ParameterizedType
import java.util.Stack

interface CodeGenerator<T : GeneratingState<*, *>> {
    fun generate(state: T): Boolean
}

interface SortableGenerator<T : GeneratingState<*, *>> : CodeGenerator<T> {
    val order: Int
}

interface GroupedGenerator<T : GeneratingState<*, *>> : CodeGenerator<T> {
    val group: String
        get() = this.javaClass.canonicalName
}

interface DependentGenerator<T : GeneratingState<*, *>> : CodeGenerator<T> {
    fun dependencies(): List<CodeGenerator<*>>
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
        this.generators += resolveDependencies(generators)
        this.generators.sortBy {
            (it as? SortableGenerator<*>)?.order ?: 0
        }
        return this
    }

    private fun resolveDependencies(generators: Iterable<CodeGenerator<*>>): List<CodeGenerator<*>> {
        val result = mutableListOf<CodeGenerator<*>>()
        val resolving = Stack<CodeGenerator<*>>().apply {
            this.addAll(generators.reversed())
        }

        while (resolving.isNotEmpty()) {
            val current = resolving.pop()
            result += current

            if (current is DependentGenerator<*>) {
                resolving.addAll(current.dependencies())
            }
        }

        return result
    }

    fun clear(): CodeGenerators {
        generators.clear()
        return this
    }

    fun basic(): CodeGenerators {
        register(EnumApiGenerator())
        register(NestedEnumGenerator())
        register(EnumBasicGenerator())
        register(EnumSupportGenerator())
        register(NestedEnumSupportGenerator())
        register(EnumSupportBasicGenerator())
        register(ExtensionApiGenerator())
        register(NestedExtensionApiGenerator())
        register(ExtensionSupportGenerator())
        register(NestedExtensionSupportGenerator())
        register(ExtensionDefinitionGenerator())
        register(ExtensionSupportBasicGenerator())
        register(MessageInterfaceFieldGenerator())
        register(MutableMessageInterfaceFieldGenerator())
        register(MessageImplementationFieldGenerator())
        register(MessageImplementationFieldBasicGenerator())
        register(ApiFileGenerator())
        register(InternalFileGenerator())
        register(FileSupportGenerator())
        register(MessageApiGenerator())
        register(MessageInterfaceBasicGenerator())
        register(MessageInternalGenerator())
        register(MutableMessageInterfaceBasicGenerator())
        register(MessageImplementationBasicGenerator())
        register(MessageSupportBasicGenerator())
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
        register(OneOfInterfaceGenerator())
        register(OneofValueBasicGenerator())
        register(OneOfMutableInterfaceGenerator())
        register(OneOfImplementationGenerator())
        register(OneofFieldImplementationInterceptorGenerator())
        register(MessageParentRegisterGenerator())
        register(NestMessageParentRegisterGenerator())
        register(EnumParentRegisterGenerator())
        register(NestEnumParentRegisterGenerator())
        register(ExtensionParentRegisterGenerator())
        register(NestExtensionParentRegisterGenerator())
        register(MessageRegisterGenerator())
        register(EnumRegisterGenerator())
        register(ExtensionRegisterGenerator())
        return this
    }

    fun resourceName(): CodeGenerators {
        return this
    }

    fun coroutineService(): CodeGenerators {
        register(CoroutineServiceGenerator())
        register(CoroutineServiceBasicGenerator())
        register(CoroutineClientBasicGenerator())
        register(CoroutineClientMethodGenerator())
        register(CoroutineServiceMethodGenerator())
        register(CoroutineServiceSupportGenerator())
        register(CoroutineServiceSupportBasicGenerator())
        register(CoroutineServiceSupportMethodGenerator())
        register(CoroutineServiceParentRegisterGenerator())
        register(CoroutineServiceRegisterGenerator())
        return this
    }

    fun spi(): CodeGenerators {
        register(ServiceLoader.load(CodeGenerator::class.java))
        return this
    }

    fun advance(state: GeneratingState<*, *>) {
        val handledGroup = mutableSetOf<String>()

        loop@ for (generator in generators) {
            val targetState = when (val type = generator.javaClass.getTypeArgument(
                CodeGenerator::class.java,
                0
            )) {
                is Class<*> -> type
                is ParameterizedType -> type.rawType as Class<*>
                else -> TODO()
            }
            if (!targetState.isInstance(state)) continue

            when (generator) {
                is GroupedGenerator<*> -> {
                    if (handledGroup.contains(generator.group)) {
                        continue@loop
                    }
                    if (generator.uncheckedCast<CodeGenerator<GeneratingState<*, *>>>()
                            .generate(state)
                    ) {
                        handledGroup += generator.group
                    }
                }
                else -> {
                    generator.uncheckedCast<CodeGenerator<GeneratingState<*, *>>>()
                        .generate(state)
                }
            }
        }
    }
}
