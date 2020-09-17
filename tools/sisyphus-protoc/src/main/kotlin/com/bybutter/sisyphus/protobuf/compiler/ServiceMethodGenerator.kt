package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.api.http
import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.primitives.MethodOptions
import com.bybutter.sisyphus.rpc.RpcBound
import com.bybutter.sisyphus.rpc.RpcMethod
import com.bybutter.sisyphus.string.toCamelCase
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.kotlin.ServerCalls
import kotlinx.coroutines.flow.Flow

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

    fun generateClientStub(): FunSpec {
        val builder = generateClientFunction().toBuilder()
        builder.modifiers.remove(KModifier.ABSTRACT)
        builder.addModifiers(KModifier.OVERRIDE)
        builder.annotations.clear()

        builder.parameters.removeIf { it.name == "metadata" }
        builder.addParameter("metadata", Metadata::class)

        when (methodType) {
            MethodDescriptor.MethodType.UNARY -> {
                builder.addStatement("return unaryCall(%T.%L, input, metadata)", parent.serviceType, kotlinName)
            }
            MethodDescriptor.MethodType.CLIENT_STREAMING -> {
                builder.addStatement("return clientStreaming(%T.%L, input, metadata)", parent.serviceType, kotlinName)
            }
            MethodDescriptor.MethodType.SERVER_STREAMING -> {
                builder.addStatement("return serverStreaming(%T.%L, input, metadata)", parent.serviceType, kotlinName)
            }
            MethodDescriptor.MethodType.BIDI_STREAMING -> {
                builder.addStatement("return bidiStreaming(%T.%L, input, metadata)", parent.serviceType, kotlinName)
            }
            MethodDescriptor.MethodType.UNKNOWN -> throw UnsupportedOperationException("Unknown method type.")
        }

        return builder.build()
    }

    fun generateAbstractFunction(): FunSpec {
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
            builder.addParameter("input", Flow::class.asClassName().parameterizedBy(getTypeNameByProtoName(input)))
        } else {
            builder.addParameter("input", getTypeNameByProtoName(input))
        }

        if (outputStreaming) {
            builder.returns(Flow::class.asClassName().parameterizedBy(getTypeNameByProtoName(output)))
        } else {
            builder.returns(getTypeNameByProtoName(output))
        }

        return builder.build()
    }

    fun generateClientFunction(): FunSpec {
        val builder = generateAbstractFunction().toBuilder()
        builder.addParameter(
                ParameterSpec.builder("metadata", Metadata::class).defaultValue("%T()", Metadata::class).build()
        )
        return builder.build()
    }

    fun generateMethodDefined(): CodeBlock = buildCodeBlock {
        val functionName = when (this@ServiceMethodGenerator.methodType) {
            MethodDescriptor.MethodType.UNARY -> "unaryServerMethodDefinition"
            MethodDescriptor.MethodType.CLIENT_STREAMING -> "clientStreamingServerMethodDefinition"
            MethodDescriptor.MethodType.SERVER_STREAMING -> "serverStreamingServerMethodDefinition"
            MethodDescriptor.MethodType.BIDI_STREAMING -> "bidiStreamingServerMethodDefinition"
            MethodDescriptor.MethodType.UNKNOWN -> TODO()
        }
        addStatement("%T.%L(context, %L, ::%L)", ServerCalls::class, functionName, kotlinName, kotlinName)
    }
}
