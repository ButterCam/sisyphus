package com.bybutter.sisyphus.middleware.hbase.converter

import com.bybutter.sisyphus.middleware.hbase.ValueConverter
import com.bybutter.sisyphus.middleware.hbase.annotation.DefaultConverter

@DefaultConverter(String::class)
class StringConverter : ValueConverter<String> {
    override fun convert(value: String): ByteArray {
        return value.toByteArray()
    }

    override fun convertBack(value: ByteArray): String {
        return value.toString(Charsets.UTF_8)
    }
}
