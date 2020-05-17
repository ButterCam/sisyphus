package com.bybutter.sisyphus.string

/**
 * Returns a character at the given [index] or `0` if the [index] is out of bounds of this char sequence.
 */
@Suppress("ConvertTwoComparisonsToRangeCheck")
fun CharSequence.getOrZero(index: Int): Char {
    return if (index >= 0 && index <= lastIndex) get(index) else 0.toChar()
}

fun String.leftPadding(size: Int, char: Char = ' '): String {
    if (this.length < size) {
        return "${char.toString().repeat(size - this.length)}$this"
    }
    return this
}

fun String.rightPadding(size: Int, char: Char = ' '): String {
    if (this.length < size) {
        return "$this${char.toString().repeat(size - this.length)}"
    }
    return this
}
