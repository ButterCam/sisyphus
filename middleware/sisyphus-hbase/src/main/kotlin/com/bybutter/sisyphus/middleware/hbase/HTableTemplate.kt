package com.bybutter.sisyphus.middleware.hbase

import com.bybutter.sisyphus.data.hashWrapper
import com.bybutter.sisyphus.dto.DtoModel
import com.bybutter.sisyphus.jackson.javaType
import com.bybutter.sisyphus.middleware.hbase.annotation.HTable
import com.bybutter.sisyphus.reflect.uncheckedCast
import java.lang.reflect.ParameterizedType
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.client.Connection
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.client.Scan

abstract class HTableTemplate<TKey : Any, TValue : Any> : HTemplate<TKey, TValue> {
    override lateinit var connection: Connection

    override val table: ByteArray
        get() {
            val valueClass = (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as? Class<*>
            val annotation = this.javaClass.getAnnotation(HTable::class.java)
                    ?: valueClass?.getAnnotation(HTable::class.java)
                    ?: throw RuntimeException("HTableTemplate only support class with annotation 'HTable'.")
            return annotation.byteValue.let { if (it.isEmpty()) null else it }
                    ?: annotation.value.toByteArray().let { if (it.isEmpty()) null else it }
                    ?: throw RuntimeException("No table name for class '${valueClass?.name}'.")
        }
    open val rowKeyConverter: ValueConverter<TKey>
        get() {
            val keyClass = (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as? Class<*>
                    ?: throw RuntimeException("Unknown table key type.")
            return getDefaultValueConverter<TKey>(keyClass.javaType) as ValueConverter<TKey>
        }
    open val tableModelConverter: TableModelConverter<TValue>
        get() {
            val valueClass = (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as? Class<*>
                    ?: throw RuntimeException("Unknown table value type.")
            return getDefaultTableModelConverter<DtoModel>(valueClass.javaType).uncheckedCast()
        }

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

    fun initializeConnection() {
        connection.getTable(TableName.valueOf(table)).use {
            it.getScanner(Scan()).next()
        }
    }

    override fun getMap(vararg keys: TKey): Map<TKey, TValue> {
        return getMap(keys.toList())
    }

    override fun setMap(vararg values: Pair<TKey, TValue>) {
        return setMap(values.toMap())
    }
}
