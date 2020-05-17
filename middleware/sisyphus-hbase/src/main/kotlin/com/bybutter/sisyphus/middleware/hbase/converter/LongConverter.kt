package com.bybutter.sisyphus.middleware.hbase.converter

import com.bybutter.sisyphus.data.toByteData
import com.bybutter.sisyphus.data.toLong
import com.bybutter.sisyphus.middleware.hbase.ValueConverter
import com.bybutter.sisyphus.middleware.hbase.annotation.DefaultConverter

@DefaultConverter(Long::class)
class LongConverter : ValueConverter<Long> {
    override fun convert(value: Long): ByteArray {
        return value.toByteData()
    }

    override fun convertBack(value: ByteArray): Long {
        return value.toLong()
    }
}
