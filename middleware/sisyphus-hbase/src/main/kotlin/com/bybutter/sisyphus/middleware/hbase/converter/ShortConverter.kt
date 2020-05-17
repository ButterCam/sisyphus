package com.bybutter.sisyphus.middleware.hbase.converter

import com.bybutter.sisyphus.data.toByteData
import com.bybutter.sisyphus.data.toShort
import com.bybutter.sisyphus.middleware.hbase.ValueConverter
import com.bybutter.sisyphus.middleware.hbase.annotation.DefaultConverter

@DefaultConverter(Short::class)
class ShortConverter : ValueConverter<Short> {
    override fun convert(value: Short): ByteArray {
        return value.toByteData()
    }

    override fun convertBack(value: ByteArray): Short {
        return value.toShort()
    }
}
