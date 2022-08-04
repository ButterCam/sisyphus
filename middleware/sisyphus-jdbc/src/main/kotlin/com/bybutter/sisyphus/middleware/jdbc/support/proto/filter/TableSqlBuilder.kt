package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import com.bybutter.sisyphus.dsl.filtering.grammar.FilterParser
import com.bybutter.sisyphus.string.toCamelCase
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.SelectConditionStep
import org.jooq.SelectJoinStep
import org.jooq.Table

open class TableSqlBuilder<T : Record>(private val table: Table<T>) : SqlBuilder<T>() {
    private val fieldMapping = mutableMapOf<String, Any?>()
    private val converters = mutableMapOf<String, (Any?) -> (Any?)>()
    final override var runtime: FilterRuntime = FilterRuntime()
        private set

    override fun buildSelect(
        dsl: DSLContext,
        expressions: List<Any?>,
        vararg fields: Field<*>
    ): SelectConditionStep<T> {
        val selectedFields = if (fields.isEmpty()) {
            table.fields()
        } else {
            fields
        }

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

        return dsl.select(*selectedFields).from(table).run {
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
        val camelCaseMember = toCamelCase(member)
        fieldMapping[member] = field
        fieldMapping[camelCaseMember] = field
        converter?.let {
            converters[member] = it
            converters[camelCaseMember] = it
        }
    }

    fun converter(member: String, converter: (Any?) -> (Any?)) {
        val camelCaseMember = toCamelCase(member)
        converters[member] = converter
        converters[camelCaseMember] = converter
    }

    fun runtime(runtime: FilterRuntime) {
        this.runtime = runtime
    }

    fun library(library: FilterStandardLibrary) {
        this.runtime = FilterRuntime(library)
    }

    private fun toCamelCase(field: String): String {
        return field.split(".").map { it.toCamelCase() }.joinToString(".")
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
