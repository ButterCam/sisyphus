package com.bybutter.sisyphus.middleware.hbase.converter

import com.bybutter.sisyphus.data.toByte
import com.bybutter.sisyphus.data.toByteData
import com.bybutter.sisyphus.middleware.hbase.ValueConverter
import com.bybutter.sisyphus.middleware.hbase.annotation.DefaultConverter

@DefaultConverter(Byte::class)
class ByteConverter : ValueConverter<Byte> {
    override fun convert(value: Byte): ByteArray {
        return value.toByteData()
    }

    override fun convertBack(value: ByteArray): Byte {
        return value.toByte()
    }
}
