package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import com.bybutter.sisyphus.string.toCamelCase
import org.jooq.Field
import org.jooq.Table

class TableBasedJooqConditionBuilder(private val table: Table<*>, private val fieldNameMapping: Map<String, String> = mapOf()) : JooqConditionBuilder() {
    private val fieldCache = table.fields().associateBy { it.name.toCamelCase() }

    override fun field(name: String): Field<*>? {
        val fieldName = if (fieldNameMapping.isNotEmpty()) {
            fieldNameMapping[name] ?: return null
        } else {
            name
        }

        return fieldCache[fieldName.toCamelCase()]
    }
}
