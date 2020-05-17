package com.bybutter.sisyphus.protobuf.compiler

fun escapeDoc(doc: String): String {
    return doc.replace("%", "%%").replace("/*", "&#47;*").replace("*/", "*&#47;")
}
