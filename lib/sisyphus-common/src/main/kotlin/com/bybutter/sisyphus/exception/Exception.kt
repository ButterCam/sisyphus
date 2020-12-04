package com.bybutter.sisyphus.exception

import java.io.PrintWriter
import java.io.StringWriter

fun Throwable.stackTraceAsString(): String {
    return StringWriter().apply {
        printStackTrace(PrintWriter(this))
    }.toString()
}
