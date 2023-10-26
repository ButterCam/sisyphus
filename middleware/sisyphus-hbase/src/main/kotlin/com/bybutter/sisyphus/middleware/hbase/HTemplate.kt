package com.bybutter.sisyphus.middleware.hbase

import org.apache.hadoop.hbase.client.Connection

interface HTemplate<TKey, TValue> {
    val connection: Connection
    val table: ByteArray

    fun get(key: TKey): TValue?

    fun set(
        key: TKey,
        value: TValue,
    )

    fun getMap(keys: Collection<TKey>): Map<TKey, TValue>

    fun setMap(values: Map<TKey, TValue>)

    fun getMap(vararg keys: TKey): Map<TKey, TValue>

    fun setMap(vararg values: Pair<TKey, TValue>)

    fun delete(vararg keys: TKey)
}
