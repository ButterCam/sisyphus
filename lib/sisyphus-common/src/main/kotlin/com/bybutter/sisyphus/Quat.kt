package com.bybutter.sisyphus

data class Quat<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
) {
    override fun toString(): String = "($first, $second, $third, $fourth)"
}

fun <T> Quat<T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth)
