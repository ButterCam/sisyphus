package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import org.jooq.Field

interface FieldHandle {
    fun field(): Field<*>

    fun valueConverter(): (Any?) -> Any?

    companion object {
        fun wrap(field: Field<*>, converter: ((Any?) -> Any?)?): Any {
            return converter?.let {
                Default(field, it)
            } ?: field
        }
    }

    private data class Default(private val field: Field<*>, private val converter: (Any?) -> Any?) {
        fun field(): Field<*> {
            return field
        }

        fun valueConverter(): (Any?) -> Any? {
            return converter
        }
    }
}
