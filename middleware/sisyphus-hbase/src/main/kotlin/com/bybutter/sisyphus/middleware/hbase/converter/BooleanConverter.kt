package com.bybutter.sisyphus.middleware.hbase.converter

import com.bybutter.sisyphus.data.toBoolean
import com.bybutter.sisyphus.data.toByteData
import com.bybutter.sisyphus.middleware.hbase.ValueConverter

class BooleanConverter : ValueConverter<Boolean> {
    override fun convert(value: Boolean): ByteArray {
        return value.toByteData()
    }

    override fun convertBack(value: ByteArray): Boolean {
        return value.toBoolean()
    }
}
