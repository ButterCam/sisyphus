package com.bybutter.sisyphus.protobuf

interface ProtoEnum {
    val proto: String
    val number: Int

    companion object {
        fun <T> fromProto(value: String?, type: Class<T>): T? where T : ProtoEnum {
            return type.enumConstants.firstOrNull { it.proto == value }
        }

        inline fun <reified T> fromProto(value: String?): T? where T : ProtoEnum {
            return fromProto(value, T::class.java)
        }

        operator fun <T> invoke(value: String?, type: Class<T>): T where T : ProtoEnum {
            return fromProto(value, type)
                    ?: invoke(type)
        }

        inline operator fun <reified T> invoke(value: String?): T where T : ProtoEnum {
            return invoke(value, T::class.java)
        }

        fun <T> fromNumber(value: Int?, type: Class<T>): T? where T : ProtoEnum {
            return type.enumConstants.firstOrNull { it.number == value }
        }

        inline fun <reified T> fromNumber(value: Int?): T? where T : ProtoEnum {
            return fromNumber(value, T::class.java)
        }

        operator fun <T> invoke(value: Int?, type: Class<T>): T where T : ProtoEnum {
            return fromNumber(value, type)
                    ?: invoke(type)
        }

        inline operator fun <reified T> invoke(value: Int?): T where T : ProtoEnum {
            return invoke(value, T::class.java)
        }

        operator fun <T> invoke(type: Class<T>): T where T : ProtoEnum {
            return type.enumConstants.first()
        }

        inline operator fun <reified T> invoke(): T where T : ProtoEnum {
            return invoke(T::class.java)
        }
    }
}

interface ProtoEnumDsl<T : ProtoEnum> {
    operator fun invoke(value: String): T

    fun fromProto(value: String): T?

    operator fun invoke(value: Int): T

    fun fromNumber(value: Int): T?

    operator fun invoke(): T

    companion object {
        operator fun <T : ProtoEnum> invoke(clazz: Class<T>): ProtoEnumDsl<T> {
            return Impl(clazz)
        }
    }

    private class Impl<T : ProtoEnum>(val clazz: Class<T>) : ProtoEnumDsl<T> {
        override fun invoke(value: String): T {
            return ProtoEnum(value, clazz)
        }

        override fun fromProto(value: String): T? {
            return ProtoEnum.fromProto(value, clazz)
        }

        override fun invoke(value: Int): T {
            return ProtoEnum(value, clazz)
        }

        override fun fromNumber(value: Int): T? {
            return ProtoEnum.fromNumber(value, clazz)
        }

        override fun invoke(): T {
            return ProtoEnum(clazz)
        }
    }
}

interface ProtoStringEnum : ProtoEnum {
    val value: String

    companion object {
        fun <T> fromValue(value: String?, type: Class<T>): T? where T : ProtoStringEnum {
            return type.enumConstants.firstOrNull { it.value == value }
        }

        inline fun <reified T> fromValue(value: String?): T? where T : ProtoStringEnum {
            return fromValue(value, T::class.java)
        }
    }
}

interface ProtoStringEnumDsl<T : ProtoStringEnum> : ProtoEnumDsl<T> {
    fun fromValue(value: String): T?

    companion object {
        operator fun <T : ProtoStringEnum> invoke(clazz: Class<T>): ProtoStringEnumDsl<T> {
            return Impl(clazz)
        }
    }

    private class Impl<T : ProtoStringEnum>(val clazz: Class<T>) : ProtoStringEnumDsl<T>, ProtoEnumDsl<T> by ProtoEnumDsl(clazz) {
        override fun fromValue(value: String): T? {
            return ProtoStringEnum.fromValue(value, clazz)
        }
    }
}
