package com.bybutter.sisyphus.dto

import kotlin.reflect.KProperty

/**
 * Handler for dto property hooks.
 */
interface PropertyHookHandler<T> {
    operator fun invoke(target: Any, value: T, params: Array<out String>, property: KProperty<T?>): T
}
