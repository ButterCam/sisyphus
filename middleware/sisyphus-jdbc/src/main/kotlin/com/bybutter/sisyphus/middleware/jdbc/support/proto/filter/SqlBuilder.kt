package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import com.bybutter.sisyphus.dsl.filtering.grammar.FilterParser
import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.toLocalDateTime
import com.bybutter.sisyphus.protobuf.primitives.toTime
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectConditionStep
import java.util.concurrent.TimeUnit

abstract class SqlBuilder<T : Record> {
    abstract val runtime: FilterRuntime

    open fun select(dsl: DSLContext, filter: String): SelectConditionStep<T> {
        return select(dsl, FilterVisitor.DEFAULT.build(this, filter))
    }

    abstract fun select(dsl: DSLContext, expressions: List<Any?>): SelectConditionStep<T>

    abstract fun member(member: FilterParser.MemberContext): Any?

    open fun value(member: FilterParser.MemberContext, value: Any?): Any? {
        return when (value) {
            is Timestamp -> value.toLocalDateTime()
            is Duration -> value.toTime(TimeUnit.MILLISECONDS)
            else -> value
        }
    }
}
