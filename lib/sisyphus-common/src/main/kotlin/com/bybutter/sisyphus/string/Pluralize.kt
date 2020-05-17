package com.bybutter.sisyphus.string

import com.bybutter.sisyphus.string.case.CaseFormat
import com.bybutter.sisyphus.string.case.CommonWordSplitter
import com.bybutter.sisyphus.string.pluralize.PluralizeUtil

val String.isSingular: Boolean
    get() {
        val words = this.splitWords().toMutableList()
        if (words.isEmpty()) return false
        return PluralizeUtil.isSingular(words.last())
    }

val String.isPlural: Boolean
    get() {
        val words = this.splitWords().toMutableList()
        if (words.isEmpty()) return false
        return PluralizeUtil.isPlural(words.last())
    }

fun String.pluralize(count: Int): String {
    return if (count == 1) {
        singular()
    } else {
        plural()
    }
}

fun String.plural(): String {
    val case = CaseFormat.bestGuess(this)
    val words = this.splitWords().toMutableList()
    if (words.isEmpty()) return this

    words[words.lastIndex] = PluralizeUtil.plural(words.last())
    return case.formatter.format(words)
}

fun String.singular(): String {
    val case = CaseFormat.bestGuess(this)
    val words = this.splitWords().toMutableList()
    if (words.isEmpty()) return this

    words[words.lastIndex] = PluralizeUtil.singular(words.last())
    return case.formatter.format(words)
}

private fun String.splitWords(): List<String> {
    return CommonWordSplitter.split(this)
}
