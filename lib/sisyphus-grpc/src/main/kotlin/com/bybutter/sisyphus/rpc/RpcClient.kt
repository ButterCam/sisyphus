package com.bybutter.sisyphus.rpc

import java.lang.annotation.Inherited

@Inherited
@Target(AnnotationTarget.CLASS)
annotation class RpcClient(val parent: String, val value: String)
