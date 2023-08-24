package com.bybutter.sisyphus.string

private fun Char.isZeroWidth(): Boolean {
    return isISOControl() || category == CharCategory.FORMAT
}

private fun Char.isHalfWidth(): Boolean {
    return this in '\u0000'..'\u00FF' || this in '\uFF61'..'\uFFDC' || this in '\uFFE8'..'\uFFEE'
}

fun CharSequence.charsWidth(): Int {
    var width = 0
    for (c in this) {
        when {
            c == '\t' -> {
                width = ((width / 4) + 1) * 4
            }

            c.isZeroWidth() -> {}
            c.isHalfWidth() -> width += 1
            else -> width += 2
        }
    }
    return width
}
