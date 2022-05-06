package com.bybutter.sisyphus.middleware.hbase

import com.bybutter.sisyphus.dto.DtoModel
import com.bybutter.sisyphus.middleware.hbase.annotation.HTable
import com.bybutter.sisyphus.middleware.hbase.converter.DefaultTableModelConverter
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import org.apache.hadoop.hbase.client.Result

interface TableModelConverter<T> {
    fun convert(value: Result): T

    fun convertBack(value: T): Map<ByteArray, Map<ByteArray, ByteArray?>>
}

internal inline fun <reified T> getDefaultTableModelConverter(): TableModelConverter<T>? where T : DtoModel {
    return getDefaultTableModelConverter(TypeFactory.defaultInstance().constructType(object : TypeReference<T>() {}))
}

internal fun <T> getDefaultTableModelConverter(type: JavaType): TableModelConverter<T>? where T : DtoModel {
    val table = type.rawClass.getAnnotation(HTable::class.java)
        ?: throw UnsupportedOperationException("Unsupported default table model convert for type'${type.typeName}'.")
    return DefaultTableModelConverter(type)
}
