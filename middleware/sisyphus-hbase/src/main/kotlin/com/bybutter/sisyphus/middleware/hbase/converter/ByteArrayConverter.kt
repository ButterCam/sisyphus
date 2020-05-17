package com.bybutter.sisyphus.middleware.hbase.converter

import com.bybutter.sisyphus.middleware.hbase.ValueConverter
import com.bybutter.sisyphus.middleware.hbase.annotation.DefaultConverter

@DefaultConverter(ByteArray::class)
class ByteArrayConverter : ValueConverter<ByteArray> {
    override fun convert(value: ByteArray): ByteArray {
        return value
    }

    override fun convertBack(value: ByteArray): ByteArray {
        return value
    }
}
