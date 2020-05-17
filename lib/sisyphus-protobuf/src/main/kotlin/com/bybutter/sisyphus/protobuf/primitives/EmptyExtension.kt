package com.bybutter.sisyphus.protobuf.primitives

/**
 * Get the singleton empty message.
 */
val Empty.Companion.INSTANCE: Empty by lazy {
    Empty {}
}
