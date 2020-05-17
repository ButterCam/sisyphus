package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.api.http
import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.primitives.MethodOptions
import com.bybutter.sisyphus.rpc.ManyToManyCall
import com.bybutter.sisyphus.rpc.ManyToOneCall
import com.bybutter.sisyphus.rpc.RpcBound
import com.bybutter.sisyphus.rpc.RpcMethod
import com.bybutter.sisyphus.rpc.ServerCallHandlers
import com.bybutter.sisyphus.rpc.asStreamObserver
import com.bybutter.sisyphus.string.toCamelCase
import com.google.common.util.concurrent.ListenableFuture
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import io.grpc.MethodDescriptor
import io.grpc.ServerCallHandler
import io.grpc.stub.ClientCalls
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.guava.await

class ServiceMethodGenerator(override val parent: ServiceGenerator, val descriptor: DescriptorProtos.MethodDescriptorProto) : ProtobufElement() {
    override val kotlinName: String = descriptor.name.toCamelCase()
    override val protoName: String = descriptor.name

    private val input: String = descriptor.inputType
    private val inputStreaming = descriptor.clientStreaming
    private val inputClassName: TypeName by lazy {
        ClassName.bestGuess(getElementByProtoName(input)!!.fullKotlinName)
    }
    private val inputProto: MessageGenerator by lazy {
        getElementByProtoName(input) as MessageGenerator
    }

    private val output: String = descriptor.outputType
    private val outputStreaming = descriptor.serverStreaming
    private val outputClassName: TypeName by lazy {
        ClassName.bestGuess(getElementByProtoName(output)!!.fullKotlinName)
    }

    private val httpRule = MethodOptions.parse(descriptor.options.toByteArray()).http

    private val methodType = when {
        inputStreaming && outputStreaming -> {
            MethodDescriptor.MethodType.BIDI_STREAMING
        }
        inputStreaming -> {
            MethodDescriptor.MethodType.CLIENT_STREAMING
        }
        outputStreaming -> {
            MethodDescriptor.MethodType.SERVER_STREAMING
        }
        else -> {
            MethodDescriptor.MethodType.UNARY
        }
    }

    var path: List<Int> = listOf()
        private set

    override val documentation: String by lazy {
        val location = ensureParent<FileGenerator>().descriptor.sourceCodeInfo.locationList.firstOrNull {
            it.pathList.contentEquals(path)
        } ?: return@lazy ""

        listOf(location.leadingComments, location.trailingComments).filter { it.isNotBlank() }.joinToString("\n\n")
    }

    override fun init() {
        super.init()
        val parent = parent
        path = parent.path + listOf(DescriptorProtos.ServiceDescriptorProto.METHOD_FIELD_NUMBER, parent.descriptor.methodList.indexOf(descriptor))
    }

    fun generateInfo(): PropertySpec {
        return PropertySpec.Companion.builder(kotlinName, MethodDescriptor::class.asTypeName().parameterizedBy(inputClassName, outputClassName))
            .initializer(buildCodeBlock {
                add("%T.newBuilder<%T,·%T>()\n", MethodDescriptor::class.asTypeName(), inputClassName, outputClassName)
                add(".setType(MethodDescriptor.MethodType.%L)\n", methodType)
                add(".setFullMethodName(%S)\n", MethodDescriptor.generateFullMethodName(parent.fullProtoName, protoName))
                add(".setRequestMarshaller(%T)\n", inputClassName)
                add(".setResponseMarshaller(%T)\n", outputClassName)
                add(".setSchemaDescriptor(%N)\n", MemberName(ensureParent<FileGenerator>().fullKotlinName, ensureParent<FileGenerator>().fileMeta))
                add(".build()\n")
            })
            .build()
    }

    fun generateClient(): FunSpec {
        val builder = FunSpec.builder(kotlinName)
            .addKdoc(escapeDoc(documentation))
            .addModifiers(KModifier.ABSTRACT)
            .addAnnotation(
                AnnotationSpec.builder(RpcMethod::class)
                    .addMember("name = %S", protoName)
                    .addMember("input = %T(type = %S, streaming = %L)", RpcBound::class, input, inputStreaming)
                    .addMember("output = %T(type = %S, streaming = %L)", RpcBound::class, output, outputStreaming)
                    .build()
            )

        when (methodType) {
            MethodDescriptor.MethodType.UNARY -> {
                builder.addModifiers(KModifier.SUSPEND)
                builder.addParameter("input", inputClassName)
                builder.returns(outputClassName)
            }
            MethodDescriptor.MethodType.CLIENT_STREAMING -> {
                builder.returns(ManyToOneCall::class.asClassName().parameterizedBy(inputClassName, outputClassName))
            }
            MethodDescriptor.MethodType.SERVER_STREAMING -> {
                builder.addParameter("input", inputClassName)
                builder.returns(ReceiveChannel::class.asClassName().parameterizedBy(outputClassName))
            }
            MethodDescriptor.MethodType.BIDI_STREAMING -> {
                builder.returns(ManyToManyCall::class.asClassName().parameterizedBy(inputClassName, outputClassName))
            }
            MethodDescriptor.MethodType.UNKNOWN -> throw UnsupportedOperationException("Unknown method type.")
        }
        return builder.build()
    }

