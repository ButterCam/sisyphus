package com.bybutter.sisyphus.protobuf.compiler.util

fun escapeDoc(doc: String): String {
    return doc.replace("%", "%%").replace("/*", "&#47;*").replace("*/", "*&#47;")
}

fun makeTag(field: Int, wireType: Int): Int {
    return (field shl 3) or wireType
}
