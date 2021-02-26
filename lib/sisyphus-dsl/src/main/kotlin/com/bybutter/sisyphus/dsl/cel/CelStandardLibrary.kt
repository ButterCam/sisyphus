package com.bybutter.sisyphus.dsl.cel

import com.bybutter.sisyphus.protobuf.CustomProtoType
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoEnum
import com.bybutter.sisyphus.protobuf.primitives.BoolValue
import com.bybutter.sisyphus.protobuf.primitives.BytesValue
import com.bybutter.sisyphus.protobuf.primitives.DoubleValue
import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.FloatValue
import com.bybutter.sisyphus.protobuf.primitives.Int32Value
import com.bybutter.sisyphus.protobuf.primitives.Int64Value
import com.bybutter.sisyphus.protobuf.primitives.ListValue
import com.bybutter.sisyphus.protobuf.primitives.NullValue
import com.bybutter.sisyphus.protobuf.primitives.StringValue
import com.bybutter.sisyphus.protobuf.primitives.Struct
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.UInt32Value
import com.bybutter.sisyphus.protobuf.primitives.UInt64Value
import com.bybutter.sisyphus.protobuf.primitives.Value
import com.bybutter.sisyphus.protobuf.primitives.compareTo
import com.bybutter.sisyphus.protobuf.primitives.invoke
import com.bybutter.sisyphus.protobuf.primitives.minus
import com.bybutter.sisyphus.protobuf.primitives.plus
import com.bybutter.sisyphus.protobuf.primitives.toInstant
import com.bybutter.sisyphus.protobuf.primitives.toSeconds
import com.bybutter.sisyphus.protobuf.primitives.toTime
import com.bybutter.sisyphus.security.base64
import java.time.DayOfWeek
import java.time.ZoneOffset
import java.util.Arrays
import java.util.concurrent.TimeUnit

open class CelStandardLibrary {

    // !_
    open fun logicalNot(value: Boolean): Boolean {
        return !value
    }

    // -_
    open fun negate(value: Long): Long {
        return -value
    }

    // -_
    open fun negate(value: Double): Double {
        return -value
    }

    // _==_ _!=_
    open fun equals(left: Any?, right: Any?): Boolean {
        return left == right
    }

    // _%_
    open fun rem(left: Long, right: Long): Long {
        return left % right
    }

    // _%_
    open fun rem(left: ULong, right: ULong): ULong {
        return left % right
    }

    // _&&_
    open fun logicalAnd(left: Boolean, right: Boolean): Boolean {
        return left && right
    }

    // _*_
    open fun times(left: Long, right: Long): Long {
        return left * right
    }

    // _*_
    open fun times(left: ULong, right: ULong): ULong {
        return left * right
    }

    // _*_
    open fun times(left: Double, right: Double): Double {
        return left * right
    }

    // _+_
    open fun plus(left: Long, right: Long): Long {
        return left + right
    }

    // _+_
    open fun plus(left: ULong, right: ULong): ULong {
        return left + right
    }

    // _+_
    open fun plus(left: Double, right: Double): Double {
        return left + right
    }

    // _+_
    open fun plus(left: String, right: String): String {
        return left + right
    }

    // _+_
    open fun plus(left: ByteArray, right: ByteArray): ByteArray {
        return left + right
    }

    // _+_
    open fun plus(left: List<*>, right: List<*>): List<*> {
        return left + right
    }

    // _+_
    open fun plus(left: Timestamp, right: Duration): Timestamp {
        return left + right
    }

    // _+_
    open fun plus(left: Duration, right: Timestamp): Timestamp {
        return right + left
    }

    // _+_
    open fun plus(left: Duration, right: Duration): Duration {
        return left + right
    }

    // _-_
    open fun minus(left: Long, right: Long): Long {
        return left - right
    }

    // _-_
    open fun minus(left: ULong, right: ULong): ULong {
        return left - right
    }

    // _-_
    open fun minus(left: Double, right: Double): Double {
        return left - right
    }

