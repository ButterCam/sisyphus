package com.bybutter.sisyphus.string.case

object CamelCaseFormatter : BaseCaseFormatter() {
    override fun formatWord(index: Int, word: CharSequence): CharSequence {
        return buildString {
            if (index == 0) {
                return word.toString().toLowerCase()
            } else {
                append(word.first().toUpperCase())
                append(word.substring(1).toLowerCase())
            }
        }
    }
}
