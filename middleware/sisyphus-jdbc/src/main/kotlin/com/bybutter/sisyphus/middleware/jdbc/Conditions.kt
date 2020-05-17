package com.bybutter.sisyphus.middleware.jdbc

import org.jooq.Condition
import org.jooq.impl.DSL

operator fun Condition?.plus(other: Condition?): Condition? {
    this ?: return other
    other ?: return this
    if (other == DSL.trueCondition()) return other
    return this.and(other)
}