    // _-_
    open fun minus(left: Timestamp, right: Duration): Timestamp {
        return left - right
    }

    // _-_
    open fun minus(left: Timestamp, right: Timestamp): Duration {
        return left - right
    }

    // _-_
    open fun minus(left: Duration, right: Duration): Duration {
        return left - right
    }

    // _/_
    open fun div(left: Long, right: Long): Long {
        return left / right
    }

    // _/_
    open fun div(left: ULong, right: ULong): ULong {
        return left / right
    }

    // _/_
    open fun div(left: Double, right: Double): Double {
        return left / right
    }

    // _<_ _<=_ _>_ _>=_
    open fun compare(left: Long, right: Long): Long {
        return left.compareTo(right).toLong()
    }

    // _<_ _<=_ _>_ _>=_
    open fun compare(left: ULong, right: ULong): Long {
        return left.compareTo(right).toLong()
    }

    // _<_ _<=_ _>_ _>=_
    open fun compare(left: Double, right: Double): Long {
        return left.compareTo(right).toLong()
    }

    // _<_ _<=_ _>_ _>=_
    open fun compare(left: String, right: String): Long {
        return left.compareTo(right).toLong()
    }

    // _<_ _<=_ _>_ _>=_
    open fun compare(left: ByteArray, right: ByteArray): Long {
        return Arrays.compare(left, right).toLong()
    }

    // _<_ _<=_ _>_ _>=_
    open fun compare(left: Timestamp, right: Timestamp): Long {
        return left.compareTo(right).toLong()
    }

    // _<_ _<=_ _>_ _>=_
    open fun compare(left: Duration, right: Duration): Long {
        return left.compareTo(right).toLong()
    }

    // _?_:_
    open fun conditional(condition: Boolean, value1: Any?, value2: Any?): Any? {
        return if (condition) value1 else value2
    }

    // _[_]
    open fun index(list: List<*>, index: Long): Any? {
        return list[index.toInt()]
    }

    // _[_]
    open fun index(map: Map<*, *>, index: Any?): Any? {
        return map[index]
    }

    // _._
    open fun access(map: Map<*, *>, index: Any?): Any? {
        return map[index]
    }

    // _._
    open fun access(message: Message<*, *>, index: String): Any? {
        return message.get<Any?>(index).protobufConversion()
    }

    private fun Any?.protobufConversion(): Any? {
        return when (this) {
            is Int -> toLong()
            is UInt -> toULong()
            is Float -> toDouble()
            is ListValue -> this.values.map { it.protobufConversion() }
            is DoubleValue -> this.value
            is FloatValue -> this.value.toDouble()
            is Int64Value -> this.value
            is UInt64Value -> this.value
            is Int32Value -> this.value.toLong()
            is UInt32Value -> this.value.toULong()
            is BoolValue -> this.value
            is StringValue -> this.value
            is BytesValue -> this.value
            is NullValue -> null
            is Struct -> this.fields.mapValues { it.value.protobufConversion() }
            is Value -> when (val kind = this.kind) {
                is Value.Kind.BoolValue -> kind.value
                is Value.Kind.ListValue -> kind.value.protobufConversion()
                is Value.Kind.NullValue -> null
                is Value.Kind.NumberValue -> kind.value
                is Value.Kind.StringValue -> kind.value
                is Value.Kind.StructValue -> kind.value
                null -> null
                else -> throw IllegalStateException("Illegal proto value type '${kind.javaClass}'.")
            }
            is ProtoEnum -> this.number.toLong()
            is List<*> -> this.map { it.protobufConversion() }
            is Map<*, *> -> this.mapValues { it.value.protobufConversion() }
            is CustomProtoType<*> -> this.value().protobufConversion()
            null -> null
            is Long, is ULong, is Double, is Boolean, is ByteArray, is String, is Message<*, *> -> this
            else -> throw IllegalStateException("Illegal proto data type '${this.javaClass}'.")
        }
    }

