package com.bybutter.sisyphus.middleware.hbase

import com.bybutter.sisyphus.data.hashWrapper
import com.bybutter.sisyphus.dto.DtoModel
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.client.Connection
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.client.Put

class HTemplateFactory<TKey, TValue> constructor(
    private val keyType: JavaType,
    private val valueType: JavaType
) where TKey : Any, TValue : DtoModel {
    private var connection: Connection? = null
    private var valueConverter: ValueConverter<TKey>? = null
    private var tableModelConverter: TableModelConverter<TValue>? = null
    private var table: ByteArray? = null

    fun using(connection: Connection): HTemplateFactory<TKey, TValue> {
        this.connection = connection
        return this
    }

    fun from(table: String): HTemplateFactory<TKey, TValue> {
        this.table = table.toByteArray()
        return this
    }

    fun from(table: ByteArray): HTemplateFactory<TKey, TValue> {
        this.table = table
        return this
    }

    fun rowKeyConverter(converter: ValueConverter<TKey>): HTemplateFactory<TKey, TValue> {
        this.valueConverter = converter
        return this
    }

    fun tableModelConverter(converter: TableModelConverter<TValue>): HTemplateFactory<TKey, TValue> {
        this.tableModelConverter = converter
        return this
    }

    fun template(): HTemplate<TKey, TValue> {
        val connection = connection ?: throw IllegalArgumentException("Not set connection for HTemplate.")
        val table = table ?: throw IllegalArgumentException("Not set table for HTemplate.")

        val keyConverter = valueConverter ?: getDefaultValueConverter<TKey>(keyType)
        ?: throw IllegalArgumentException("No convert for type '${keyType.typeName}'.")
        val valueConverter = tableModelConverter ?: getDefaultTableModelConverter<TValue>(valueType)
        ?: throw IllegalArgumentException("No convert for type '${valueType.typeName}'.")

        return DefaultTemplate(connection, table, keyConverter, valueConverter)
    }

    companion object {
        inline operator fun <reified TKey, reified TValue> invoke(): HTemplateFactory<TKey, TValue> where TKey : Any, TValue : DtoModel {
            return HTemplateFactory(TypeFactory.defaultInstance().constructType(object : TypeReference<TKey>() {}),
                    TypeFactory.defaultInstance().constructType(object : TypeReference<TValue>() {})
            )
        }
    }

    private class DefaultTemplate<TKey, TValue>(
        override val connection: Connection,
        override val table: ByteArray,
        val rowKeyConverter: ValueConverter<TKey>,
        val tableModelConverter: TableModelConverter<TValue>
    ) : HTemplate<TKey, TValue> where TKey : Any, TValue : Any {

        override fun get(key: TKey): TValue? {
            connection.getTable(TableName.valueOf(table)).use {
                val get = Get(rowKeyConverter.convert(key))
                return it.get(get)?.let {
                    if (it.row != null) {
                        tableModelConverter.convert(it)
                    } else {
                        null
                    }
                }
            }
        }

        override fun set(key: TKey, value: TValue) {
            connection.getTable(TableName.valueOf(table)).use {
                val put = Put(rowKeyConverter.convert(key))
                val values = tableModelConverter.convertBack(value)
                values.forEach { (key, value) ->
                    value.forEach { (q, v) ->
                        put.addColumn(key, q, v)
                    }
                }
                return it.put(put)
            }
        }

        override fun getMap(keys: Collection<TKey>): Map<TKey, TValue> {
            connection.getTable(TableName.valueOf(table)).use {
                val getRequests = keys.map { Get(rowKeyConverter.convert(it)) }
                return it.get(getRequests).filter { it.row != null }.associate {
                    val keyMap = keys.associate { rowKeyConverter.convert(it)?.hashWrapper() to it }
                    (keyMap[it.row.hashWrapper()]
                            ?: throw RuntimeException("Key value not found.")) to tableModelConverter.convert(it)
                }
            }
        }

        override fun setMap(values: Map<TKey, TValue>) {
            connection.getTable(TableName.valueOf(table)).use {
                val putRequests = values.map {
                    val put = Put(rowKeyConverter.convert(it.key))
                    tableModelConverter.convertBack(it.value).forEach { (key, value) ->
                        value.forEach { (q, v) ->
                            put.addColumn(key, q, v)
                        }
                    }
                    put
                }
                return it.put(putRequests)
            }
        }

        override fun getMap(vararg keys: TKey): Map<TKey, TValue> {
            return getMap(keys.toList())
        }

        override fun setMap(vararg values: Pair<TKey, TValue>) {
            return setMap(values.toMap())
        }
    }
}
