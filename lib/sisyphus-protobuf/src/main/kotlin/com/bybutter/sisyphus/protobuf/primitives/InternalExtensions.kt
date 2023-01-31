package com.bybutter.sisyphus.protobuf.primitives

import com.bybutter.sisyphus.protobuf.InternalProtoApi
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoReflection
import com.bybutter.sisyphus.protobuf.findMessageSupport

internal fun Message<*, *>.wrapAny(): Message<*, *> {
    // If this message is a resolve failed Any, do not wrap it, just return it directly.
    if (this.type() == Any.name && annotations().contains(AnyResolveFailed)) return this

    return ProtoReflection.findMessageSupport(Any.name).invoke {
        this[Any.TYPE_URL_FIELD_NUMBER] = this@wrapAny.support().typeUrl()
        this[Any.VALUE_FIELD_NUMBER] = this@wrapAny.toProto()
    }
}

@OptIn(InternalProtoApi::class)
internal fun Message<*, *>.unwrapAny(): Message<*, *> {
    if (this.support().name != Any.name) {
        throw IllegalArgumentException("Message is not a Any.")
    }
    return try {
        ProtoReflection.findMessageSupport(this[Any.TYPE_URL_FIELD_NUMBER]).parse(this[Any.VALUE_FIELD_NUMBER])
    } catch (e: Exception) {
        cloneMutable().apply { annotation(AnyResolveFailed) }
    }
}

internal fun Message<*, *>.timestampString(): String {
    if (this.support().name != Timestamp.name) {
        throw IllegalArgumentException("Message is not a Timestamp.")
    }

    val seconds = get<Long>(Timestamp.SECONDS_FIELD_NUMBER)
    val nanos = get<Int>(Timestamp.NANOS_FIELD_NUMBER)

    return Timestamp.string(seconds, nanos)

}

internal fun Message<*, *>.durationString(): String {
    if (this.support().name != Duration.name) {
        throw IllegalArgumentException("Message is not a Timestamp.")
    }

    val seconds = get<Long>(Duration.SECONDS_FIELD_NUMBER)
    val nanos = get<Int>(Duration.NANOS_FIELD_NUMBER)

    return Duration.string(seconds, nanos)
}

internal fun Message<*, *>.fieldMaskString(): String {
    if (this.support().name != FieldMask.name) {
        throw IllegalArgumentException("Message is not a FieldMask.")
    }

    return this.get<List<String>>(FieldMask.PATHS_FIELD_NUMBER).joinToString(",")
}
