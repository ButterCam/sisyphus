package com.bybutter.sisyphus.protobuf.compiler.rxjava

import com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator
import com.bybutter.sisyphus.protobuf.compiler.RuntimeMethods
import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.constructor
import com.bybutter.sisyphus.protobuf.compiler.core.state.ApiFileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.advance
import com.bybutter.sisyphus.protobuf.compiler.extends
import com.bybutter.sisyphus.protobuf.compiler.function
import com.bybutter.sisyphus.protobuf.compiler.kClass
import com.bybutter.sisyphus.protobuf.compiler.parameter
import com.bybutter.sisyphus.protobuf.compiler.plusAssign
import com.bybutter.sisyphus.protobuf.compiler.property
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock

class RxClientGenerator : GroupedGenerator<ApiFileGeneratingState> {
    override fun generate(state: ApiFileGeneratingState): Boolean {
        for (service in state.descriptor.services) {
            state.target.addType(kClass(service.name()) {
                ClientGeneratingState(state, service, this).advance()
            })
        }
        return true
    }
}

class RxClientBasicGenerator : GroupedGenerator<ClientGeneratingState> {
    override fun generate(state: ClientGeneratingState): Boolean {
        state.target.apply {
            this extends RuntimeTypes.ABSTRACT_REACTIVE_STUB.parameterizedBy(
                state.descriptor.className()
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

                returns(state.descriptor.className())

                addStatement(
                    "return %T(channel, optionsInterceptors, options)",
                    state.descriptor.className()
                )
            }
        }
        return true
    }
}

class RxClientMethodGenerator : GroupedGenerator<ClientGeneratingState> {
    override fun generate(state: ClientGeneratingState): Boolean {
        for (method in state.descriptor.methods) {
            val returnEmpty: Boolean = method.outputMessage().className().simpleName == "Empty"

            // generate client rpc function
            state.target.function(method.name()) {
                when {
                    returnEmpty -> {
                        returns(RuntimeTypes.COMPLETABLE)
                    }
                    method.descriptor.serverStreaming -> {
                        returns(RuntimeTypes.FLOWABLE.parameterizedBy(method.outputMessage().className()))
                    }
                    else -> {
                        returns(RuntimeTypes.SINGLE.parameterizedBy(method.outputMessage().className()))
                    }
                }

                if (method.descriptor.clientStreaming) {
                    addParameter(
                        "input",
                        RuntimeTypes.FLOWABLE.parameterizedBy(method.inputMessage().className())
                    )
                } else {
                    addParameter("input", method.inputMessage().className())
                }

                val returnStatementPostfix = if (returnEmpty) ".ignoreElement()" else ""

                when {
                    !method.descriptor.clientStreaming && !method.descriptor.serverStreaming -> {
                        addStatement("return unaryCall(${method.name()}, input)$returnStatementPostfix")
                    }
                    method.descriptor.clientStreaming && !method.descriptor.serverStreaming -> {
                        addStatement("return clientStreaming(${method.name()}, input)$returnStatementPostfix")
                    }
                    !method.descriptor.clientStreaming && method.descriptor.serverStreaming -> {
                        addStatement("return serverStreaming(${method.name()}, input)$returnStatementPostfix")
                    }
                    method.descriptor.clientStreaming && method.descriptor.serverStreaming -> {
                        addStatement("return bidiStreaming(${method.name()}, input)$returnStatementPostfix")
                    }
                }
            }

            // generate method descriptor
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
                    add(".setType(MethodDescriptor.MethodType.%L)\n", "UNARY")
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
                    add(".setSchemaDescriptor(this)\n")
                    add(".build()\n")
                })
            }
        }
        return true
    }
}
