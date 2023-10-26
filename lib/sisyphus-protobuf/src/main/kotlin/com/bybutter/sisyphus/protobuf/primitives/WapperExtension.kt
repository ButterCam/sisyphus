package com.bybutter.sisyphus.protobuf.primitives

fun String.wrapper() =
    StringValue {
        value = this@wrapper
    }

fun Boolean.wrapper() =
    BoolValue {
        value = this@wrapper
    }

fun Double.wrapper() =
    DoubleValue {
        value = this@wrapper
    }

fun Float.wrapper() =
    FloatValue {
        value = this@wrapper
    }

fun UInt.wrapper() =
    UInt32Value {
        value = this@wrapper
    }

fun ULong.wrapper() =
    UInt64Value {
        value = this@wrapper
    }

fun Int.wrapper() =
    Int32Value {
        value = this@wrapper
    }

fun Long.wrapper() =
    Int64Value {
        value = this@wrapper
    }

fun ByteArray.wrapper() =
    BytesValue {
        value = this@wrapper
    }
