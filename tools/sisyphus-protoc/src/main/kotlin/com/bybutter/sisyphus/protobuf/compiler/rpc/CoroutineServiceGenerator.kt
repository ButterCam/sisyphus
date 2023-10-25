package com.bybutter.sisyphus.protobuf.compiler.rpc

import com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator
import com.bybutter.sisyphus.protobuf.compiler.RuntimeAnnotations
import com.bybutter.sisyphus.protobuf.compiler.RuntimeMethods
import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.beginScope
import com.bybutter.sisyphus.protobuf.compiler.companion
import com.bybutter.sisyphus.protobuf.compiler.constructor
import com.bybutter.sisyphus.protobuf.compiler.core.state.ApiFileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.FileParentGeneratingState
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
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class CoroutineServiceGenerator : GroupedGenerator<ApiFileGeneratingState> {
    override fun generate(state: ApiFileGeneratingState): Boolean {
        for (service in state.descriptor.services) {
            state.target.addType(
                kClass(service.name()) {
                    ServiceGeneratingState(state, service, this).advance()
                },
            )
        }
        return true
    }
}

class CoroutineServiceBasicGenerator : GroupedGenerator<ServiceGeneratingState> {
    override fun generate(state: ServiceGeneratingState): Boolean {
        state.target.apply {
            this += KModifier.ABSTRACT
            this extends RuntimeTypes.ABSTRACT_COROUTINE_SERVER_IMPL
            addKdoc(state.descriptor.document())

            addAnnotation(
                AnnotationSpec.builder(RuntimeAnnotations.PROTOBUF_DEFINITION)
                    .addMember("%S", state.descriptor.fullProtoName())
                    .build(),
            )

            constructor {
                parameter("context", CoroutineContext::class) {
                    defaultValue("%T", EmptyCoroutineContext::class)
                }
            }

            addSuperclassConstructorParameter("context")

            function("bindService") {
                this += KModifier.OVERRIDE
                returns(RuntimeTypes.SERVER_SERVICE_DEFINITION)

                addStatement(
                    "return %L",
                    buildCodeBlock {
                        add("%T.builder(serviceDescriptor)\n", RuntimeTypes.SERVER_SERVICE_DEFINITION)
                        for (method in state.descriptor.methods) {
                            when {
                                !method.descriptor.clientStreaming && !method.descriptor.serverStreaming -> {
                                    add(
                                        ".addMethod(%T.unaryServerMethodDefinition(context, ${
                                            method.descriptor.name.toCamelCase()
                                        }, ::${method.descriptor.name.toCamelCase()}))",
                                        RuntimeTypes.SERVER_CALLS,
                                    )
                                }

                                method.descriptor.clientStreaming && !method.descriptor.serverStreaming -> {
                                    add(
                                        ".addMethod(%T.clientStreamingServerMethodDefinition(context, ${
                                            method.descriptor.name.toCamelCase()
                                        }, ::${method.descriptor.name.toCamelCase()}))",
                                        RuntimeTypes.SERVER_CALLS,
                                    )
                                }

                                !method.descriptor.clientStreaming && method.descriptor.serverStreaming -> {
                                    add(
                                        ".addMethod(%T.serverStreamingServerMethodDefinition(context, ${
                                            method.descriptor.name.toCamelCase()
                                        }, ::${method.descriptor.name.toCamelCase()}))",
                                        RuntimeTypes.SERVER_CALLS,
                                    )
                                }

                                method.descriptor.clientStreaming && method.descriptor.serverStreaming -> {
                                    add(
                                        ".addMethod(%T.bidiStreamingServerMethodDefinition(context, ${
                                            method.descriptor.name.toCamelCase()
                                        }, ::${method.descriptor.name.toCamelCase()}))",
                                        RuntimeTypes.SERVER_CALLS,
                                    )
                                }
                            }
                        }
                        add(".build()\n")
                    },
                )
            }

            addType(
                kClass("Client") {
                    ClientGeneratingState(state, state.descriptor, this).advance()
                },
            )

            function("support") {
                this += KModifier.OVERRIDE
                returns(state.descriptor.className().nestedClass("Companion"))
                addStatement("return %T", state.descriptor.className())
            }

            companion {
                this extends state.descriptor.supportClassName()
                ServiceCompanionGeneratingState(state, state.descriptor, this).advance()
            }
        }

        return true
    }
}

class CoroutineClientBasicGenerator : GroupedGenerator<ClientGeneratingState> {
    override fun generate(state: ClientGeneratingState): Boolean {
        state.target.apply {
            this extends
                RuntimeTypes.ABSTRACT_COROUTINE_STUB.parameterizedBy(
                    state.descriptor.className().nestedClass("Client"),
                )
            constructor {
                parameter("channel", RuntimeTypes.CHANNEL)
                parameter(
                    "optionsInterceptors",
                    Iterable::class.asClassName().parameterizedBy(RuntimeTypes.CALL_OPTIONS_INTERCEPTOR),
                ) {
                    defaultValue("listOf()")
                }
                parameter("callOptions", RuntimeTypes.CALL_OPTIONS) {
                    defaultValue("%T.DEFAULT", RuntimeTypes.CALL_OPTIONS)
                }
            }

            addSuperclassConstructorParameter("channel")
            addSuperclassConstructorParameter("optionsInterceptors")
            addSuperclassConstructorParameter("callOptions")

            function("build") {
                this += KModifier.OVERRIDE
                parameter("channel", RuntimeTypes.CHANNEL)
                parameter(
                    "optionsInterceptors",
                    Iterable::class.asClassName().parameterizedBy(RuntimeTypes.CALL_OPTIONS_INTERCEPTOR),
                )
                parameter("callOptions", RuntimeTypes.CALL_OPTIONS)

                returns(state.descriptor.className().nestedClass("Client"))

                addStatement(
                    "return %T(channel, optionsInterceptors, callOptions)",
                    state.descriptor.className().nestedClass("Client"),
                )
            }

            function("support") {
                this += KModifier.OVERRIDE
                returns(state.descriptor.className().nestedClass("Companion"))
                addStatement("return %T", state.descriptor.className())
            }
        }
        return true
    }
}

