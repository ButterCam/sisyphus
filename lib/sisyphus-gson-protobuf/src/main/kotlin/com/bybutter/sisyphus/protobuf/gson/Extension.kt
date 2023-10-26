package com.bybutter.sisyphus.protobuf.gson

import com.bybutter.sisyphus.protobuf.primitives.Struct
import com.google.gson.Gson
import com.google.gson.GsonBuilder

fun GsonBuilder.registerProtobufAdapterFactory(): GsonBuilder {
    return registerTypeAdapterFactory(MessageTypeAdapterFactory)
}

inline fun <reified T> Gson.fromJson(struct: Struct): T {
    return fromJson(struct, T::class.java)
}

fun <T> Gson.fromJson(
    struct: Struct,
    clazz: Class<T>,
): T {
    return fromJson(this.toJsonTree(struct), clazz)
}
