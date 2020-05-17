package com.bybutter.sisyphus.rpc

import java.lang.annotation.Inherited
import kotlin.reflect.KClass

@Inherited
@Target(AnnotationTarget.CLASS)
annotation class RpcService(val parent: String, val value: String, val client: KClass<*>)
