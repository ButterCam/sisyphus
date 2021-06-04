package com.bybutter.sisyphus.protobuf.primitives

import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.MessageSupport

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
fun Any.toMessage(): Message<*, *> {
    return (support().reflection.findSupport(this.typeUrl) as MessageSupport<*, *>).parse(this.value)
}
