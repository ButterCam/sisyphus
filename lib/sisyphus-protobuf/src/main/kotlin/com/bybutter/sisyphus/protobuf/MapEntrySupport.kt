package com.bybutter.sisyphus.protobuf

import com.google.protobuf.CodedInputStream
import com.google.protobuf.CodedOutputStream
import com.google.protobuf.WireFormat

abstract class MapEntrySupport<TKey, TValue, T : MapEntry<TKey, TValue, T, TM>, TM : MutableMapEntry<TKey, TValue, T, TM>>(fullName: String) : ProtoSupport<T, TM>(fullName) {
    fun sizeOf(number: Int, value: Map<TKey, TValue>): Int {
        return value.entries.sumBy {
            val size = sizeOfPair(it)
            CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeInt32SizeNoTag(size) + size
        }
    }

    fun writeMap(output: CodedOutputStream, number: Int, value: Map<TKey, TValue>) {
        for (entry in value.entries) {
            output.writeTag(number, WireFormat.WIRETYPE_LENGTH_DELIMITED)
            output.writeInt32NoTag(sizeOfPair(entry))
            writePair(output, entry)
        }
    }

    abstract fun sizeOfPair(entry: Map.Entry<TKey, TValue>): Int

    abstract fun writePair(output: CodedOutputStream, entry: Map.Entry<TKey, TValue>)

    abstract fun readPair(input: CodedInputStream, size: Int): Pair<TKey, TValue>
}
