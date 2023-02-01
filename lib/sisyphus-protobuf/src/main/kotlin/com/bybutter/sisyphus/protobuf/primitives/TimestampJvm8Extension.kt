package com.bybutter.sisyphus.protobuf.primitives

import java.time.Instant
import java.time.LocalDateTime
import java.time.Month
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

private val defaultOffset = OffsetDateTime.now().offset

fun java.sql.Timestamp.toProto(): Timestamp = Timestamp {
    seconds = TimeUnit.MILLISECONDS.toSeconds(this@toProto.time)
    nanos = this@toProto.nanos
}

fun Timestamp.toSql(): java.sql.Timestamp {
    val result = java.sql.Timestamp(TimeUnit.SECONDS.toMillis(seconds))
    result.nanos = nanos
    return result
}

fun LocalDateTime.toProto(offset: ZoneOffset = defaultOffset): Timestamp = Timestamp {
    seconds = this@toProto.toInstant(offset).epochSecond
    nanos = this@toProto.nano
}

fun Timestamp.toLocalDateTime(offset: ZoneOffset = defaultOffset): LocalDateTime {
    return LocalDateTime.ofEpochSecond(seconds, nanos, offset)
}

fun Timestamp.toInstant(offset: ZoneOffset = defaultOffset): Instant {
    return Instant.ofEpochSecond(seconds, nanos.toLong())
}

fun Timestamp.toZonedDataTime(zone: ZoneId = ZoneId.systemDefault()): ZonedDateTime {
    return ZonedDateTime.ofInstant(toInstant(), zone)
}

fun Timestamp.toOffsetDataTime(offset: ZoneOffset = defaultOffset): OffsetDateTime {
    return OffsetDateTime.of(toLocalDateTime(offset), offset)
}

fun Timestamp.Companion.currentZoneOffset(): ZoneOffset {
    return defaultOffset
}

fun Timestamp.Companion.currentZoneId(): ZoneId {
    return ZoneId.systemDefault()
}

operator fun Timestamp.Companion.invoke(
    year: Int,
    month: Month,
    day: Int,
    hour: Int = 0,
    minute: Int = 0,
    second: Int = 0,
    nano: Int = 0,
    zoneId: ZoneId = ZoneId.systemDefault(),
): Timestamp {
    val instant = ZonedDateTime.of(year, month.value, day, hour, minute, second, nano, zoneId).toInstant()
    return Timestamp {
        this.seconds = instant.epochSecond
        this.nanos = instant.nano
    }
}

operator fun Timestamp.Companion.invoke(
    year: Int,
    month: Int,
    day: Int,
    hour: Int = 0,
    minute: Int = 0,
    second: Int = 0,
    nano: Int = 0,
    zoneId: ZoneId = ZoneId.systemDefault(),
): Timestamp {
    val instant = ZonedDateTime.of(year, month, day, hour, minute, second, nano, zoneId).toInstant()
    return Timestamp {
        this.seconds = instant.epochSecond
        this.nanos = instant.nano
    }
}

operator fun Timestamp.Companion.invoke(instant: Instant): Timestamp {
    return Timestamp {
        this.seconds = instant.epochSecond
        this.nanos = instant.nano
    }
}

fun Timestamp.toInstant(): Instant {
    return Instant.ofEpochSecond(this.seconds, this.nanos.toLong())
}

internal fun Timestamp.Companion.parseJvm8(value: String): Timestamp {
    return Timestamp {
        val instant = ZonedDateTime.parse(value).toInstant()
        seconds = instant.epochSecond
        nanos = instant.nano
    }
}

internal fun Timestamp.Companion.parsePayloadJvm8(value: String): Pair<Long, Int> {
    val instant = ZonedDateTime.parse(value).toInstant()
    return instant.epochSecond to instant.nano
}

internal fun Timestamp.Companion.nowJvm8(): Timestamp {
    val instant = Instant.now()
    return Timestamp {
        this.seconds = instant.epochSecond
        this.nanos = instant.nano
    }
}

internal fun Timestamp.stringJvm8(): String {
    return this.toInstant().toString()
}

internal fun Timestamp.Companion.stringJvm8(seconds: Long, nanos: Int): String {
    return Instant.ofEpochSecond(seconds, nanos.toLong()).toString()
}
