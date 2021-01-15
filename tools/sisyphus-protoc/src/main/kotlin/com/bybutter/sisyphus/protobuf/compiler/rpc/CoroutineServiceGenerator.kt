package com.bybutter.sisyphus.protobuf.compiler.rpc

import com.bybutter.sisyphus.protobuf.compiler.RuntimeMethods
import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.companion
import com.bybutter.sisyphus.protobuf.compiler.constructor
import com.bybutter.sisyphus.protobuf.compiler.core.state.ApiFileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.FileParentRegisterGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.InternalFileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.advance
import com.bybutter.sisyphus.protobuf.compiler.extends
import com.bybutter.sisyphus.protobuf.compiler.function
import com.bybutter.sisyphus.protobuf.compiler.getter
import com.bybutter.sisyphus.protobuf.compiler.kClass
import com.bybutter.sisyphus.protobuf.compiler.parameter
import com.bybutter.sisyphus.protobuf.compiler.plusAssign
import com.bybutter.sisyphus.protobuf.compiler.property
import com.bybutter.sisyphus.string.toCamelCase
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.flow.Flow

class CoroutineServiceGenerator : com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator<ApiFileGeneratingState> {
    override fun generate(state: ApiFileGeneratingState): Boolean {
        for (service in state.descriptor.services) {
            state.target.addType(kClass(service.name()) {
                ServiceGeneratingState(state, service, this).advance()
            })
        }
        return true
    }
}

class CoroutineServiceBasicGenerator :
    com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator<ServiceGeneratingState> {
    override fun generate(state: ServiceGeneratingState): Boolean {
        state.target.apply {
            this += KModifier.ABSTRACT
            this extends RuntimeTypes.ABSTRACT_COROUTINE_SERVER_IMPL
            addKdoc(state.descriptor.document())

            constructor {
                parameter("context", CoroutineContext::class) {
                    defaultValue("%T", EmptyCoroutineContext::class)
                }
            }

            addSuperclassConstructorParameter("context")

            function("bindService") {
                this += KModifier.OVERRIDE
                returns(RuntimeTypes.SERVER_SERVICE_DEFINITION)

                addStatement("return %L", buildCodeBlock {
                    add("%T.builder(serviceDescriptor)\n", RuntimeTypes.SERVER_SERVICE_DEFINITION)
                    for (method in state.descriptor.methods) {
                        when {
                            !method.descriptor.clientStreaming && !method.descriptor.serverStreaming -> {
                                add(
                                    ".addMethod(%T.unaryServerMethodDefinition(context, ${method.descriptor.name.toCamelCase()}, ::${method.descriptor.name.toCamelCase()}))",
                                    RuntimeTypes.SERVER_CALLS
                                )
                            }
                            method.descriptor.clientStreaming && !method.descriptor.serverStreaming -> {
                                add(
                                    ".addMethod(%T.clientStreamingServerMethodDefinition(context, ${method.descriptor.name.toCamelCase()}, ::${method.descriptor.name.toCamelCase()}))",
                                    RuntimeTypes.SERVER_CALLS
                                )
                            }
                            !method.descriptor.clientStreaming && method.descriptor.serverStreaming -> {
                                add(
                                    ".addMethod(%T.serverStreamingServerMethodDefinition(context, ${method.descriptor.name.toCamelCase()}, ::${method.descriptor.name.toCamelCase()}))",
                                    RuntimeTypes.SERVER_CALLS
                                )
                            }
                            method.descriptor.clientStreaming && method.descriptor.serverStreaming -> {
                                add(
                                    ".addMethod(%T.bidiStreamingServerMethodDefinition(context, ${method.descriptor.name.toCamelCase()}, ::${method.descriptor.name.toCamelCase()}))",
                                    RuntimeTypes.SERVER_CALLS
                                )
                            }
                        }
                    }
                    add(".build()\n")
                })
            }

            addType(kClass("Client") {
                ClientGeneratingState(state, state.descriptor, this).advance()
            })

            companion {
                this extends state.descriptor.supportClassName()
            }
        }

        return true
    }
}

