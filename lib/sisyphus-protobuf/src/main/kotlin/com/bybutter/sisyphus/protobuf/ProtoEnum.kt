package com.bybutter.sisyphus.protobuf

interface ProtoEnum {
    val proto: String
    val number: Int
}

interface ProtoEnumDsl<T : ProtoEnum> {
    operator fun invoke(value: String): T

    fun fromProto(value: String): T?

    operator fun invoke(value: Int): T

    fun fromNumber(value: Int): T?

    operator fun invoke(): T
}
