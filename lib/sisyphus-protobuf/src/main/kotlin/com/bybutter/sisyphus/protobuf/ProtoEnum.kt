package com.bybutter.sisyphus.protobuf

interface ProtoEnum<T : ProtoEnum<T>> {
    val proto: String
    val number: Int

    fun support(): EnumSupport<T>
}
