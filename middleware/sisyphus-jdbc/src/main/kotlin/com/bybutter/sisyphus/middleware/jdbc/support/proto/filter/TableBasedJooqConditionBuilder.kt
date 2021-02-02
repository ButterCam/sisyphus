package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import com.bybutter.sisyphus.string.toCamelCase
import org.jooq.Condition
import org.jooq.Field
import org.jooq.Table

class TableBasedJooqConditionBuilder(
    val table: Table<*>,
    private val fieldNameMapping: Map<String, String> = mapOf()
) : JooqConditionBuilder() {
    private val fieldCache = table.fields().associateBy { it.name.toCamelCase() }

    override fun buildCondition(name: String, op: String, value: String): Condition? {
        val field = field(name) as? Field<Any> ?: return null
        val rightValue = fieldValue(field, value)

        return when (op) {
            "<=" -> field.le(rightValue)
            "<" -> field.lt(rightValue)
            ">=" -> field.ge(rightValue)
            ">" -> field.gt(rightValue)
            "=" -> field.eq(rightValue ?: return field.isNull)
            "!=" -> field.ne(rightValue ?: return field.isNotNull)
            ":" -> {
                if (value == "*") return field.isNotNull
                if (value == "null") return field.isNull
                if (value.startsWith("*") || value.endsWith("*")) {
                    return field.like(value.replace('*', '%'))
                }
                field.eq(rightValue)
            }
            else -> TODO()
        }
    }

    open fun fieldValue(field: Field<*>, value: Any?): Any? {
        if (value is Field<*>) return value
        return value
    }

    open fun field(name: String): Field<*>? {
        val fieldName = if (fieldNameMapping.isNotEmpty()) {
            fieldNameMapping[name] ?: return null
        } else {
            name
        }

        return fieldCache[fieldName.toCamelCase()]
    }
}
