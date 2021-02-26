package com.bybutter.sisyphus.math

import com.bybutter.sisyphus.Quat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

typealias Vec2 = Pair<Double, Double>

val Vec2.x get() = first
val Vec2.y get() = second

operator fun Vec2.plus(other: Vec2): Vec2 {
    return (x + other.x) to (y + other.y)
}

operator fun Vec2.minus(other: Vec2): Vec2 {
    return (x - other.x) to (y - other.y)
}

operator fun Vec2.times(other: Vec2): Vec2 {
    return (x * other.x) to (y * other.y)
}

operator fun Vec2.div(other: Vec2): Vec2 {
    return (x / other.x) to (y / other.y)
}

operator fun Vec2.plus(other: Double): Vec2 {
    return (x + other) to (y + other)
}

operator fun Vec2.minus(other: Double): Vec2 {
    return (x - other) to (y - other)
}

operator fun Vec2.times(other: Double): Vec2 {
    return (x * other) to (y * other)
}

operator fun Vec2.div(other: Double): Vec2 {
    return (x / other) to (y / other)
}

fun Vec2.length(): Double {
    return sqrt(x * x + y * y)
}

fun Vec2.angle(): Double {
    return atan2(y, x)
}

fun Vec2.dot(other: Vec2): Double {
    return (x * other.x) + (y * other.y)
}

fun Vec2.toPolar(): Vec2 {
    return length() to angle()
}

fun Vec2.toCartesian(): Vec2 {
    return (cos(y) * x) to (sin(y) * x)
}

typealias Vec3 = Triple<Double, Double, Double>

val Vec3.x get() = first
val Vec3.y get() = second
val Vec3.z get() = third
val Vec3.r get() = first
val Vec3.g get() = second
val Vec3.b get() = third
val Vec3.xy: Vec2 get() = x to y
val Vec3.xz: Vec2 get() = x to z
val Vec3.yz: Vec2 get() = y to z

operator fun Vec3.plus(other: Vec3): Vec3 {
    return Vec3(x + other.x, y + other.y, z + other.z)
}

operator fun Vec3.minus(other: Vec3): Vec3 {
    return Vec3(x - other.x, y - other.y, z - other.z)
}

operator fun Vec3.times(other: Vec3): Vec3 {
    return Vec3(x * other.x, y * other.y, z * other.z)
}

operator fun Vec3.div(other: Vec3): Vec3 {
    return Vec3(x / other.x, y / other.y, z / other.z)
}

operator fun Vec3.plus(other: Double): Vec3 {
    return Vec3(x + other, y + other, z + other)
}

operator fun Vec3.minus(other: Double): Vec3 {
    return Vec3(x - other, y - other, z - other)
}

operator fun Vec3.times(other: Double): Vec3 {
    return Vec3(x * other, y * other, z * other)
}

operator fun Vec3.div(other: Double): Vec3 {
    return Vec3(x / other, y / other, z / other)
}

fun Vec3.dot(other: Vec3): Double {
    return (x * other.x) + (y * other.y) + (z * other.z)
}

typealias Vec4 = Quat<Double, Double, Double, Double>

val Vec4.x get() = first
val Vec4.y get() = second
val Vec4.z get() = third
val Vec4.w get() = fourth
val Vec4.r get() = first
val Vec4.g get() = second
val Vec4.b get() = third
val Vec4.a get() = fourth
val Vec4.xy: Vec2 get() = x to y
val Vec4.xyz: Vec3 get() = Vec3(x, y, z)
val Vec4.rgb: Vec3 get() = Vec3(x, y, z)

operator fun Vec4.plus(other: Vec4): Vec4 {
    return Vec4(x + other.x, y + other.y, z + other.z, w + other.w)
}

operator fun Vec4.minus(other: Vec4): Vec4 {
    return Vec4(x - other.x, y - other.y, z - other.z, w - other.w)
}

operator fun Vec4.times(other: Vec4): Vec4 {
    return Vec4(x * other.x, y * other.y, z * other.z, w * other.w)
}

operator fun Vec4.div(other: Vec4): Vec4 {
    return Vec4(x / other.x, y / other.y, z / other.z, w / other.w)
}

operator fun Vec4.plus(other: Double): Vec4 {
    return Vec4(x + other, y + other, z + other, w + other)
}

operator fun Vec4.minus(other: Double): Vec4 {
    return Vec4(x - other, y - other, z - other, w - other)
}

operator fun Vec4.times(other: Double): Vec4 {
    return Vec4(x * other, y * other, z * other, w * other)
}

operator fun Vec4.div(other: Double): Vec4 {
    return Vec4(x / other, y / other, z / other, w / other)
}

fun Vec4.dot(other: Vec4): Double {
    return (x * other.x) + (y * other.y) + (z * other.z) + (w * other.w)
}