class CoroutineClientMethodGenerator : GroupedGenerator<ClientGeneratingState> {
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
                        Flow::class.asClassName().parameterizedBy(method.inputMessage().className()),
                    )
                } else {
                    addParameter("input", method.inputMessage().className())
                }

                when {
                    !method.descriptor.clientStreaming && !method.descriptor.serverStreaming -> {
                        addStatement(
                            "return unaryCall(%T.${method.name()}, input, %T())",
                            method.parent.className(),
                            RuntimeTypes.METADATA,
                        )
                    }

                    method.descriptor.clientStreaming && !method.descriptor.serverStreaming -> {
                        addStatement(
                            "return clientStreaming(%T.${method.name()}, input, %T())",
                            method.parent.className(),
                            RuntimeTypes.METADATA,
                        )
                    }

                    !method.descriptor.clientStreaming && method.descriptor.serverStreaming -> {
                        addStatement(
                            "return serverStreaming(%T.${method.name()}, input, %T())",
                            method.parent.className(),
                            RuntimeTypes.METADATA,
                        )
                    }

                    method.descriptor.clientStreaming && method.descriptor.serverStreaming -> {
                        addStatement(
                            "return bidiStreaming(%T.${method.name()}, input, %T())",
                            method.parent.className(),
                            RuntimeTypes.METADATA,
                        )
                    }
                }
            }

            if (!method.descriptor.clientStreaming) {
                state.target.function(method.name()) {
                    this += KModifier.INLINE
                    addParameter("block", LambdaTypeName.get(method.inputMessage().mutableClassName(), listOf(), UNIT))
                    if (method.descriptor.serverStreaming) {
                        returns(Flow::class.asClassName().parameterizedBy(method.outputMessage().className()))
                    } else {
                        this += KModifier.SUSPEND
                        returns(method.outputMessage().className())
                    }
                    addStatement("return ${method.name()}(%T { block() })", method.inputMessage().className())
                }
            }
        }
        return true
    }
}

class CoroutineServiceMethodGenerator : GroupedGenerator<ServiceGeneratingState> {
    override fun generate(state: ServiceGeneratingState): Boolean {
        for (method in state.descriptor.methods) {
            state.target.function(method.name()) {
                this += KModifier.ABSTRACT
                addKdoc(method.document())

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

class CoroutineServiceSupportGenerator : GroupedGenerator<InternalFileGeneratingState> {
    override fun generate(state: InternalFileGeneratingState): Boolean {
        for (service in state.descriptor.services) {
            state.target.addType(
                kClass(service.supportName()) {
                    ServiceSupportGeneratingState(state, service, this).advance()
                },
            )
        }
        return true
    }
}

class CoroutineServiceSupportBasicGenerator : GroupedGenerator<ServiceSupportGeneratingState> {
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
                delegate(
                    buildCodeBlock {
                        beginControlFlow("%M", RuntimeMethods.LAZY)
                        addStatement(
                            "%T.descriptor.service.first{ it.name == %S }",
                            state.descriptor.parent.fileMetadataClassName(),
                            state.descriptor.descriptor.name,
                        )
                        endControlFlow()
                    },
                )
            }

            property("serviceDescriptor", RuntimeTypes.SERVICE_DESCRIPTOR) {
                delegate(
                    buildCodeBlock {
                        beginScope("%M", RuntimeMethods.LAZY) {
                            add(
                                "%T.newBuilder(%S)\n",
                                RuntimeTypes.SERVICE_DESCRIPTOR,
                                state.descriptor.fullProtoName().trim('.'),
                            )
                            for (method in state.descriptor.methods) {
                                add(".addMethod(%L)\n", method.name())
                            }
                            add(".setSchemaDescriptor(%T)\n", state.descriptor.className())
                            add(".build()\n")
                        }
                    },
                )
            }
        }
        return true
    }
}

class CoroutineServiceSupportMethodGenerator : GroupedGenerator<ServiceSupportGeneratingState> {
    override fun generate(state: ServiceSupportGeneratingState): Boolean {
        for (method in state.descriptor.methods) {
            state.target.property(
                method.name(),
                RuntimeTypes.METHOD_DESCRIPTOR.parameterizedBy(
                    method.inputMessage().className(),
                    method.outputMessage().className(),
                ),
            ) {
                initializer(
                    buildCodeBlock {
                        add(
                            "%T.newBuilder<%T,·%T>()\n",
                            RuntimeTypes.METHOD_DESCRIPTOR,
                            method.inputMessage().className(),
                            method.outputMessage().className(),
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
                        add(".setFullMethodName(%S)\n", method.fullProtoName().substring(1))
                        add(
                            ".setRequestMarshaller(%T.%M())\n",
                            method.inputMessage().className(),
                            RuntimeMethods.MARSHALLER,
                        )
                        add(
                            ".setResponseMarshaller(%T.%M())\n",
                            method.outputMessage().className(),
                            RuntimeMethods.MARSHALLER,
                        )
                        add(".setSchemaDescriptor(%T)\n", method.parent.className())
                        add(".build()\n")
                    },
                )
            }
        }
        return true
    }
}

class CoroutineServiceChildGenerator : GroupedGenerator<FileParentGeneratingState> {
    override fun generate(state: FileParentGeneratingState): Boolean {
        for (service in state.descriptor.services) {
            state.target += service.className()
        }
        return true
    }
}
