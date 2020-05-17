package com.bybutter.sisyphus.string.case

object ScreamingSnakeCaseFormatter : BaseCaseFormatter() {
    override fun formatWord(index: Int, word: CharSequence): CharSequence {
        return word.toString().toUpperCase()
    }

    override fun appendDelimiter(builder: StringBuilder) {
        builder.append('_')
    }
}
