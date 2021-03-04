package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.protobuf.primitives.EnumDescriptorProto
import kotlin.reflect.KClass

abstract class EnumSupport<T : ProtoEnum>(val enumClass: KClass<T>) : ProtoEnumDsl<T>,
    ProtoSupport<EnumDescriptorProto> {
    private val numberMap = enumClass.java.enumConstants.associate {
        (it as ProtoEnum).number to (it as T)
    }

    private val nameMap = enumClass.java.enumConstants.associate {
        (it as ProtoEnum).proto to (it as T)
    }

    override fun fromNumber(value: Int): T? {
        return numberMap[value]
    }

    override fun fromProto(value: String): T? {
        return nameMap[value]
    }

    override fun invoke(): T {
        return enumClass.java.enumConstants.first() as T
    }

    override fun invoke(value: Int): T {
        return fromNumber(value) ?: invoke()
    }

    override fun invoke(value: String): T {
        return fromProto(value) ?: invoke()
    }
}
