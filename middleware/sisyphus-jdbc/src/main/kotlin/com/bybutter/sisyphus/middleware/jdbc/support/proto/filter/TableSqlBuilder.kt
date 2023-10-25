package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import com.bybutter.sisyphus.dsl.filtering.grammar.FilterParser
import com.bybutter.sisyphus.string.toCamelCase
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.OrderField
import org.jooq.Record
import org.jooq.SelectConditionStep
import org.jooq.SelectJoinStep
import org.jooq.Table

open class TableSqlBuilder<T : Record>(private val table: Table<T>) : SqlBuilder<T>() {
    private val fieldMapping = mutableMapOf<String, Any>()
    final override var runtime: FilterRuntime = FilterRuntime()
        private set

    override fun buildSelect(
        dsl: DSLContext,
        expressions: List<Any?>,
        vararg fields: Field<*>,
    ): SelectConditionStep<T> {
        val selectedFields =
            if (fields.isEmpty()) {
                table.fields()
            } else {
                fields
            }

        val conditions =
            expressions.flatMap {
                when (it) {
                    is Condition -> listOf(it)
                    is ConditionProvider -> it.provideConditions()
                    else -> listOf()
                }
            }
        val joins =
            expressions.flatMap {
                when (it) {
                    is JoinProvider -> it.provideJoins()
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

    fun field(
        member: String,
        field: Field<*>,
        converter: ((Any?) -> Any?)? = null,
    ) {
        val camelCaseMember = toCamelCase(member)
        val fieldOrHandle = FieldHandle.wrap(field, converter)
        fieldMapping[member] = fieldOrHandle
        fieldMapping[camelCaseMember] = fieldOrHandle
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

fun <T : Record> sqlBuilder(
    table: Table<T>,
    block: TableSqlBuilder<T>.() -> Unit,
): TableSqlBuilder<T> {
    return TableSqlBuilder(table).apply(block)
}

fun Condition.withJoin(join: Join): SqlFilterPart {
    return SqlFilterPart(this, listOf(join), listOf())
}

fun Condition.orderBy(vararg orderBy: OrderField<*>): SqlFilterPart {
    return SqlFilterPart(this, listOf(), orderBy.toList())
}

fun Condition.filterPart(): SqlFilterPart {
    return SqlFilterPart(this, listOf(), listOf())
}
