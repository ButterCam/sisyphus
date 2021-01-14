package com.bybutter.sisyphus.protobuf.compiler.generator.rpc

import com.bybutter.sisyphus.protobuf.compiler.RuntimeMethods
import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.companion
import com.bybutter.sisyphus.protobuf.compiler.constructor
import com.bybutter.sisyphus.protobuf.compiler.extends
import com.bybutter.sisyphus.protobuf.compiler.function
import com.bybutter.sisyphus.protobuf.compiler.generating.EnumGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.ExtensionFieldGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.FileGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.MessageGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.ServiceGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.advance
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.ApiFileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.ClientMethodGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.ImplementationFileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.RegisterGenerating
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.ServiceGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.ServiceMethodGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.ServiceSupportGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.ServiceSupportMethodGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.className
import com.bybutter.sisyphus.protobuf.compiler.generating.compiler
import com.bybutter.sisyphus.protobuf.compiler.generating.document
import com.bybutter.sisyphus.protobuf.compiler.generating.file
import com.bybutter.sisyphus.protobuf.compiler.generating.fileMetadataClassName
import com.bybutter.sisyphus.protobuf.compiler.generating.fullProtoName
import com.bybutter.sisyphus.protobuf.compiler.generating.inputClassName
import com.bybutter.sisyphus.protobuf.compiler.generating.name
import com.bybutter.sisyphus.protobuf.compiler.generating.outputClassName
import com.bybutter.sisyphus.protobuf.compiler.generating.supportClassName
import com.bybutter.sisyphus.protobuf.compiler.generating.supportName
import com.bybutter.sisyphus.protobuf.compiler.generator.SortableGenerator
import com.bybutter.sisyphus.protobuf.compiler.generator.UniqueGenerator
import com.bybutter.sisyphus.protobuf.compiler.getter
import com.bybutter.sisyphus.protobuf.compiler.kClass
import com.bybutter.sisyphus.protobuf.compiler.kInterface
import com.bybutter.sisyphus.protobuf.compiler.parameter
import com.bybutter.sisyphus.protobuf.compiler.plusAssign
import com.bybutter.sisyphus.protobuf.compiler.property
import com.bybutter.sisyphus.string.toCamelCase
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import io.grpc.MethodDescriptor
import io.grpc.ServerServiceDefinition
import io.grpc.ServiceDescriptor
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class CoroutineServiceGenerator : UniqueGenerator<ServiceGeneratingState> {
    override fun generate(state: ServiceGeneratingState): Boolean {
        state.target.apply {
            addType(kClass(state.name()) {
                this += KModifier.ABSTRACT
                this extends RuntimeTypes.ABSTRACT_COROUTINE_SERVER_IMPL
                addKdoc(state.document())

                constructor {
                    parameter("context", CoroutineContext::class) {
                        defaultValue("%T", EmptyCoroutineContext::class)
                    }
                }
                addSuperclassConstructorParameter("context")

                for (method in state.descriptor.methodList) {
                    ServiceMethodGeneratingState(state, method, this).advance()
                }

                function("bindService") {
                    this += KModifier.OVERRIDE
                    returns(RuntimeTypes.SERVER_SERVICE_DEFINITION)

                    addStatement("return %L", buildCodeBlock {
                        add("%T.builder(serviceDescriptor)\n", RuntimeTypes.SERVER_SERVICE_DEFINITION)
                        for (method in state.descriptor.methodList) {
                            when {
                                !method.clientStreaming && !method.serverStreaming -> {
                                    add(".addMethod(%T.unaryServerMethodDefinition(context, ${method.name.toCamelCase()}, ::${method.name.toCamelCase()}))", RuntimeTypes.SERVER_CALLS)
                                }
                                method.clientStreaming && !method.serverStreaming -> {
                                    add(".addMethod(%T.clientStreamingServerMethodDefinition(context, ${method.name.toCamelCase()}, ::${method.name.toCamelCase()}))", RuntimeTypes.SERVER_CALLS)
                                }
                                !method.clientStreaming && method.serverStreaming -> {
                                    add(".addMethod(%T.serverStreamingServerMethodDefinition(context, ${method.name.toCamelCase()}, ::${method.name.toCamelCase()}))", RuntimeTypes.SERVER_CALLS)
                                }
                                method.clientStreaming && method.serverStreaming -> {
                                    add(".addMethod(%T.bidiStreamingServerMethodDefinition(context, ${method.name.toCamelCase()}, ::${method.name.toCamelCase()}))", RuntimeTypes.SERVER_CALLS)
                                }
                            }
                        }
                        add(".build()\n")
                    })
                }

                addType(kClass("Client") {
                    this extends RuntimeTypes.ABSTRACT_COROUTINE_STUB.parameterizedBy(state.className().nestedClass("Client"))
                    constructor {
                        parameter("channel", RuntimeTypes.CHANNEL)
                        parameter("optionsInterceptors", Iterable::class.asClassName().parameterizedBy(RuntimeTypes.CALL_OPTIONS_INTERCEPTOR)) {
                            defaultValue("listOf()")
                        }
                        parameter("options", RuntimeTypes.CALL_OPTIONS) {
                            defaultValue("%T.DEFAULT", RuntimeTypes.CALL_OPTIONS)
                        }
                    }

                    addSuperclassConstructorParameter("channel")
                    addSuperclassConstructorParameter("optionsInterceptors")
                    addSuperclassConstructorParameter("options")

                    for (method in state.descriptor.methodList) {
                        ClientMethodGeneratingState(state, method, this).advance()
                    }

                    function("build") {
                        this += KModifier.OVERRIDE
                        parameter("channel", RuntimeTypes.CHANNEL)
                        parameter("optionsInterceptors", Iterable::class.asClassName().parameterizedBy(RuntimeTypes.CALL_OPTIONS_INTERCEPTOR))
                        parameter("options", RuntimeTypes.CALL_OPTIONS)

                        returns(state.className().nestedClass("Client"))

                        addStatement("return %T(channel, optionsInterceptors, options)", state.className().nestedClass("Client"))
                    }
                })

                companion {
                    this extends state.supportClassName()
                }
            })
        }
        return true
    }
}

