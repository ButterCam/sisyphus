package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import org.jooq.Condition

class SqlFilterPart(val condition: Condition, val joins: List<Join>) {
    fun and(other: SqlFilterPart): SqlFilterPart {
        return SqlFilterPart(condition.and(other.condition), joins + other.joins)
    }

    fun or(other: SqlFilterPart): SqlFilterPart {
        return SqlFilterPart(condition.or(other.condition), joins + other.joins)
    }

    fun not(): SqlFilterPart {
        return SqlFilterPart(condition.not(), joins)
    }
}
