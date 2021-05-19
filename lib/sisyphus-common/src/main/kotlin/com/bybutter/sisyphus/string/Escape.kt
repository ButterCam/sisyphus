package com.bybutter.sisyphus.string

fun String.escape(): String = buildString {
    val data = this@escape
    var i = 0

    while (i < data.length) {
        when (val ch = data[i]) {
            '\\' -> append("\\\\")
            '\b' -> append("\\b")
            '\u000C' -> append("\\f")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            '\'' -> append("\\'")
            '\"' -> append("\\\"")
            else -> {
                if (ch.isISOControl()) {
                    if (ch.code < 255) {
                        append("\\")
                        append(ch.code.toString(8))
                    } else {
                        append("\\u")
                        val hex = ch.code.toString(16)
                        repeat(4 - hex.length) {
                            append('0')
                        }
                        append(hex)
                    }
                } else {
                    append(ch)
                }
            }
        }
        i++
    }
}

fun String.unescape(): String = buildString(this.length) {
    val data = this@unescape
    var i = 0

    while (i < data.length) {
        var ch = data[i]
        if (ch != '\\') {
            append(ch)
            i++
            continue
        }

        i++
        ch = data[i]

        when (ch) {
            '\\' -> append('\\')
            'b' -> append('\b')
            'f' -> append('\u000C')
            'n' -> append('\n')
            'r' -> append('\r')
            't' -> append('\t')
            '\"' -> append('\"')
            '\'' -> append('\'')
            'u' -> {
                append("${data[i + 1]}${data[i + 2]}${data[i + 3]}${data[i + 4]}".toInt(16).toChar())
                i += 4
            }
            in '0'..'7' -> {
                var octal = ""
                while ((octal.length < 2 || (octal.length == 2 && octal[0] in '0'..'3')) && data[i] in '0'..'7') {
                    octal += data[i]
                    i++
                }
                append(octal.toInt(8).toChar())
                i--
            }
            else -> {
                append('\\')
                append(ch)
            }
        }
        i++
    }
}