    // _in_
    open fun contains(map: Map<*, *>, key: Any?): Boolean {
        return key in map
    }

    // _in_
    open fun contains(map: List<*>, key: Any?): Boolean {
        return key in map
    }

    // string.contains(string) -> bool
    open fun String.contains(other: String): Boolean {
        return this.contains(other, false)
    }

    // _||_
    open fun logicalOr(left: Boolean, right: Boolean): Boolean {
        return left || right
    }

    // bytes(_)
    open fun bytes(value: String): ByteArray {
        return value.toByteArray()
    }

    open fun double(value: Long): Double {
        return value.toDouble()
    }

    open fun double(value: ULong): Double {
        return value.toDouble()
    }

    open fun double(value: String): Double {
        return value.toDouble()
    }

    open fun duration(value: String): Duration {
        return Duration(value)
    }

    open fun String.endsWith(value: String): Boolean {
        return this.endsWith(value, false)
    }

    open fun type(value: Any?): String {
        return when (value) {
            is Long -> "int"
            is ULong -> "uint"
            is Double -> "double"
            is Boolean -> "bool"
            is String -> "string"
            is ByteArray -> "bytes"
            is List<*> -> "list"
            is Map<*, *> -> "map"
            null -> "null_type"
            is Message<*, *> -> value.type()
            else -> "unknown"
        }
    }

    // open fun Timestamp.getDate(): String {
    //    return this.toInstant().atOffset(ZoneOffset.UTC).toLocalDate().toString()
    // }

    // open fun Timestamp.getDate(timezone: String): String {
    //    return this.toInstant().atOffset(ZoneOffset.of(timezone)).toLocalDate().toString()
    // }

    /**
     * Get day of month from the date in UTC, zero-based indexing
     */
    open fun Timestamp.getDayOfMonth(): Long {
        return getDayOfMonth(ZoneOffset.UTC.id)
    }

    /**
     * Get day of month from the date with timezone, zero-based indexing
     */
    open fun Timestamp.getDayOfMonth(timezone: String): Long {
        return this.toInstant().atOffset(ZoneOffset.of(timezone)).dayOfMonth.toLong() - 1
    }

    /**
     * Get day of week from the date in UTC, zero-based, zero for Sunday
     */
    open fun Timestamp.getDayOfWeek(): Long {
        return getDayOfWeek(ZoneOffset.UTC.id)
    }

    /**
     * Get day of week from the date with timezone, zero-based, zero for Sunday
     */
    open fun Timestamp.getDayOfWeek(timezone: String): Long {
        return when (this.toInstant().atOffset(ZoneOffset.of(timezone)).dayOfWeek) {
            DayOfWeek.MONDAY -> 1
            DayOfWeek.TUESDAY -> 2
            DayOfWeek.WEDNESDAY -> 3
            DayOfWeek.THURSDAY -> 4
            DayOfWeek.FRIDAY -> 5
            DayOfWeek.SATURDAY -> 6
            DayOfWeek.SUNDAY -> 0
        }
    }

    /**
     * Get day of year from the date in UTC, zero-based indexing
     */
    open fun Timestamp.getDayOfYear(): Long {
        return getDayOfYear(ZoneOffset.UTC.id)
    }

    /**
     * Get day of year from the date with timezone, zero-based indexing
     */
    open fun Timestamp.getDayOfYear(timezone: String): Long {
        return this.toInstant().atOffset(ZoneOffset.of(timezone)).dayOfYear.toLong() - 1
    }

    /**
     * Get year from the date in UTC
     */
    open fun Timestamp.getFullYear(): Long {
        return getFullYear(ZoneOffset.UTC.id)
    }

    /**
     * Get year from the date with timezone
     */
    open fun Timestamp.getFullYear(timezone: String): Long {
        return this.toInstant().atOffset(ZoneOffset.of(timezone)).year.toLong()
    }

    /**
     * Get hours from the date in UTC, 0-23
     */
    open fun Timestamp.getHours(): Long {
        return getHours(ZoneOffset.UTC.id)
    }

