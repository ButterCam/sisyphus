package com.bybutter.sisyphus.middleware.jdbc.transaction

import org.jooq.Transaction
import org.jooq.TransactionContext
import org.jooq.TransactionProvider

class SisyphusTransactionProvider : TransactionProvider {
    override fun rollback(ctx: TransactionContext) {
        val transaction = ctx.transaction() as SisyphusTransaction
        val context = transaction.context
        context.rollback()

        if (context is CoroutineTransactionContext) {
            context.restoreThreadContext(context, null)
        }
    }

    override fun commit(ctx: TransactionContext) {
        val transaction = ctx.transaction() as SisyphusTransaction
        val context = transaction.context
        context.commit()

        if (context is CoroutineTransactionContext) {
            context.restoreThreadContext(context, null)
        }
    }

    override fun begin(ctx: TransactionContext) {
        val context = CoroutineTransactionContext.current()?.nest() ?: CoroutineTransactionContext()
        ctx.transaction(SisyphusTransaction(context))

        if (context is CoroutineTransactionContext) {
            context.updateThreadContext(context)
        }
    }

    private data class SisyphusTransaction(val context: com.bybutter.sisyphus.middleware.jdbc.transaction.TransactionContext) :
        Transaction
}
