package com.bybutter.sisyphus.middleware.jdbc.support.proto.order

import com.bybutter.sisyphus.string.toCamelCase
import org.jooq.Field
import org.jooq.SortField

class CustomOrderByBuilder : OrderByBuilder() {
    private val defaultOrder = mutableListOf<SortField<*>>()
    private val fieldMapping = mutableMapOf<String, Field<*>>()

    override fun field(name: String): Field<*> {
        return fieldMapping[name] ?: throw IllegalStateException("Order field '$name' not support.")
    }

    override fun default(): List<SortField<*>> {
        return defaultOrder
    }

    fun field(
        member: String,
        field: Field<*>,
    ) {
        fieldMapping[member] = field
        fieldMapping[member.toCamelCase()] = field
    }

    fun default(vararg fields: SortField<*>) {
        defaultOrder += fields
    }
}

fun orderByBuilder(block: CustomOrderByBuilder.() -> Unit): CustomOrderByBuilder {
    return CustomOrderByBuilder().apply(block)
}
