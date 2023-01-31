package com.bybutter.sisyphus.protobuf.primitives

import com.bybutter.sisyphus.protobuf.invoke
import com.bybutter.sisyphus.protobuf.primitives.internal.MutableDuration
import com.bybutter.sisyphus.protobuf.primitives.internal.MutableTimestamp
import com.bybutter.sisyphus.reflect.Reflect
import com.bybutter.sisyphus.string.leftPadding
import com.bybutter.sisyphus.string.rightPadding
import java.math.BigInteger
import java.time.format.DateTimeParseException
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.sign

private const val nanosPerSecond = 1000000000L

private val nanosPerSecondBigInteger = nanosPerSecond.toBigInteger()

private val useJvm7 = !Reflect.classExist("java.time.ZonedDateTime")

operator fun Timestamp.Companion.invoke(value: String): Timestamp {
    return if (useJvm7) {
        Timestamp.parseJvm7(value)
    } else {
        Timestamp.parseJvm8(value)
    }
}

fun Timestamp.Companion.now(): Timestamp {
    return if (useJvm7) {
        Timestamp.nowJvm7()
    } else {
        Timestamp.nowJvm8()
    }
}

fun Timestamp.string(): String {
    return if (useJvm7) {
        stringJvm7()
    } else {
        stringJvm8()
    }
}

fun Timestamp.Companion.tryParse(value: String): Timestamp? {
    return try {
        invoke(value)
    } catch (e: DateTimeParseException) {
        null
    }
}

internal fun Timestamp.Companion.string(seconds: Long, nanos: Int): String {
    return if (useJvm7) {
        stringJvm7(seconds, nanos)
    } else {
        stringJvm8(seconds, nanos)
    }
}

internal fun Timestamp.Companion.parsePayload(value: String): Pair<Long, Int> {
    return if (useJvm7) {
        parsePayloadJvm7(value)
    } else {
        parsePayloadJvm8(value)
    }
}

operator fun Timestamp.Companion.invoke(seconds: Long, nanos: Int = 0): Timestamp {
    return Timestamp {
        this.seconds = seconds
        this.nanos = nanos
        normalized()
    }
}

operator fun Timestamp.Companion.invoke(seconds: Double): Timestamp {
    return Timestamp {
        this.seconds = seconds.toLong()
        this.nanos = ((seconds - seconds.toLong()) * seconds.sign * nanosPerSecond).toInt()
    }
}

operator fun Timestamp.Companion.invoke(nanos: BigInteger): Timestamp {
    return Timestamp {
        this.seconds = (nanos / nanosPerSecondBigInteger).toLong()
        this.nanos = (nanos % nanosPerSecondBigInteger).toInt()
    }
}

private val durationRegex = """^(-)?([0-9]+)(?:\.([0-9]+))?s$""".toRegex()
operator fun Duration.Companion.invoke(value: String): Duration {
    return tryParse(value) ?: throw IllegalArgumentException("Illegal duration value '$value'.")
}

fun Duration.Companion.tryParse(value: String): Duration? {
    val (seconds, nanos) = Duration.tryParsePayload(value) ?: return null
    return Duration(seconds, nanos)
}

internal fun Duration.Companion.tryParsePayload(value: String): Pair<Long, Int>? {
    val result = durationRegex.matchEntire(value) ?: return null

    val sign = if (result.groupValues[1].isEmpty()) 1 else -1
    val seconds = result.groupValues[2].toLong() * sign
    val nanos = result.groupValues[3].rightPadding(9, '0').toInt() * sign

    return seconds to nanos
}

operator fun Duration.Companion.invoke(seconds: Long, nanos: Int = 0): Duration {
    return Duration {
        this.seconds = seconds
        this.nanos = nanos
        normalized()
    }
}

operator fun Duration.Companion.invoke(seconds: Double): Duration {
    return invoke(seconds, TimeUnit.SECONDS)
}

operator fun Duration.Companion.invoke(time: Double, unit: TimeUnit): Duration {
    val nanos = (unit.toNanos(1) * time).toLong()

    return Duration {
        this.seconds = nanos / nanosPerSecond
        this.nanos = (nanos % nanosPerSecond).toInt()
    }
}

operator fun Duration.Companion.invoke(nanos: BigInteger): Duration {
    return Duration {
        this.seconds = (nanos / nanosPerSecondBigInteger).toLong()
        this.nanos = (nanos % nanosPerSecondBigInteger).toInt()
    }
}

