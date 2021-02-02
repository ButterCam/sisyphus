package com.bybutter.sisyphus.rpc

import com.bybutter.sisyphus.protobuf.EnumSupport
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.MessageSupport
import com.bybutter.sisyphus.protobuf.MutableMessage
import com.bybutter.sisyphus.protobuf.ProtoEnum
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import java.io.InputStream

interface MessageMarshaller<T : Message<T, TM>, TM : MutableMessage<T, TM>> : MethodDescriptor.Marshaller<T>,
    Metadata.BinaryMarshaller<T>

private class MessageMarshallerImpl<T : Message<T, TM>, TM : MutableMessage<T, TM>>(private val support: MessageSupport<T, TM>) :
    MessageMarshaller<T, TM> {
    override fun stream(value: T): InputStream {
        return value.toProto().inputStream()
    }

    override fun parse(stream: InputStream): T {
        return support.parse(stream, Int.MAX_VALUE)
    }

    override fun toBytes(value: T): ByteArray {
        return value.toProto()
    }

    override fun parseBytes(serialized: ByteArray): T {
        return support.parse(serialized)
    }
}

private class EnumMarshallerImpl<T : ProtoEnum>(private val support: EnumSupport<T>) : Metadata.AsciiMarshaller<T> {
    override fun toAsciiString(value: T): String {
        return value.proto
    }

    override fun parseAsciiString(serialized: String): T {
        return support(serialized)
    }
}

fun <T : Message<T, TM>, TM : MutableMessage<T, TM>> MessageSupport<T, TM>.marshaller(): MessageMarshaller<T, TM> {
    return MessageMarshallerImpl(this)
}

fun <T : ProtoEnum> EnumSupport<T>.marshaller(): Metadata.AsciiMarshaller<T> {
    return EnumMarshallerImpl(this)
}

operator fun Status.Companion.invoke(status: io.grpc.Status): Status {
    return Status {
        this.code = status.code.value()
        status.description?.let { this.message = it }
    }
}

operator fun Status.Companion.invoke(exception: Throwable): Status {
    return when (exception) {
        is StatusException -> Status {
            code = exception.status.code.value()
            exception.status.description?.let { message = it }
        }
        is StatusRuntimeException -> Status {
            code = exception.status.code.value()
            exception.status.description?.let { message = it }
        }
        is com.bybutter.sisyphus.rpc.StatusException -> Status {
            code = exception.code
            exception.message?.let { message = it }
        }
        is ClientStatusException -> Status {
            code = exception.status.code.value()
            exception.status.description?.let { message = it }
        }
        else -> Status {
            code = Code.INTERNAL.number
            exception.message?.let { message = it }
        }
    }
}

val STATUS_META_KEY = Metadata.Key.of("grpc-status", Status.marshaller())
