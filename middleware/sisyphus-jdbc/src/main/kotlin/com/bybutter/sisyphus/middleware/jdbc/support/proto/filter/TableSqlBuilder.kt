package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import com.bybutter.sisyphus.dsl.filtering.grammar.FilterParser
import com.bybutter.sisyphus.string.toCamelCase
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
        val conditions = expressions.mapNotNull {
            when (it) {
                is SqlFilterPart -> it.condition
                is Condition -> it
                else -> null
            }
        }
        val joins = expressions.flatMap {
            when (it) {
                is SqlFilterPart -> it.joins
                is Join -> listOf(it)
                else -> listOf()
            }
        }.distinctBy { it.javaClass }

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
        fieldMapping[member.toCamelCase()] = field
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

    @Deprecated("Use func method to register custom function", ReplaceWith("func"))
    fun library(library: FilterStandardLibrary) {
        this.runtime = FilterRuntime(library)
    }

    fun <R> func(function: String, block: Function<R>) {
        this.runtime.register(function, block)
    }
}

fun <T : Record> sqlBuilder(table: Table<T>, block: TableSqlBuilder<T>.() -> Unit): TableSqlBuilder<T> {
    return TableSqlBuilder(table).apply(block)
}

fun Condition.withJoin(join: Join): SqlFilterPart {
    return SqlFilterPart(this, listOf(join))
}

fun Condition.filterPart(): SqlFilterPart {
    return SqlFilterPart(this, listOf())
}
