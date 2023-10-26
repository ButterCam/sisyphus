package com.bybutter.sisyphus.string.case

object PascalCaseFormatter : BaseCaseFormatter() {
    override fun formatWord(
        index: Int,
        word: CharSequence,
    ): CharSequence {
        return buildString {
            append(word.first().uppercase())
            append(word.substring(1).lowercase())
        }
    }
}
