package com.bybutter.sisyphus.protobuf.primitives

import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.MessageSupport
import com.bybutter.sisyphus.protobuf.ProtoReflection

/**
 * Wrap message to [Any].
 */
fun Message<*, *>.toAny(): Any {
    if (this is Any) return this
    return Any {
        this.typeUrl = this@toAny.typeUrl()
        this.value = this@toAny.toProto()
    }
}

/**
 * Unwrap any.
 */
fun Any.toMessage(): Message<*, *>? {
    val support = ProtoReflection.findSupport(this.typeUrl) as? MessageSupport<*, *> ?: return null
    return support.parse(this.value)
}

/**
 * An annotation to mark current Any resolved failed when parsing protos.
 * When encoding this message, should not re-wrap it to Any.
 */
internal object AnyResolveFailed
