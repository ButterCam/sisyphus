package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import org.jooq.SelectJoinStep

fun interface Join {
    fun joinTable(step: SelectJoinStep<*>): SelectJoinStep<*>
}