class CoroutineClientBasicGenerator : com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator<ClientGeneratingState> {
    override fun generate(state: ClientGeneratingState): Boolean {
        state.target.apply {
            this extends RuntimeTypes.ABSTRACT_COROUTINE_STUB.parameterizedBy(
                state.descriptor.className().nestedClass("Client")
            )
            constructor {
                parameter("channel", RuntimeTypes.CHANNEL)
                parameter(
                    "optionsInterceptors",
                    Iterable::class.asClassName().parameterizedBy(RuntimeTypes.CALL_OPTIONS_INTERCEPTOR)
                ) {
                    defaultValue("listOf()")
                }
                parameter("options", RuntimeTypes.CALL_OPTIONS) {
                    defaultValue("%T.DEFAULT", RuntimeTypes.CALL_OPTIONS)
                }
            }

            addSuperclassConstructorParameter("channel")
            addSuperclassConstructorParameter("optionsInterceptors")
            addSuperclassConstructorParameter("options")

            function("build") {
                this += KModifier.OVERRIDE
                parameter("channel", RuntimeTypes.CHANNEL)
                parameter(
                    "optionsInterceptors",
                    Iterable::class.asClassName().parameterizedBy(RuntimeTypes.CALL_OPTIONS_INTERCEPTOR)
                )
                parameter("options", RuntimeTypes.CALL_OPTIONS)

                returns(state.descriptor.className().nestedClass("Client"))

                addStatement(
                    "return %T(channel, optionsInterceptors, options)",
                    state.descriptor.className().nestedClass("Client")
                )
            }
        }
        return true
    }
}

class CoroutineClientMethodGenerator : com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator<ClientGeneratingState> {
    override fun generate(state: ClientGeneratingState): Boolean {
        for (method in state.descriptor.methods) {
            state.target.function(method.name()) {
                if (method.descriptor.serverStreaming) {
                    returns(Flow::class.asClassName().parameterizedBy(method.outputMessage().className()))
                } else {
                    this += KModifier.SUSPEND
                    returns(method.outputMessage().className())
                }

                if (method.descriptor.clientStreaming) {
                    addParameter(
                        "input",
                        Flow::class.asClassName().parameterizedBy(method.inputMessage().className())
                    )
                } else {
                    addParameter("input", method.inputMessage().className())
                }

                parameter("metadata", RuntimeTypes.METADATA) {
                    defaultValue("%T()", RuntimeTypes.METADATA)
                }

                when {
                    !method.descriptor.clientStreaming && !method.descriptor.serverStreaming -> {
                        addStatement(
                            "return unaryCall(%T.${method.name()}, input, metadata)",
                            method.parent.className()
                        )
                    }
                    method.descriptor.clientStreaming && !method.descriptor.serverStreaming -> {
                        addStatement(
                            "return clientStreaming(%T.${method.name()}, input, metadata)",
                            method.parent.className()
                        )
                    }
                    !method.descriptor.clientStreaming && method.descriptor.serverStreaming -> {
                        addStatement(
                            "return serverStreaming(%T.${method.name()}, input, metadata)",
                            method.parent.className()
                        )
                    }
                    method.descriptor.clientStreaming && method.descriptor.serverStreaming -> {
                        addStatement(
                            "return bidiStreaming(%T.${method.name()}, input, metadata)",
                            method.parent.className()
                        )
                    }
                }
            }
        }
        return true
    }
}

class CoroutineServiceMethodGenerator :
    com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator<ServiceGeneratingState> {
    override fun generate(state: ServiceGeneratingState): Boolean {
        for (method in state.descriptor.methods) {
            state.target.function(state.descriptor.name()) {
                this += KModifier.ABSTRACT
                addKdoc(state.descriptor.document())

                if (method.descriptor.serverStreaming) {
                    returns(Flow::class.asClassName().parameterizedBy(method.outputMessage().className()))
                } else {
                    this += KModifier.SUSPEND
                    returns(method.outputMessage().className())
                }

                if (method.descriptor.clientStreaming) {
                    addParameter("input", Flow::class.asClassName().parameterizedBy(method.inputMessage().className()))
                } else {
                    addParameter("input", method.inputMessage().className())
                }
            }
        }
        return true
    }
}

class CoroutineServiceSupportGenerator :
    com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator<InternalFileGeneratingState> {
    override fun generate(state: InternalFileGeneratingState): Boolean {
        for (service in state.descriptor.services) {
            state.target.addType(kClass(service.supportName()) {
                ServiceSupportGeneratingState(state, service, this).advance()
            })
        }
        return true
    }
}

