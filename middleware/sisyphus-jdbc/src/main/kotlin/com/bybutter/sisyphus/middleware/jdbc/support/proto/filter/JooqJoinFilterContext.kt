package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import com.bybutter.sisyphus.dsl.filtering.grammar.FilterParser
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.SelectConditionStep
import org.jooq.SelectJoinStep
import org.jooq.Table
import org.jooq.TableField
import org.jooq.impl.DSL

abstract class JooqJoinFilterContext<T : Record>(private val table: Table<T>) : JooqFilterContext<T>() {
    private val joinedTables = mutableSetOf<Table<*>>()

    override fun select(dsl: DSLContext, filter: String): SelectConditionStep<T> {
        val condition = JooqSqlBuilder.DEFAULT.build(this, filter) ?: DSL.trueCondition()
        return select(dsl, condition)
    }

    fun select(dsl: DSLContext, condition: Condition?): SelectConditionStep<T> {
        return if (joinedTables.isEmpty()) {
            dsl.selectFrom(table).where(condition)
        } else {
            var step = dsl.select(*table.fields()).from(table) as SelectJoinStep<T>
            joinedTables.forEach {
                step = joinTable(step, it)
            }
            step.where(condition)
        }
    }

    protected abstract fun field(member: FilterParser.MemberContext): Field<*>

    protected abstract fun joinTable(step: SelectJoinStep<T>, table: Table<*>): SelectJoinStep<T>

    final override fun member(member: FilterParser.MemberContext): Field<*> {
        return field(member).apply {
            if (this is TableField<*, *>) {
                this.table?.let {
                    if (it != table) {
                        joinedTables += it
                    }
                }
            }
        }
    }
}
