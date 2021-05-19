package com.bybutter.sisyphus.protobuf.dynamic

import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto

abstract class AbstractRepeatedDynamicField<T>(private val descriptor: FieldDescriptorProto) :
    DynamicField<MutableList<T>> {
    protected val value: MutableList<T> = mutableListOf()

    override fun descriptor(): FieldDescriptorProto {
        return descriptor
    }

    override fun get(): MutableList<T> {
        return value
    }

    override fun set(value: MutableList<T>) {
        this.value.clear()
        this.value += value
    }

    override fun has(): Boolean {
        return value.isNotEmpty()
    }

    override fun clear(): MutableList<T>? {
        if (has()) {
            return value.toMutableList().also {
                value.clear()
            }
        }
        return null
    }

    override fun hashCode(): Int {
        var result = this.javaClass.hashCode()
        for (value in get()) {
            result = result * 37 + descriptor().number
            result = result * 31 + value.hashCode()
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is List<*> -> get().contentEquals(other)
            is AbstractRepeatedDynamicField<*> -> {
                if(this.javaClass != other.javaClass) return false
                get().contentEquals(other.get()) && descriptor().number == other.descriptor().number
            }
            else -> false
        }
    }
}