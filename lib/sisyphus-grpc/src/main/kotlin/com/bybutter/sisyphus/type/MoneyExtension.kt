package com.bybutter.sisyphus.type

import com.bybutter.sisyphus.jackson.parseJson
import com.bybutter.sisyphus.protobuf.invoke
import java.math.BigDecimal
import java.util.Currency
import kotlin.math.abs
import kotlin.math.sign
import proto.internal.com.bybutter.sisyphus.type.MutableMoney

private const val nanosPerUnit = 1000000000L

val Money.Companion.STD get() = "STD"

/**
 * Create money based on STD currency, 1 STD = 0.01 CNY.
 */
fun Money.Companion.fromSTD(std: Long): Money {
    return Money {
        currencyCode = Money.STD
        units = std
        nanos = 0
    }
}

fun Money.Companion.supportCurrency(code: String): Boolean {
    return ExchangeRate.current.rates.containsKey(code)
}

/**
 * Convert money to specified currency.
 */
fun Money.convertTo(currencyCode: String, fallbackCurrencyCode: String = ""): Money {
    if (this.currencyCode == currencyCode) return this

    val targetRate = ExchangeRate.current.rates[currencyCode]
        ?: if (fallbackCurrencyCode.isEmpty()) {
            throw UnsupportedOperationException("Unsupported currency code '${this.currencyCode}'.")
        } else {
            return convertTo(fallbackCurrencyCode)
        }
    val currentRate = ExchangeRate.current.rates[this.currencyCode]
        ?: throw UnsupportedOperationException("Unsupported currency code '${this.currencyCode}'.")

    val rate = targetRate / currentRate

    val current = BigDecimal.valueOf(this.units) * BigDecimal.valueOf(nanosPerUnit) + BigDecimal.valueOf(this.nanos.toLong())
    val target = current * BigDecimal.valueOf(rate)

    return Money {
        this.currencyCode = currencyCode
        this.units = (target / BigDecimal.valueOf(nanosPerUnit)).toLong()
        this.nanos = (target % BigDecimal.valueOf(nanosPerUnit)).toInt()
    }
}

fun Money.toLocalizedString(): String {
    val money = this.units + 1.0 * this.nanos / nanosPerUnit
    val currency = Currency.getInstance(this.currencyCode)
    return "${currency.symbol} ${String.format("%.2f", money)}"
}

operator fun Money.plus(other: Money): Money {
    val other = if (other.currencyCode != this.currencyCode) {
        other.convertTo(this.currencyCode)
    } else {
        other
    }

    return this {
        units += other.units
        nanos += other.nanos
        normalized()
    }
}

operator fun Money.minus(other: Money): Money {
    val other = if (other.currencyCode != this.currencyCode) {
        other.convertTo(this.currencyCode)
    } else {
        other
    }

    return this {
        units -= other.units
        nanos -= other.nanos
        normalized()
    }
}

operator fun Money.unaryPlus(): Money {
    return this {}
}

operator fun Money.unaryMinus(): Money {
    return this {
        normalized()
        units = -units
        nanos = -nanos
    }
}

operator fun Money.compareTo(other: Money): Int {
    val other = if (other.currencyCode != this.currencyCode) {
        other.convertTo(this.currencyCode)
    } else {
        other
    }

    if (this.units != other.units) {
        return this.units.compareTo(other.units)
    }

    return this.nanos.compareTo(other.nanos)
}

private fun MutableMoney.normalized() {
    if (units.sign == 0 || nanos.sign == 0) {
        return
    }

    if (units.sign != nanos.sign) {
        units += nanos.sign
        nanos = ((nanosPerUnit - abs(nanos)) * units.sign).toInt()
    }

    if (nanos >= nanosPerUnit) {
        units += nanos / nanosPerUnit
        nanos %= nanosPerUnit.toInt()
    }
}

private data class ExchangeRate(
    val time_last_update_unix: Long,
    val time_next_update_unix: Long,
    val base_code: String,
    val rates: MutableMap<String, Double>
) {
    companion object {
        val current: ExchangeRate by lazy {
            ExchangeRate::class.java.classLoader.getResources("exchangerate.json").asSequence().map {
                it.readText().parseJson<ExchangeRate>()
            }.maxByOrNull {
                it.time_last_update_unix
            }?.apply {
                rates[Money.STD] = rates.getValue("CNY") * 100
            } ?: throw IllegalStateException("Read exchange rate failed.")
        }
    }
}
