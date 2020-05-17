package com.bybutter.sisyphus.data

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset

fun String.urlEncode(charset: Charset = Charsets.UTF_8): String {
    return URLEncoder.encode(this, charset)
}

fun String.urlDecode(charset: Charset = Charsets.UTF_8): String {
    return URLDecoder.decode(this, charset)
}
