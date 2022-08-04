package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import com.bybutter.sisyphus.dsl.filtering.grammar.FilterParser
import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.toLocalDateTime
import com.bybutter.sisyphus.protobuf.primitives.toTime
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.SelectConditionStep
import java.util.concurrent.TimeUnit

abstract class SqlBuilder<T : Record> {
    abstract val runtime: FilterRuntime

    open fun select(dsl: DSLContext, filter: String): SelectConditionStep<T> {
        return selectFields(dsl, filter)
    }

    open fun selectFields(dsl: DSLContext, filter: String, vararg fields: Field<*>): SelectConditionStep<T> {
        return buildSelect(dsl, FilterVisitor.DEFAULT.build(this, filter), *fields)
    }

    protected abstract fun buildSelect(
        dsl: DSLContext,
        expressions: List<Any?>,
        vararg fields: Field<*>
    ): SelectConditionStep<T>

    abstract fun member(member: FilterParser.MemberContext): Any?

    open fun value(member: FilterParser.MemberContext, value: Any?): Any? {
        return when (value) {
            is Timestamp -> value.toLocalDateTime()
            is Duration -> value.toTime(TimeUnit.MILLISECONDS)
            else -> value
        }
    }
}
