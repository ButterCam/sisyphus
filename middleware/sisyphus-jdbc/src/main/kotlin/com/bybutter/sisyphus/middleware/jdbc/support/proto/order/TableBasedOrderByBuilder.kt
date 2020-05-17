package com.bybutter.sisyphus.middleware.jdbc.support.proto.order

import com.bybutter.sisyphus.string.toCamelCase
import org.jooq.Field
import org.jooq.Table

class TableBasedOrderByBuilder(private val table: Table<*>, private val fieldNameMapping: Map<String, String> = mapOf()) : OrderByBuilder() {
    private val fieldCache = table.fields().associateBy { it.name.toCamelCase() }

    override fun field(name: String): Field<*> {
        val fieldName = if (fieldNameMapping.isNotEmpty()) {
            fieldNameMapping[name]
                ?: throw IllegalStateException("Field '$name' can't be used in database ConditionQL.")
        } else {
            name
        }

        return fieldCache[fieldName.toCamelCase()]
            ?: throw IllegalStateException("Field '$name' not existed in table '${table.name}'.")
    }
}
