package com.bybutter.sisyphus.string.case

object DotCaseFormatter : BaseCaseFormatter() {
    override fun formatWord(
        index: Int,
        word: CharSequence,
    ): CharSequence {
        return word.toString().lowercase()
    }

    override fun appendDelimiter(builder: StringBuilder) {
        builder.append('.')
    }
}
