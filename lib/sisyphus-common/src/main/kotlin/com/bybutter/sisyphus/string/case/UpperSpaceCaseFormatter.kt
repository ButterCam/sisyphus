package com.bybutter.sisyphus.string.case

object UpperSpaceCaseFormatter : BaseCaseFormatter() {
    override fun formatWord(index: Int, word: CharSequence): CharSequence {
        return word.toString().toUpperCase()
    }

    override fun appendDelimiter(builder: StringBuilder) {
        builder.append(' ')
    }
}
