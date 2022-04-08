package com.bybutter.sisyphus.middleware.jdbc.hint

import org.jooq.ExecuteContext
import org.jooq.impl.DefaultExecuteListener

class HintExecuteListener : DefaultExecuteListener() {
    override fun renderEnd(ctx: ExecuteContext) {
        val context = CoroutineExecuteHintContext.current() ?: return
        ctx.sql(context.wrapSql(ctx.sql() ?: return))
    }
}
