package com.bybutter.sisyphus.string

import kotlin.random.Random

private const val BASE62_TABLE = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

fun randomString(length: Int): String {
    return randomString(length, BASE62_TABLE)
}

fun Random.randomString(length: Int): String {
    return Random.randomString(length, BASE62_TABLE)
}

private const val NUMBER_TABLE = "0123456789"

fun randomNumberString(length: Int): String {
    return randomString(length, NUMBER_TABLE)
}

fun Random.randomNumberString(length: Int): String {
    return Random.randomString(length, NUMBER_TABLE)
}

private const val LETTER_TABLE = "abcdefghijklmnopqrstuvwxyz"

fun randomLetterString(length: Int): String {
    return randomString(length, LETTER_TABLE)
}

fun Random.randomLetterString(length: Int): String {
    return Random.randomString(length, LETTER_TABLE)
}

private const val FRIENDLY_TABLE = "123456789abcdefghjkmnpqrstuvwxyz"

fun randomFriendlyString(length: Int): String {
    return randomString(length, FRIENDLY_TABLE)
}

fun Random.randomFriendlyString(length: Int): String {
    return Random.randomString(length, FRIENDLY_TABLE)
}

fun randomString(
    length: Int,
    table: CharSequence,
): String =
    buildString {
        return Random.randomString(length, table)
    }

fun Random.randomString(
    length: Int,
    table: CharSequence,
): String =
    buildString {
        repeat(length) {
            append(table.random(this@randomString))
        }
    }