class CoroutineServiceMethodGenerator: UniqueGenerator<ServiceMethodGeneratingState> {
    override fun generate(state: ServiceMethodGeneratingState): Boolean {
        state.target.apply {
            function(state.name()) {
                this += KModifier.ABSTRACT
                addKdoc(state.document())

                if(state.descriptor.serverStreaming) {
                    returns(Flow::class.asClassName().parameterizedBy(state.outputClassName()))
                } else {
                    this += KModifier.SUSPEND
                    returns(state.outputClassName())
                }

                if(state.descriptor.clientStreaming) {
                    addParameter("input", Flow::class.asClassName().parameterizedBy(state.inputClassName()))
                } else {
                    addParameter("input", state.inputClassName())
                }
            }
        }
        return true
    }
}

class CoroutineServiceSupportGenerator: UniqueGenerator<ServiceSupportGeneratingState> {
    override fun generate(state: ServiceSupportGeneratingState): Boolean {
        state.target.apply {
            addType(kClass(state.supportName()) {
                this extends RuntimeTypes.SERVICE_SUPPORT
                this += KModifier.ABSTRACT

                property("name", String::class) {
                    this += KModifier.OVERRIDE
                    getter {
                        addStatement("return %S", state.fullProtoName())
                    }
                }

                property("parent", RuntimeTypes.FILE_SUPPORT) {
                    this += KModifier.OVERRIDE
                    getter {
                        addStatement("return %T", state.parent.fileMetadataClassName())
                    }
                }

                property("descriptor", RuntimeTypes.SERVICE_DESCRIPTOR_PROTO) {
                    this += KModifier.OVERRIDE
                    delegate(buildCodeBlock {
                        beginControlFlow("%M", MemberName("kotlin", "lazy"))
                        addStatement(
                            "%T.descriptor.service.first{ it.name == %S }",
                            state.parent.fileMetadataClassName(),
                            state.descriptor.name
                        )
                        endControlFlow()
                    })
                }

                for (method in state.descriptor.methodList) {
                    ServiceSupportMethodGeneratingState(state, method, this).advance()
                }

                property("serviceDescriptor", RuntimeTypes.SERVICE_DESCRIPTOR) {
                    initializer(buildCodeBlock {
                        add("%T.newBuilder(name)\n", RuntimeTypes.SERVICE_DESCRIPTOR)
                        for (method in state.descriptor.methodList) {
                            add(".addMethod(%L)\n", method.name.toCamelCase())
                        }
                        add(".setSchemaDescriptor(%T)\n", state.className())
                        add(".build()\n")
                    })
                }
            })
        }
        return true
    }
}

