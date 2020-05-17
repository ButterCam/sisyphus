package com.bybutter.sisyphus.protobuf

interface OneOfValue<T> {
    val name: String
    val number: Int
    val value: T
}