operator fun Duration.Companion.invoke(hours: Long, minutes: Long, seconds: Long, nanos: Int = 0): Duration {
    return Duration(
        (
                TimeUnit.HOURS.toNanos(hours) + TimeUnit.MINUTES.toNanos(minutes) + TimeUnit.SECONDS.toNanos(
                    seconds
                ) + nanos
                ).toBigInteger()
    )
}

fun Timestamp.toBigInteger(): BigInteger {
    return BigInteger.valueOf(this.seconds) * BigInteger.valueOf(nanosPerSecond) + BigInteger.valueOf(this.nanos.toLong())
}

fun Timestamp.toTime(unit: TimeUnit): Long {
    val nanos = seconds * nanosPerSecond + nanos
    return unit.convert(nanos, TimeUnit.NANOSECONDS)
}

fun Timestamp.toSeconds(): Long {
    return toTime(TimeUnit.SECONDS)
}

fun Duration.toBigInteger(): BigInteger {
    return BigInteger.valueOf(this.seconds) * BigInteger.valueOf(nanosPerSecond) + BigInteger.valueOf(this.nanos.toLong())
}

fun Duration.toTime(unit: TimeUnit): Long {
    val nanos = seconds * nanosPerSecond + nanos
    return unit.convert(nanos, TimeUnit.NANOSECONDS)
}

fun Duration.toSeconds(): Long {
    return toTime(TimeUnit.SECONDS)
}

operator fun Timestamp.plus(duration: Duration): Timestamp {
    return this {
        seconds += duration.seconds
        nanos += duration.nanos
        normalized()
    }
}

operator fun Timestamp.minus(duration: Duration): Timestamp {
    return this {
        seconds -= duration.seconds
        nanos -= duration.nanos
        normalized()
    }
}

operator fun Timestamp.minus(other: Timestamp): Duration {
    return Duration(this.toBigInteger() - other.toBigInteger())
}

operator fun Timestamp.compareTo(other: Timestamp): Int {
    if (this.seconds != other.seconds) {
        return this.seconds.compareTo(other.seconds)
    }

    return this.nanos.compareTo(other.nanos)
}

operator fun Duration.plus(duration: Duration): Duration {
    return this {
        seconds += duration.seconds
        nanos += duration.nanos
        normalized()
    }
}

operator fun Duration.minus(duration: Duration): Duration {
    return this {
        seconds -= duration.seconds
        nanos -= duration.nanos
        normalized()
    }
}

operator fun Duration.unaryPlus(): Duration {
    return this {}
}

operator fun Duration.unaryMinus(): Duration {
    return this {
        normalized()
        seconds = -seconds
        nanos = -nanos
    }
}

operator fun Duration.compareTo(other: Duration): Int {
    if (this.seconds != other.seconds) {
        return this.seconds.compareTo(other.seconds)
    }

    return this.nanos.compareTo(other.nanos)
}

fun Duration.string(): String = buildString {
    return Duration.string(seconds, nanos)
}

internal fun Duration.Companion.string(seconds: Long, nanos: Int): String = buildString {
    append(seconds)
    if (nanos != 0) {
        append('.')
        append(abs(nanos).toString().leftPadding(9, '0').trimEnd('0'))
    }
    append('s')
}

fun abs(duration: Duration): Duration {
    return duration {
        normalized()
        seconds = abs(seconds)
        nanos = abs(nanos)
    }
}

private fun MutableTimestamp.normalized() {
    if (seconds.sign == 0 || nanos.sign == 0) {
        return
    }

    if (seconds.sign != nanos.sign) {
        seconds += nanos.sign
        nanos = ((nanosPerSecond - abs(nanos)) * seconds.sign).toInt()
    }

    if (nanos >= nanosPerSecond) {
        seconds += nanos / nanosPerSecond
        nanos %= nanosPerSecond.toInt()
    }
}

private fun MutableDuration.normalized() {
    if (seconds.sign == 0 || nanos.sign == 0) {
        return
    }

    if (seconds.sign != nanos.sign) {
        seconds += nanos.sign
        nanos = ((nanosPerSecond - abs(nanos)) * seconds.sign).toInt()
    }

    if (nanos >= nanosPerSecond) {
        seconds += nanos / nanosPerSecond
        nanos %= nanosPerSecond.toInt()
    }
}
