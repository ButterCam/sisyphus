package com.bybutter.sisyphus.dto

import com.bybutter.sisyphus.reflect.uncheckedCast
import kotlin.math.roundToInt
import kotlin.reflect.KProperty
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

interface TestDto<T> : DtoModel {
    var value: T
}

interface StringTest : TestDto<String> {
    override var value: String
}

interface Test2Dto<T> : TestDto<T>

class DtoTest {
    interface NormalDto : DtoModel {
        var stringValue: String
        var numberValue: Float
    }

    @Test
    fun `normal dto`() {
        DtoModel<NormalDto> {
            stringValue = "test"
            numberValue = 123.456f
        }
    }

    @Test
    fun `null value`() {
        assertCauseThrows<NullPointerException> {
            DtoModel<NormalDto> {
                stringValue = "test"
            }
        }
    }

    interface NormalDtoWithNullableProperty : DtoModel {
        var stringValue: String
        @get:NullableProperty
        var numberValue: Float
    }

    @Test
    fun `nullable property`() {
        DtoModel<NormalDtoWithNullableProperty> {
            stringValue = "test"
        }
    }

    @Test
    fun `dto cast`() {
        val dto = DtoModel<NormalDtoWithNullableProperty> {
            stringValue = "test"
        }

        assertCauseThrows<NullPointerException> {
            dto.cast<NormalDto>()
        }

        val newDto = dto.cast<NormalDto> {
            numberValue = 123.456f
        }

        Assertions.assertEquals(newDto.numberValue, dto.numberValue)
        Assertions.assertEquals(newDto.uncheckedCast<DtoMeta>().`$modelMap`, dto.uncheckedCast<DtoMeta>().`$modelMap`)
    }

    interface NormalDtoWithPropertyValidation : DtoModel {
        class MustBiggerThan : PropertyValidator<Float> {
            override fun verify(
                proxy: ModelProxy,
                value: Float,
                params: Array<out String>,
                property: KProperty<Float?>
            ): Exception? {
                return if (value <= params[0].toFloat()) {
                    IllegalArgumentException("$property must be bigger than '${params[0]}'")
                } else {
                    null
                }
            }
        }

        @set:PropertyValidation(MustBiggerThan::class, ["2.0"])
        var numberValue: Float

        @set:PropertyValidation(MustBiggerThan::class, ["10.0"])
        var numberValue2: Float
    }

    @Test
    fun `property validation`() {
        DtoModel<NormalDtoWithPropertyValidation> {
            numberValue = 5.0f
            numberValue2 = 15.0f
        }

        assertCauseThrows<IllegalArgumentException> {
            DtoModel<NormalDtoWithPropertyValidation> {
                numberValue = 0.0f
                numberValue2 = 15.0f
            }
        }

        assertCauseThrows<IllegalArgumentException> {
            DtoModel<NormalDtoWithPropertyValidation> {
                numberValue = 5.0f
                numberValue2 = 0.0f
            }
        }
    }

    @DtoValidation(NormalDtoWithDtoValidation.MaxMustBiggerThanMin::class)
    interface NormalDtoWithDtoValidation : DtoModel {
        class MaxMustBiggerThanMin : DtoValidator<NormalDtoWithDtoValidation> {
            override fun verify(value: NormalDtoWithDtoValidation, params: Array<out String>): Exception? {
                if (value.min >= value.max) {
                    return IllegalArgumentException("max must be bigger than min.")
                }

                return null
            }
        }

        var min: Float

        var max: Float
    }

    @Test
    fun `dto validation`() {
        DtoModel<NormalDtoWithDtoValidation> {
            min = 0.0f
            max = 1.0f
        }

        assertCauseThrows<IllegalArgumentException>("max must be bigger than min.") {
            DtoModel<NormalDtoWithDtoValidation> {
                min = 0.0f
                max = -1.0f
            }
        }
    }

    interface NormalDtoWithHook : DtoModel {
        class RoundingProperty : PropertyHookHandler<String> {
            override fun invoke(
                target: Any,
                value: String,
                params: Array<out String>,
                property: KProperty<String?>
            ): String {
                return value.toFloat().roundToInt().toString()
            }
        }

        @get:PropertyHook(RoundingProperty::class)
        var numberWithRounding: String

        @set:PropertyHook(RoundingProperty::class)
        var numberWithRounding2: String
    }

    @Test
    fun `dto hook`() {
        val dto = DtoModel<NormalDtoWithHook> {
            numberWithRounding = "1.4"
            numberWithRounding2 = "2.5"
        }

        Assertions.assertEquals("1", dto.numberWithRounding)
        Assertions.assertEquals("3", dto.numberWithRounding2)
        Assertions.assertEquals("1.4", dto.uncheckedCast<DtoMeta>().`$modelMap`["numberWithRounding"])
        Assertions.assertEquals("3", dto.uncheckedCast<DtoMeta>()["numberWithRounding2"])
    }

    interface NormalDtoWithDefaultValue : DtoModel {
        @get:DefaultValue("string")
        var value: String

        @get:DefaultValue("123")
        var intValue: Int
    }

    @Test
    fun `dto with default value`() {
        val dto = DtoModel<NormalDtoWithDefaultValue>()

        Assertions.assertEquals("string", dto.value)
        Assertions.assertEquals(123, dto.intValue)
    }
}

inline fun <reified T : Exception> assertCauseThrows(block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        var ex: Throwable? = e
        while (ex != null) {
            if (ex is T) {
                assertThrows<T> {
                    throw ex ?: return@assertThrows
                }
                return
            }

            ex = ex.cause
        }

        assertThrows<T> {}
    }
}

inline fun <reified T : Exception> assertCauseThrows(message: String, block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        var ex: Throwable? = e
        while (ex != null) {
            if (ex is T) {
                assertThrows<T>(message) {
                    throw ex ?: return@assertThrows
                }
                return
            }

            ex = ex.cause
        }

        assertThrows<T> {}
    }
}
