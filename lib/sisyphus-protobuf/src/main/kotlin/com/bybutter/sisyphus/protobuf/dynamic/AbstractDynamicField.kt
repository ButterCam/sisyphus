package com.bybutter.sisyphus.protobuf.dynamic

import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto

abstract class AbstractDynamicField<T>(
    private val descriptor: FieldDescriptorProto
) : DynamicField<T> {
    protected abstract var value: T
    protected var hasValue = false

    protected abstract fun defaultValue(): T

    override fun descriptor(): FieldDescriptorProto {
        return descriptor
    }

    override fun get(): T {
        if (has()) return value
        return defaultValue()
    }

    override fun set(value: T) {
        this.value = value
        this.hasValue = true
    }

    override fun has(): Boolean {
        return hasValue
    }

    override fun clear(): T? {
        if (has()) {
            return value.also {
                this.value = defaultValue()
                this.hasValue = false
            }
        }
        return null
    }

    override fun hashCode(): Int {
        var result = this.javaClass.hashCode()
        if (has()) {
            result = result * 37 + descriptor().number
            result = result * 31 + this.`value`.hashCode()
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (get() == other) {
            return true
        }
        if (other is AbstractDynamicField<*> && javaClass == other.javaClass && get() == other.get() && descriptor().number == other.descriptor().number) {
            return true
        }
        return false
    }
}
