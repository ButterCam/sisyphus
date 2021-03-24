package com.bybutter.sisyphus.middleware.jdbc

import org.jooq.types.UByte
import org.jooq.types.UInteger
import org.jooq.types.ULong
import org.jooq.types.UShort
import java.math.BigInteger
import java.sql.Timestamp
import java.util.concurrent.TimeUnit

fun String.toUInt(): UInteger {
    return UInteger.valueOf(this)
}

fun String.toULong(): ULong {
    return ULong.valueOf(this)
}

fun String.toUShort(): UShort {
    return UShort.valueOf(this)
}

fun String.toUByte(): UByte {
    return UByte.valueOf(this)
}

fun Int.toUInt(): UInteger {
    return UInteger.valueOf(this)
}

fun Long.toULong(): ULong {
    return ULong.valueOf(this)
}

fun BigInteger.toULong(): ULong {
    return ULong.valueOf(this)
}

fun Int.toUByte(): UByte {
    return UByte.valueOf(this)
}

fun Byte.toUByte(): UByte {
    return UByte.valueOf(this)
}

fun Long.toUByte(): UByte {
    return UByte.valueOf(this)
}

fun Short.toUByte(): UByte {
    return UByte.valueOf(this)
}

fun Short.toUShort(): UShort {
    return UShort.valueOf(this)
}

fun Int.toUShort(): UShort {
    return UShort.valueOf(this)
}

fun Long.toTimestamp(unit: TimeUnit = TimeUnit.SECONDS): Timestamp {
    return Timestamp(unit.toMillis(this))
}

fun ULong.toTimestamp(unit: TimeUnit = TimeUnit.SECONDS): Timestamp {
    return Timestamp(unit.toMillis(this.toLong()))
}

fun Timestamp.toLong(unit: TimeUnit = TimeUnit.SECONDS): Long {
    return unit.convert(this.time, TimeUnit.MILLISECONDS)
}

fun Timestamp.toULong(unit: TimeUnit = TimeUnit.SECONDS): ULong {
    return unit.convert(this.time, TimeUnit.MILLISECONDS).toULong()
}
