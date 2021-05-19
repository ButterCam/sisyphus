package com.bybutter.sisyphus.protobuf.primitives

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

val timestampFormatWithoutMillisecond = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
    timeZone = TimeZone.getTimeZone("GMT")
}

val timestampFormatWithMillisecond = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
    timeZone = TimeZone.getTimeZone("GMT")
}

internal fun Timestamp.Companion.parseJvm7(value: String): Timestamp {
    val date = timestampFormatWithMillisecond.parse(value, ParsePosition(0))
        ?: timestampFormatWithoutMillisecond.parse(value)
    return fromDate(date)
}

internal fun Timestamp.Companion.nowJvm7(): Timestamp {
    return fromDate(Date())
}

fun Timestamp.Companion.fromDate(date: Date): Timestamp {
    val millis = date.time
    val seconds = millis / 1000
    val nanos = ((millis % 1000) * 1000000).toInt()
    return Timestamp {
        this.seconds = seconds
        this.nanos = nanos
    }
}

fun Timestamp.toDate(): Date {
    return Date((seconds * 1000) + (nanos / 1000000))
}

internal fun Timestamp.stringJvm7(): String {
    return timestampFormatWithMillisecond.format(toDate())
}
