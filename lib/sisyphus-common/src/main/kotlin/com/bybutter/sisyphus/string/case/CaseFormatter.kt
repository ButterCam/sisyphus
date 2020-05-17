package com.bybutter.sisyphus.string.case

interface CaseFormatter {
    fun format(words: Iterable<String>): String
}
