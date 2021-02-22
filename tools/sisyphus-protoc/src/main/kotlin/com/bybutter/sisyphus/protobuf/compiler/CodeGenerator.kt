package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.protobuf.compiler.core.generator.*
import com.bybutter.sisyphus.protobuf.compiler.core.state.GeneratingState
import com.bybutter.sisyphus.protobuf.compiler.resourcename.*
import com.bybutter.sisyphus.protobuf.compiler.rpc.*
import com.bybutter.sisyphus.protobuf.compiler.rxjava.*
import com.bybutter.sisyphus.reflect.getTypeArgument
import com.bybutter.sisyphus.reflect.uncheckedCast
import com.bybutter.sisyphus.spi.ServiceLoader
import java.lang.reflect.ParameterizedType
import java.util.*

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
    private val targetType = mutableMapOf<CodeGenerator<*>, Class<*>>()

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
        register(MessageInterfaceFieldBasicGenerator())
        register(MutableMessageInterfaceBasicGenerator())
        register(MutableMessageInterfaceBasicFieldGenerator())
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
        register(MessageCompanionFieldNameConstGenerator())
        register(OneofInterfaceGenerator())
        register(OneofValueBasicGenerator())
        register(OneofKindTypeGenerator())
        register(OneofKindTypeBasicGenerator())
        register(OneofMutableInterfaceGenerator())
        register(OneofImplementationGenerator())
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
        register(ResourceNameGenerator())
        register(MessageResourceNameGenerator())
        register(ResourceNameBasicGenerator())
        register(ResourceNameImplementationGenerator())
        register(ResourceNameCompanionBasicGenerator())
        register(ResourceNameInterfaceFieldGenerator())
        register(ResourceNameMutableInterfaceFieldGenerator())
        register(ResourceNameImplementationFieldGenerator())
        register(ResourceNameOneofImplementationFieldGenerator())
        register(ResourceNameOneofKindTypeBasicGenerator())
        register(ResourceNameMessageFieldWriteFunctionGenerator())
        register(ResourceNameMessageFieldReadFunctionGenerator())
        register(ResourceNameCompanionInvokeGenerator())
        return this
    }

    fun coroutineService(): CodeGenerators {
        register(CoroutineServiceGenerator())
        register(CoroutineServiceSupportGenerator())

        register(CoroutineServiceBasicGenerator())
        register(CoroutineClientBasicGenerator())
        register(CoroutineClientMethodGenerator())
        register(CoroutineServiceMethodGenerator())
        register(CoroutineServiceSupportBasicGenerator())
        register(CoroutineServiceSupportMethodGenerator())
        register(CoroutineServiceParentRegisterGenerator())
        register(CoroutineServiceRegisterGenerator())
        return this
    }

    fun separatedCoroutineService(): CodeGenerators {
        register(SeparatedCoroutineServiceGenerator())
        register(SeparatedCoroutineServiceSupportGenerator())

        register(SeparatedCoroutineServiceApiFileGenerator())
        register(SeparatedCoroutineServiceInternalFileGenerator())
        register(SeparatedCoroutineServiceFileSupportGenerator())
        register(SeparatedCoroutineServiceSupportBasicGenerator())

        coroutineService()
        return this
    }

    fun rxjavaClient(): CodeGenerators {
        register(RxClientGenerator())

        register(RxClientBasicGenerator())
        register(RxClientMethodGenerator())

        return this
    }

    fun separatedRxjavaClient(): CodeGenerators {
        register(SeparatedRxClientApiFileGenerator())
        register(SeparatedRxClientGenerator())

        rxjavaClient()
        return this
    }

    fun spi(): CodeGenerators {
        register(ServiceLoader.load(CodeGenerator::class.java))
        return this
    }

    fun advance(state: GeneratingState<*, *>) {
        val handledGroup = mutableSetOf<String>()

        loop@ for (generator in generators) {
            val targetState = targetType.getOrPut(generator) {
                when (val type = generator.javaClass.getTypeArgument(
                    CodeGenerator::class.java,
                    0
                )) {
                    is Class<*> -> type
                    is ParameterizedType -> type.rawType as Class<*>
                    else -> TODO()
                }
            }

            if (!targetState.isInstance(state)) continue
            when (generator) {
                is GroupedGenerator<*> -> {
                    if (handledGroup.contains(generator.group)) {
                        continue@loop
                    }
                    if (generator.uncheckedCast<CodeGenerator<GeneratingState<*, *>>>().generate(state)) {
                        handledGroup += generator.group
                    }
                }
                else -> {
                    generator.uncheckedCast<CodeGenerator<GeneratingState<*, *>>>().generate(state)
                }
            }
        }
    }
}
