package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import com.bybutter.sisyphus.dsl.filtering.grammar.FilterParser
import com.bybutter.sisyphus.rpc.Code
import com.bybutter.sisyphus.rpc.StatusException
import com.bybutter.sisyphus.string.toCamelCase
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.SelectConditionStep
import org.jooq.Table
import org.jooq.impl.DSL

class JooqTableFilterContext<T : Record>(
    private val table: Table<T>,
    private val fieldNameMapping: Map<String, String> = mapOf()
) : JooqFilterContext<T>() {
    private val fieldCache = table.fields().associateBy { it.name.toCamelCase() }

    override fun select(dsl: DSLContext, filter: String): SelectConditionStep<T> {
        return dsl.selectFrom(table).where(
            JooqSqlBuilder.DEFAULT.build(this, filter) ?: DSL.trueCondition()
        )
    }

    override fun member(member: FilterParser.MemberContext): Field<*> {
        val field = member.text
        val fieldName = if (fieldNameMapping.isNotEmpty()) {
            fieldNameMapping[field] ?: throw StatusException(
                Code.INVALID_ARGUMENT,
                "Unsupported filter for '$field' field"
            )
        } else {
            field
        }
        return fieldCache[fieldName.toCamelCase()]
            ?: throw StatusException(Code.INVALID_ARGUMENT, "Unsupported filter for '$field' field")
    }
}