    /**
     * Get hours from the date with timezone, 0-23
     */
    open fun Timestamp.getHours(timezone: String): Long {
        return this.toInstant().atOffset(ZoneOffset.of(timezone)).hour.toLong()
    }

    /**
     * Get hours from duration
     */
    open fun Duration.getHours(): Long {
        return this.toTime(TimeUnit.HOURS)
    }

    /**
     * Get milliseconds from the date in UTC, 0-999
     */
    open fun Timestamp.getMilliseconds(): Long {
        return getMilliseconds(ZoneOffset.UTC.id)
    }

    /**
     * Get milliseconds from the date with timezone, 0-999
     */
    open fun Timestamp.getMilliseconds(timezone: String): Long {
        return TimeUnit.NANOSECONDS.toMillis(this.toInstant().atOffset(ZoneOffset.of(timezone)).nano.toLong())
    }

    /**
     * Get milliseconds from duration, 0-999
     */
    open fun Duration.getMilliseconds(): Long {
        return TimeUnit.NANOSECONDS.toMillis(this.nanos.toLong())
    }

    /**
     * Get minutes from the date in UTC, 0-59
     */
    open fun Timestamp.getMinutes(): Long {
        return getMinutes(ZoneOffset.UTC.id)
    }

    /**
     * Get minutes from the date with timezone, 0-59
     */
    open fun Timestamp.getMinutes(timezone: String): Long {
        return this.toInstant().atOffset(ZoneOffset.of(timezone)).minute.toLong()
    }

    /**
     * Get minutes from duration
     */
    open fun Duration.getMinutes(): Long {
        return this.toTime(TimeUnit.MINUTES)
    }

    /**
     * Get month from the date in UTC, 0-11
     */
    open fun Timestamp.getMonth(): Long {
        return getMonth(ZoneOffset.UTC.id)
    }

    /**
     * Get month from the date with timezone, 0-11
     */
    open fun Timestamp.getMonth(timezone: String): Long {
        return this.toInstant().atOffset(ZoneOffset.of(timezone)).monthValue.toLong() - 1
    }

    /**
     * Get seconds from the date in UTC, 0-59
     */
    open fun Timestamp.getSeconds(): Long {
        return getSeconds(ZoneOffset.UTC.id)
    }

    /**
     * Get seconds from the date with timezone, 0-59
     */
    open fun Timestamp.getSeconds(timezone: String): Long {
        return this.toInstant().atOffset(ZoneOffset.of(timezone)).second.toLong()
    }

    /**
     * Get seconds from duration
     */
    open fun Duration.getSeconds(): Long {
        return this.seconds
    }

    open fun int(value: ULong): Long {
        return value.toLong()
    }

    open fun int(value: Double): Long {
        return value.toLong()
    }

    open fun int(value: String): Long {
        return value.toLong()
    }

    open fun int(value: Timestamp): Long {
        return value.toSeconds()
    }

    open fun String.matches(regex: String): Boolean {
        return this.matches(regex.toRegex())
    }

    open fun size(value: String): Long {
        return value.length.toLong()
    }

    open fun size(value: ByteArray): Long {
        return value.size.toLong()
    }

    open fun size(value: List<*>): Long {
        return value.size.toLong()
    }

    open fun size(value: Map<*, *>): Long {
        return value.size.toLong()
    }

    open fun String.startsWith(value: String): Boolean {
        return this.startsWith(value, false)
    }

    open fun string(value: Long): String {
        return value.toString()
    }

    open fun string(value: ULong): String {
        return value.toString()
    }

    open fun string(value: Double): String {
        return value.toString()
    }

    open fun string(value: ByteArray): String {
        return value.base64()
    }

    open fun timestamp(value: String): Timestamp {
        return Timestamp(value)
    }

    open fun uint(value: Long): ULong {
        return value.toULong()
    }

    open fun uint(value: Double): ULong {
        return value.toULong()
    }

    open fun uint(value: String): ULong {
        return value.toULong()
    }
}
