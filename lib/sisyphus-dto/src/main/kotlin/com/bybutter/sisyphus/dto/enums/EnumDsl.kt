package com.bybutter.sisyphus.dto.enums

interface IntEnumDsl<T : IntEnum> {
    operator fun invoke(value: Int): T

    fun valueOf(value: Int): T?

    companion object {
        operator fun <T : IntEnum> invoke(clazz: Class<T>): IntEnumDsl<T> {
            return Impl(clazz)
        }
    }

    private class Impl<T : IntEnum>(val clazz: Class<T>) : IntEnumDsl<T> {
        override fun invoke(value: Int): T {
            return IntEnum(value, clazz)
        }

        override fun valueOf(value: Int): T? {
            return IntEnum.valueOf(value, clazz)
        }
    }
}

interface StringEnumDsl<T : StringEnum> {
    operator fun invoke(value: String): T

    fun valueOf(value: String): T?

    companion object {
        operator fun <T : StringEnum> invoke(clazz: Class<T>): StringEnumDsl<T> {
            return Impl(clazz)
        }
    }

    private class Impl<T : StringEnum>(val clazz: Class<T>) : StringEnumDsl<T> {
        override fun invoke(value: String): T {
            return StringEnum(value, clazz)
        }

        override fun valueOf(value: String): T? {
            return StringEnum.valueOf(value, clazz)
        }
    }
}
