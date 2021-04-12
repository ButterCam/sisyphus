package com.bybutter.sisyphus.protobuf.gson

import com.google.gson.GsonBuilder

fun GsonBuilder.registerProtobufAdapterFactory(): GsonBuilder {
    return registerTypeAdapterFactory(MessageTypeAdapterFactory)
}
