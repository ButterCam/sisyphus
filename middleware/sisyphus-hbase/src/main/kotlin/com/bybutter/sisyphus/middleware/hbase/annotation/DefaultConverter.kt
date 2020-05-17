package com.bybutter.sisyphus.middleware.hbase.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
internal annotation class DefaultConverter(val target: KClass<*>)
