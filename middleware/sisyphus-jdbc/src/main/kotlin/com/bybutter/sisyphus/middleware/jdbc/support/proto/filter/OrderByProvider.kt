package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import org.jooq.OrderField

fun interface OrderByProvider {
    fun provideOrderByFields(): List<OrderField<*>>
}
