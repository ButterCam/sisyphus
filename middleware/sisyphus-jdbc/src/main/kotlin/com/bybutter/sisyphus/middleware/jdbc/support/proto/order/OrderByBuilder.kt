package com.bybutter.sisyphus.middleware.jdbc.support.proto.order

import com.bybutter.sisyphus.dsl.ordering.grammar.OrderParser
import org.jooq.Field
import org.jooq.SortField

abstract class OrderByBuilder {
    fun visit(orderBy: OrderParser.StartContext): List<SortField<*>> {
        return visit(orderBy.expr() ?: return listOf())
    }

    fun visit(sortField: OrderParser.ExprContext): List<SortField<*>> {
        return sortField.order().map {
            visit(it)
        }
    }

    fun visit(sortField: OrderParser.OrderContext): SortField<*> {
        val field = field(sortField.field().text)

        if (sortField.DESC() != null) {
            return field.desc()
        }

        return field.asc()
    }

    abstract fun field(name: String): Field<*>
}
