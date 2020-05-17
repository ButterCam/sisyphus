package com.bybutter.sisyphus.rpc

import java.lang.annotation.Inherited

@Inherited
@Target(AnnotationTarget.FUNCTION)
annotation class RpcMethod(
    val name: String,
    val input: RpcBound,
    val output: RpcBound
)
