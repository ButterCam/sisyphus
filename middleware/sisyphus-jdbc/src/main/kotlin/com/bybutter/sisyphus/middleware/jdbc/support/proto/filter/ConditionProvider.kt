package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import org.jooq.Condition

fun interface ConditionProvider {
    fun provideConditions(): List<Condition>
}
