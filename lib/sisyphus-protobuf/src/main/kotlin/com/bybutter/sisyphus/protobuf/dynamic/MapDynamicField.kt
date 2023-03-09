package com.bybutter.sisyphus.protobuf.dynamic

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.ProtoReflection
import com.bybutter.sisyphus.protobuf.coded.Reader
import com.bybutter.sisyphus.protobuf.coded.WireType
import com.bybutter.sisyphus.protobuf.coded.Writer
import com.bybutter.sisyphus.protobuf.findMapEntryDescriptor
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto

class MapDynamicField<TKey, TValue>(
    private val descriptor: FieldDescriptorProto
) : DynamicField<MutableMap<TKey, TValue>> {
    private val map = mutableMapOf<TKey, TValue>()

    private val mapEntryDescriptor by lazy {
        ProtoReflection.findMapEntryDescriptor(descriptor().typeName)!!
    }

    private val keyField = mapEntryDescriptor.field.first { it.number == 1 }

    private val valueField = mapEntryDescriptor.field.first { it.number == 2 }

    override fun descriptor(): FieldDescriptorProto {
        return descriptor
    }

    override fun get(): MutableMap<TKey, TValue> {
        return map
    }

    override fun set(value: MutableMap<TKey, TValue>) {
        this.map.clear()
        this.map += value
    }

    override fun has(): Boolean {
        return map.isNotEmpty()
    }

    override fun clear(): MutableMap<TKey, TValue>? {
        if (!has()) return null
        return map.toMutableMap().apply {
            map.clear()
        }
    }

    override fun read(reader: Reader, field: Int, wire: Int) {
        reader.mapEntry(
            {
                it.read<Any?>(keyField.type, keyField.typeName)
            },
            {
                it.read<Any?>(valueField.type, valueField.typeName)
            }
        ) { k, v ->
            get()[k as TKey] = v as TValue
        }
    }

    override fun writeTo(writer: Writer) {
        map.forEach { (k, v) ->
            writer.tag(descriptor().number, WireType.LENGTH_DELIMITED)
                .beginLd()
                .tag(keyField.number, WireType.ofType(keyField.type))
                .write(keyField.type, k)
                .tag(valueField.number, WireType.ofType(valueField.type))
                .write(valueField.type, v)
                .endLd()
        }
    }

    override fun hashCode(): Int {
        var result = descriptor.typeName.hashCode()
        for ((key, value) in map) {
            result = result * 37 + descriptor.number
            result = result * 31 + key.hashCode()
            result = result * 31 + value.hashCode()
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Map<*, *> -> get().contentEquals(other as Map<TKey, TValue>)
            is MapDynamicField<*, *> -> {
                if (this.javaClass != other.javaClass) return false
                get().contentEquals(other.get() as Map<TKey, TValue>) && descriptor().number == other.descriptor().number
            }

            else -> false
        }
    }
}
