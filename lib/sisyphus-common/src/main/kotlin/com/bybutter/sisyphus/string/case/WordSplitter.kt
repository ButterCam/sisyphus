package com.bybutter.sisyphus.string.case

interface WordSplitter {
    fun split(string: CharSequence): List<String>
}
