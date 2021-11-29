package com.bybutter.sisyphus.middleware.jdbc.support.proto.order

import com.bybutter.sisyphus.dsl.ordering.OrderDsl
import com.bybutter.sisyphus.dsl.ordering.grammar.OrderParser
import org.jooq.Field
import org.jooq.SortField

abstract class OrderByBuilder {
    fun build(orderBy: String): List<SortField<*>> {
        return visit(OrderDsl.parse(orderBy)).takeIf { it.isNotEmpty() } ?: default()
    }

    protected abstract fun default(): List<SortField<*>>

    protected fun visit(orderBy: OrderParser.StartContext): List<SortField<*>> {
        return visit(orderBy.expr() ?: return listOf())
    }

    protected fun visit(sortField: OrderParser.ExprContext): List<SortField<*>> {
        return sortField.order().map {
            visit(it)
        }
    }

    protected fun visit(sortField: OrderParser.OrderContext): SortField<*> {
        val field = field(sortField.field().text)

        if (sortField.DESC() != null) {
            return field.desc()
        }

        return field.asc()
    }

    abstract fun field(name: String): Field<*>
}
