package com.bybutter.sisyphus.rpc

annotation class RpcBound(
    val type: String,
    val streaming: Boolean = false
)
