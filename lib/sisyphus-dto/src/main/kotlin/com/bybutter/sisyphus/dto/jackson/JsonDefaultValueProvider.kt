package com.bybutter.sisyphus.dto.jackson

import com.bybutter.sisyphus.dto.DefaultValueProvider
import com.bybutter.sisyphus.dto.ModelProxy
import com.bybutter.sisyphus.jackson.Json
import com.bybutter.sisyphus.reflect.jvm
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaType

class JsonDefaultValueProvider : DefaultValueProvider<Any?> {
    override fun getValue(
        proxy: ModelProxy,
        param: String,
        property: KProperty<Any?>
    ): Any? {
        return Json.deserialize(param, property.returnType.javaType.jvm)
    }
}
