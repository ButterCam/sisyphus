package com.bybutter.sisyphus.data

import kotlin.random.Random

fun randomByteArray(length: Int): ByteArray {
    return Random.nextBytes(length)
}

fun randomByteArray(array: ByteArray, from: Int = 0, to: Int = array.size): ByteArray {
    return Random.nextBytes(array, from, to)
}