class CoroutineServiceSupportMethodGenerator: UniqueGenerator<ServiceSupportMethodGeneratingState> {
    override fun generate(state: ServiceSupportMethodGeneratingState): Boolean {
        state.target.apply {
            property(state.name(), RuntimeTypes.METHOD_DESCRIPTOR.parameterizedBy(state.inputClassName(), state.outputClassName())) {
                initializer(buildCodeBlock {
                    add("%T.newBuilder<%T,·%T>()\n", RuntimeTypes.METHOD_DESCRIPTOR, state.inputClassName(), state.outputClassName())
                    when {
                        !state.descriptor.clientStreaming && !state.descriptor.serverStreaming -> {
                            add(".setType(MethodDescriptor.MethodType.%L)\n", "UNARY")
                        }
                        state.descriptor.clientStreaming && !state.descriptor.serverStreaming -> {
                            add(".setType(MethodDescriptor.MethodType.%L)\n", "CLIENT_STREAMING")
                        }
                        !state.descriptor.clientStreaming && state.descriptor.serverStreaming -> {
                            add(".setType(MethodDescriptor.MethodType.%L)\n", "SERVER_STREAMING")
                        }
                        state.descriptor.clientStreaming && state.descriptor.serverStreaming -> {
                            add(".setType(MethodDescriptor.MethodType.%L)\n", "BIDI_STREAMING")
                        }
                    }
                    add(".setFullMethodName(%S)\n", state.fullProtoName())
                    add(".setRequestMarshaller(%T.%M())\n", state.inputClassName(), RuntimeMethods.MARSHALLER)
                    add(".setResponseMarshaller(%T.%M())\n", state.outputClassName(), RuntimeMethods.MARSHALLER)
                    add(".setSchemaDescriptor(%T)\n", state.parent.className())
                    add(".build()\n")
                })
            }
        }
        return true
    }
}

class CoroutineClientMethodGenerator: UniqueGenerator<ClientMethodGeneratingState> {
    override fun generate(state: ClientMethodGeneratingState): Boolean {
        state.target.apply {
            function(state.name()) {
                if(state.descriptor.serverStreaming) {
                    returns(Flow::class.asClassName().parameterizedBy(state.compiler().protoClassName(state.descriptor.outputType)))
                } else {
                    this += KModifier.SUSPEND
                    returns(state.compiler().protoClassName(state.descriptor.outputType))
                }

                if(state.descriptor.clientStreaming) {
                    addParameter("input", Flow::class.asClassName().parameterizedBy(state.compiler().protoClassName(state.descriptor.inputType)))
                } else {
                    addParameter("input", state.compiler().protoClassName(state.descriptor.inputType))
                }
                parameter("metadata", RuntimeTypes.METADATA) {
                    defaultValue("%T()", RuntimeTypes.METADATA)
                }

                when {
                    !state.descriptor.clientStreaming && !state.descriptor.serverStreaming -> {
                        addStatement("return unaryCall(%T.${state.name()}, input, metadata)", state.parent.className())
                    }
                    state.descriptor.clientStreaming && !state.descriptor.serverStreaming -> {
                        addStatement("return clientStreaming(%T.${state.name()}, input, metadata)", state.parent.className())
                    }
                    !state.descriptor.clientStreaming && state.descriptor.serverStreaming -> {
                        addStatement("return serverStreaming(%T.${state.name()}, input, metadata)", state.parent.className())
                    }
                    state.descriptor.clientStreaming && state.descriptor.serverStreaming -> {
                        addStatement("return bidiStreaming(%T.${state.name()}, input, metadata)", state.parent.className())
                    }
                }

            }
        }
        return true
    }
}

open class CoroutineServiceRegisterGenerator : UniqueGenerator<RegisterGenerating<*, *>> {
    override fun generate(state: RegisterGenerating<*, *>): Boolean {
        when (state) {
            is ServiceGenerating<*, *> -> {
                state.target.addStatement("%T.register(%T)", RuntimeTypes.PROTO_TYPES, state.className())
            }
            else -> return false
        }
        return true
    }
}