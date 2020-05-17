package com.bybutter.sisyphus.string

import kotlin.random.Random

private const val base62Table = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

fun randomString(length: Int): String {
    return randomString(length, base62Table)
}

fun Random.randomString(length: Int): String {
    return Random.randomString(length, base62Table)
}

private const val numberTable = "0123456789"

fun randomNumberString(length: Int): String {
    return randomString(length, numberTable)
}

fun Random.randomNumberString(length: Int): String {
    return Random.randomString(length, numberTable)
}

private const val letterTable = "abcdefghijklmnopqrstuvwxyz"

fun randomLetterString(length: Int): String {
    return randomString(length, letterTable)
}

fun Random.randomLetterString(length: Int): String {
    return Random.randomString(length, letterTable)
}

private const val friendlyTable = "123456789abcdefghjkmnpqrstuvwxyz"

fun randomFriendlyString(length: Int): String {
    return randomString(length, friendlyTable)
}

fun Random.randomFriendlyString(length: Int): String {
    return Random.randomString(length, friendlyTable)
}

fun randomString(length: Int, table: CharSequence): String = buildString {
    return Random.randomString(length, table)
}

fun Random.randomString(length: Int, table: CharSequence): String = buildString {
    repeat(length) {
        append(table.random(this@randomString))
    }
}