class CoroutineServiceSupportBasicGenerator :
    com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator<ServiceSupportGeneratingState> {
    override fun generate(state: ServiceSupportGeneratingState): Boolean {
        state.target.apply {
            this extends RuntimeTypes.SERVICE_SUPPORT
            this += KModifier.ABSTRACT

            property("name", String::class) {
                this += KModifier.OVERRIDE
                getter {
                    addStatement("return %S", state.descriptor.fullProtoName())
                }
            }

            property("parent", RuntimeTypes.FILE_SUPPORT) {
                this += KModifier.OVERRIDE
                getter {
                    addStatement("return %T", state.descriptor.parent.fileMetadataClassName())
                }
            }

            property("descriptor", RuntimeTypes.SERVICE_DESCRIPTOR_PROTO) {
                this += KModifier.OVERRIDE
                delegate(buildCodeBlock {
                    beginControlFlow("%M", MemberName("kotlin", "lazy"))
                    addStatement(
                        "%T.descriptor.service.first{ it.name == %S }",
                        state.descriptor.parent.fileMetadataClassName(),
                        state.descriptor.descriptor.name
                    )
                    endControlFlow()
                })
            }

            property("serviceDescriptor", RuntimeTypes.SERVICE_DESCRIPTOR) {
                initializer(buildCodeBlock {
                    add("%T.newBuilder(name)\n", RuntimeTypes.SERVICE_DESCRIPTOR)
                    for (method in state.descriptor.methods) {
                        add(".addMethod(%L)\n", method.name())
                    }
                    add(".setSchemaDescriptor(%T)\n", state.descriptor.className())
                    add(".build()\n")
                })
            }
        }
        return true
    }
}

class CoroutineServiceSupportMethodGenerator :
    com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator<ServiceSupportGeneratingState> {
    override fun generate(state: ServiceSupportGeneratingState): Boolean {
        for (method in state.descriptor.methods) {
            state.target.property(
                method.name(),
                RuntimeTypes.METHOD_DESCRIPTOR.parameterizedBy(
                    method.inputMessage().className(),
                    method.outputMessage().className()
                )
            ) {
                initializer(buildCodeBlock {
                    add(
                        "%T.newBuilder<%T,·%T>()\n",
                        RuntimeTypes.METHOD_DESCRIPTOR,
                        method.inputMessage().className(),
                        method.outputMessage().className()
                    )
                    when {
                        !method.descriptor.clientStreaming && !method.descriptor.serverStreaming -> {
                            add(".setType(MethodDescriptor.MethodType.%L)\n", "UNARY")
                        }
                        method.descriptor.clientStreaming && !method.descriptor.serverStreaming -> {
                            add(".setType(MethodDescriptor.MethodType.%L)\n", "CLIENT_STREAMING")
                        }
                        !method.descriptor.clientStreaming && method.descriptor.serverStreaming -> {
                            add(".setType(MethodDescriptor.MethodType.%L)\n", "SERVER_STREAMING")
                        }
                        method.descriptor.clientStreaming && method.descriptor.serverStreaming -> {
                            add(".setType(MethodDescriptor.MethodType.%L)\n", "BIDI_STREAMING")
                        }
                    }
                    add(".setFullMethodName(%S)\n", method.fullProtoName())
                    add(
                        ".setRequestMarshaller(%T.%M())\n",
                        method.inputMessage().className(),
                        RuntimeMethods.MARSHALLER
                    )
                    add(
                        ".setResponseMarshaller(%T.%M())\n",
                        method.outputMessage().className(),
                        RuntimeMethods.MARSHALLER
                    )
                    add(".setSchemaDescriptor(%T)\n", method.parent.className())
                    add(".build()\n")
                })
            }
        }
        return true
    }
}

class CoroutineServiceParentRegisterGenerator :
    com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator<FileParentRegisterGeneratingState> {
    override fun generate(state: FileParentRegisterGeneratingState): Boolean {
        for (service in state.descriptor.services) {
            ServiceRegisterGeneratingState(state, service, state.target).advance()
        }
        return true
    }
}

class CoroutineServiceRegisterGenerator :
    com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator<ServiceRegisterGeneratingState> {
    override fun generate(state: ServiceRegisterGeneratingState): Boolean {
        state.target.addStatement("%T.register(%T)", RuntimeTypes.PROTO_TYPES, state.descriptor.supportClassName())
        return true
    }
}
