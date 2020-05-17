package com.bybutter.sisyphus.protobuf

interface MapEntry<TKey, TValue, T : MapEntry<TKey, TValue, T, TM>, TM : MutableMapEntry<TKey, TValue, T, TM>> :
        Message<T, TM> {
    val key: TKey
    val value: TValue
}

interface MutableMapEntry<TKey, TValue, T : MapEntry<TKey, TValue, T, TM>, TM : MutableMapEntry<TKey, TValue, T, TM>> :
        MapEntry<TKey, TValue, T, TM>, MutableMessage<T, TM> {
    override var key: TKey
    override var value: TValue
}
