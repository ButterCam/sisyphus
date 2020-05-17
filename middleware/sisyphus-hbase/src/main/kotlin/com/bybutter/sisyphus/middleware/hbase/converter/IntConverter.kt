package com.bybutter.sisyphus.middleware.hbase.converter

import com.bybutter.sisyphus.data.toByteData
import com.bybutter.sisyphus.data.toInt
import com.bybutter.sisyphus.middleware.hbase.ValueConverter
import com.bybutter.sisyphus.middleware.hbase.annotation.DefaultConverter

@DefaultConverter(Int::class)
class IntConverter : ValueConverter<Int> {
    override fun convert(value: Int): ByteArray {
        return value.toByteData()
    }

    override fun convertBack(value: ByteArray): Int {
        return value.toInt()
    }
}
