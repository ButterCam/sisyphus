package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import com.bybutter.sisyphus.dsl.filtering.grammar.FilterParser
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectConditionStep
import org.jooq.SelectJoinStep
import org.jooq.Table

open class TableSqlBuilder<T : Record>(private val table: Table<T>) : SqlBuilder<T>() {
    private val fieldMapping = mutableMapOf<String, Any?>()
    private val converters = mutableMapOf<String, (Any?) -> (Any?)>()
    final override var runtime: FilterRuntime = FilterRuntime()
        private set

    override fun select(dsl: DSLContext, expressions: List<Any?>): SelectConditionStep<T> {
        val joins = expressions.filterIsInstance<Join>()
        val conditions = expressions.mapNotNull {
            if (it is JooqConditionSupplier) {
                it.get()
            } else {
                it as? Condition
            }
        }

        return dsl.select().from(table).run {
            joins.fold(this as SelectJoinStep<*>) { step, join ->
                join.joinTable(step)
            }
        }.where(conditions) as SelectConditionStep<T>
    }

    override fun member(member: FilterParser.MemberContext): Any? {
        return fieldMapping[member.text]
    }

    override fun value(member: FilterParser.MemberContext, value: Any?): Any? {
        return converters[member.text]?.invoke(value) ?: super.value(member, value)
    }

    fun field(member: String, field: Any?, converter: ((Any?) -> Any?)? = null) {
        fieldMapping[member] = field
        converter?.let {
            converters[member] = converter
        }
    }

    fun converter(member: String, converter: (Any?) -> (Any?)) {
        converters[member] = converter
    }

    fun runtime(runtime: FilterRuntime) {
        this.runtime = runtime
    }

    fun library(library: FilterStandardLibrary) {
        this.runtime = FilterRuntime(library)
    }
}

fun <T : Record> sqlBuilder(table: Table<T>, block: TableSqlBuilder<T>.() -> Unit): TableSqlBuilder<T> {
    return TableSqlBuilder(table).apply(block)
}

class ConditionWithJoin(private val condition: Condition, join: Join) : Join by join, JooqConditionSupplier {
    override fun get(): Condition {
        return condition
    }
}

fun Condition.withJoin(join: Join): ConditionWithJoin {
    return ConditionWithJoin(this, join)
}
