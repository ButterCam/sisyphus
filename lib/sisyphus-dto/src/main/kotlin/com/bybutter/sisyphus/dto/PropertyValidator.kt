package com.bybutter.sisyphus.dto

import kotlin.reflect.KProperty

interface PropertyValidator<T> {
    fun verify(proxy: ModelProxy, value: T, params: Array<out String>, property: KProperty<T?>): Exception?
}
