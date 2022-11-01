package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import org.jooq.Condition
import org.jooq.OrderField

class SqlFilterPart(val condition: Condition, val joins: List<Join>, val orderBy: List<OrderField<*>>) :
    ConditionProvider, JoinProvider, OrderByProvider {
    fun and(other: SqlFilterPart): SqlFilterPart {
        return SqlFilterPart(condition.and(other.condition), joins + other.joins, orderBy + other.orderBy)
    }

    fun or(other: SqlFilterPart): SqlFilterPart {
        return SqlFilterPart(condition.or(other.condition), joins + other.joins, orderBy + other.orderBy)
    }

    fun not(): SqlFilterPart {
        return SqlFilterPart(condition.not(), joins, orderBy)
    }

    override fun provideConditions(): List<Condition> {
        return listOf(condition)
    }

    override fun provideJoins(): List<Join> {
        return joins
    }

    override fun provideOrderByFields(): List<OrderField<*>> {
        return orderBy
    }
}