    fun generateClientStub(): FunSpec {
        val builder = generateClient().toBuilder()
        builder.modifiers.remove(KModifier.ABSTRACT)
        builder.addModifiers(KModifier.OVERRIDE)
        builder.annotations.clear()

        builder.addCode(buildCodeBlock {
            addStatement("val call = channel.newCall(%T.%L, callOptions)", parent.serviceType, kotlinName)
            when (methodType) {
                MethodDescriptor.MethodType.UNARY -> {
                    addStatement("return %T.futureUnaryCall(call, input).%M()", ClientCalls::class.asTypeName(), ListenableFuture<*>::await.asMemberName())
                }
                MethodDescriptor.MethodType.CLIENT_STREAMING -> {
                    addStatement("val defer = %T()", CompletableDeferred::class.asTypeName().parameterizedBy(outputClassName))
                    addStatement("val request = %T.asyncClientStreamingCall(call, defer.%M())", ClientCalls::class.asTypeName(), CompletableDeferred<*>::asStreamObserver.asMemberName())
                    addStatement("return %T(request, defer)", ManyToOneCall::class.asTypeName())
                }
                MethodDescriptor.MethodType.SERVER_STREAMING -> {
                    addStatement("val channel = %T(%T.UNLIMITED)", Channel::class.asTypeName().parameterizedBy(outputClassName), Channel::class)
                    addStatement("%T.asyncServerStreamingCall(call, input, channel.%M())", ClientCalls::class.asTypeName(), Channel<*>::asStreamObserver.asMemberName())
                    addStatement("return channel")
                }
                MethodDescriptor.MethodType.BIDI_STREAMING -> {
                    addStatement("val channel = %T(%T.UNLIMITED)", Channel::class.asTypeName().parameterizedBy(outputClassName), Channel::class)
                    addStatement("val request = %T.asyncBidiStreamingCall(call, channel.%M())", ClientCalls::class.asTypeName(), Channel<*>::asStreamObserver.asMemberName())
                    addStatement("return %T(request, channel)", ManyToManyCall::class.asTypeName())
                }
                MethodDescriptor.MethodType.UNKNOWN -> throw UnsupportedOperationException("Unknown method type.")
            }
        })

        return builder.build()
    }

    fun generateService(): FunSpec {
        val builder = FunSpec.builder(kotlinName)
            .addKdoc(escapeDoc(documentation))
            .addModifiers(KModifier.ABSTRACT)
            .addAnnotation(
                AnnotationSpec.builder(RpcMethod::class)
                    .addMember("name = %S", protoName)
                    .addMember("input = %T(type = %S, streaming = %L)", RpcBound::class, input, inputStreaming)
                    .addMember("output = %T(type = %S, streaming = %L)", RpcBound::class, output, outputStreaming)
                    .build()
            )

        when (methodType) {
            MethodDescriptor.MethodType.UNARY, MethodDescriptor.MethodType.CLIENT_STREAMING -> {
                builder.addModifiers(KModifier.SUSPEND)
            }
            else -> {
            }
        }

        if (inputStreaming) {
            builder.addParameter("input", ReceiveChannel::class.asClassName().parameterizedBy(getTypeNameByProtoName(input)))
        } else {
            builder.addParameter("input", getTypeNameByProtoName(input))
        }

        if (outputStreaming) {
            builder.returns(ReceiveChannel::class.asClassName().parameterizedBy(getTypeNameByProtoName(output)))
        } else {
            builder.returns(getTypeNameByProtoName(output))
        }

        return builder.build()
    }

    fun generateServiceHandler(): PropertySpec {
        val handler = "${kotlinName}Handler"

        val builder = PropertySpec.Companion.builder(handler, ServerCallHandler::class.asTypeName().parameterizedBy(inputClassName, outputClassName))
            .initializer(buildCodeBlock {
                when (methodType) {
                    MethodDescriptor.MethodType.UNARY -> {
                        beginScope("%T.asyncUnaryCall", ServerCallHandlers::class) {
                            addStatement("this.%L(it)", kotlinName)
                        }
                    }
                    MethodDescriptor.MethodType.CLIENT_STREAMING -> {
                        beginScope("%T.asyncClientStreamingCall", ServerCallHandlers::class) {
                            addStatement("this.%L(it)", kotlinName)
                        }
                    }
                    MethodDescriptor.MethodType.SERVER_STREAMING -> {
                        beginScope("%T.asyncServerStreamingCall", ServerCallHandlers::class) {
                            addStatement("this.%L(it)", kotlinName)
                        }
                    }
                    MethodDescriptor.MethodType.BIDI_STREAMING -> {
                        beginScope("%T.asyncBidiStreamingCall", ServerCallHandlers::class) {
                            addStatement("this.%L(it)", kotlinName)
                        }
                    }
                    MethodDescriptor.MethodType.UNKNOWN -> throw UnsupportedOperationException("Unknown method type.")
                }
            })
        return builder.build()
    }
}
