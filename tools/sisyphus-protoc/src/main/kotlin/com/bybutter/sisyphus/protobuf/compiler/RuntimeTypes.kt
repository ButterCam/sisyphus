package com.bybutter.sisyphus.protobuf.compiler

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

object RuntimeAnnotations {
    val PROTOBUF_DEFINITION = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.ProtobufDefinition")
}

object RuntimeTypes {
    val PROTOBUF_BOOSTER = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.ProtobufBooster")

    val MESSAGE = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.Message")

    val PROTO_REFLECTION = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.ProtoReflection")

    val MESSAGE_SUPPORT = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.MessageSupport")

    val MESSAGE_EXTENSION = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.MessageExtension")

    val EXTENSION_SUPPORT = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.ExtensionSupport")

    val FILE_SUPPORT = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.FileSupport")

    val SERVICE_SUPPORT = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.ServiceSupport")

    val MUTABLE_MESSAGE = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.MutableMessage")

    val ABSTRACT_MUTABLE_MESSAGE = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.AbstractMutableMessage")

    val ONE_OF_VALUE = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.OneOfValue")

    val PROTO_ENUM = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.ProtoEnum")

    val ENUM_SUPPORT = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.EnumSupport")

    val PROTO_SUPPORT = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.ProtoSupport")

    val INTERNAL_PROTO_API = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.InternalProtoApi")

    val DESCRIPTOR_PROTO = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.primitives.DescriptorProto")

    val SERVICE_DESCRIPTOR_PROTO =
        ClassName.bestGuess("com.bybutter.sisyphus.protobuf.primitives.ServiceDescriptorProto")

    val FIELD_DESCRIPTOR_PROTO = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto")

    val FILE_DESCRIPTOR_PROTO = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.primitives.FileDescriptorProto")

    val ENUM_DESCRIPTOR_PROTO = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.primitives.EnumDescriptorProto")

    val ABSTRACT_COROUTINE_SERVER_IMPL = ClassName.bestGuess("com.bybutter.sisyphus.rpc.AbstractCoroutineServerImpl")

    val ABSTRACT_COROUTINE_STUB = ClassName.bestGuess("com.bybutter.sisyphus.rpc.AbstractCoroutineStub")

    val ABSTRACT_REACTIVE_STUB = ClassName.bestGuess("com.bybutter.sisyphus.rpc.AbstractReactiveStub")

    val SINGLE = ClassName.bestGuess("io.reactivex.Single")

    val FLOWABLE = ClassName.bestGuess("io.reactivex.Flowable")

    val COMPLETABLE = ClassName.bestGuess("io.reactivex.Completable")

    val CALL_OPTIONS_INTERCEPTOR = ClassName.bestGuess("com.bybutter.sisyphus.rpc.CallOptionsInterceptor")

    val SERVICE_DESCRIPTOR = ClassName.bestGuess("io.grpc.ServiceDescriptor")

    val SERVER_SERVICE_DEFINITION = ClassName.bestGuess("io.grpc.ServerServiceDefinition")

    val SERVER_CALLS = ClassName.bestGuess("io.grpc.kotlin.ServerCalls")

    val METADATA = ClassName.bestGuess("io.grpc.Metadata")

    val CHANNEL = ClassName.bestGuess("io.grpc.Channel")

    val CALL_OPTIONS = ClassName.bestGuess("io.grpc.CallOptions")

    val OPT_IN = ClassName.bestGuess("kotlin.OptIn")

    val METHOD_DESCRIPTOR = ClassName.bestGuess("io.grpc.MethodDescriptor")

    val WRITER = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.coded.Writer")

    val READER = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.coded.Reader")

    val PROTO_TYPES = ClassName.bestGuess("com.bybutter.sisyphus.protobuf.ProtoTypes")

    val RESOURCE_NAME = ClassName.bestGuess("com.bybutter.sisyphus.api.ResourceName")

    val ABSTRACT_RESOURCE_NAME = ClassName.bestGuess("com.bybutter.sisyphus.api.AbstractResourceName")

    val RESOURCE_NAME_SUPPORT = ClassName.bestGuess("com.bybutter.sisyphus.api.ResourceNameSupport")

    val UNKNOWN_RESOURCE_NAME = ClassName.bestGuess("com.bybutter.sisyphus.api.UnknownResourceName")
}

object RuntimeMethods {
    val UNCHECK_CAST = MemberName("com.bybutter.sisyphus.reflect", "uncheckedCast")

    val CONTENT_EQUALS = MemberName("com.bybutter.sisyphus.collection", "contentEquals")

    val MARSHALLER = MemberName("com.bybutter.sisyphus.rpc", "marshaller")

    val LAZY = MemberName("kotlin", "lazy")
}
