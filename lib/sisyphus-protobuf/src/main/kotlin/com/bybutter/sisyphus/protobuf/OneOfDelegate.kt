package com.bybutter.sisyphus.protobuf

import kotlin.reflect.KProperty

interface OneOfDelegate<T : OneOfValue<*>, TM : Message<*, *>> {
    var value: T?

    operator fun <T> getValue(ref: TM, property: KProperty<*>): T

    operator fun <T> setValue(ref: TM, property: KProperty<*>, value: T)

    fun <T> clear(ref: TM, property: KProperty<*>): T?

    fun has(ref: TM, property: KProperty<*>): Boolean
}
