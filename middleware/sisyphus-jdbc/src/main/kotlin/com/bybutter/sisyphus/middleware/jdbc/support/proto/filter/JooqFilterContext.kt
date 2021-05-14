package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import com.bybutter.sisyphus.dsl.filtering.grammar.FilterParser
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.toSql
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.SelectConditionStep

abstract class JooqFilterContext<T : Record> {
    abstract fun select(dsl: DSLContext, filter: String): SelectConditionStep<T>

    abstract fun member(member: FilterParser.MemberContext): Field<*>

    fun value(field: Field<*>, value: Any?): Any? {
        return when (value) {
            is Timestamp -> value.toSql()
            else -> value
        }
    }
}
