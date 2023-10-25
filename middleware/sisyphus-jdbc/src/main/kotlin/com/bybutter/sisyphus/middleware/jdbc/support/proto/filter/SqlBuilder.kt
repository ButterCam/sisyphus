package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import com.bybutter.sisyphus.dsl.filtering.grammar.FilterParser
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.SelectConditionStep
import org.jooq.impl.DSL

abstract class SqlBuilder<T : Record> {
    abstract val runtime: FilterRuntime

    open fun select(
        dsl: DSLContext,
        filter: String,
    ): SelectConditionStep<T> {
        return selectFields(dsl, filter)
    }

    open fun selectFields(
        dsl: DSLContext,
        filter: String,
        vararg fields: Field<*>,
    ): SelectConditionStep<T> {
        return buildSelect(dsl, expressions(filter), *fields)
    }

    open fun select(
        dsl: DSLContext,
        filter: FilterParser.FilterContext,
    ): SelectConditionStep<T> {
        return selectFields(dsl, filter)
    }

    open fun selectFields(
        dsl: DSLContext,
        filter: FilterParser.FilterContext,
        vararg fields: Field<*>,
    ): SelectConditionStep<T> {
        return buildSelect(dsl, expressions(filter), *fields)
    }

    open fun condition(filter: String): Condition {
        val conditions =
            expressions(filter).flatMap {
                when (it) {
                    is Condition -> listOf(it)
                    is ConditionProvider -> it.provideConditions()
                    else -> listOf()
                }
            }
        return DSL.and(conditions)
    }

    open fun expressions(filter: String): List<Any?> {
        return FilterVisitor.DEFAULT.build(this, filter)
    }

    open fun expressions(filter: FilterParser.FilterContext): List<Any?> {
        return FilterVisitor.DEFAULT.visit(this, filter)
    }

    protected abstract fun buildSelect(
        dsl: DSLContext,
        expressions: List<Any?>,
        vararg fields: Field<*>,
    ): SelectConditionStep<T>

    abstract fun member(member: FilterParser.MemberContext): Any?
}
