package com.bybutter.sisyphus.string.case

abstract class BaseCaseFormatter : CaseFormatter {
    protected open fun formatWord(
        index: Int,
        word: CharSequence,
    ): CharSequence {
        return word
    }

    protected open fun appendDelimiter(builder: StringBuilder) {
    }

    override fun format(words: Iterable<String>): String {
        return buildString {
            for ((index, word) in words.withIndex()) {
                if (isNotEmpty()) {
                    appendDelimiter(this)
                }
                append(formatWord(index, word))
            }
        }
    }
}
