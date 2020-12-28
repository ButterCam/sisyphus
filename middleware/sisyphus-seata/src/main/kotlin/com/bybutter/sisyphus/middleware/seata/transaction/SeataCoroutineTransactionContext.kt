package com.bybutter.sisyphus.middleware.seata.transaction

import io.seata.core.context.RootContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.ThreadContextElement

class SeataCoroutineTransactionContext(private val xid: String? = RootContext.getXID()) :
        AbstractCoroutineContextElement(SeataCoroutineTransactionContext),
        ThreadContextElement<String?> {

    companion object : CoroutineContext.Key<SeataCoroutineTransactionContext>

    override fun restoreThreadContext(context: CoroutineContext, oldState: String?) {
        if (oldState != xid && oldState != null) {
            RootContext.bind(oldState)
        } else {
            RootContext.unbind()
        }
    }

    override fun updateThreadContext(context: CoroutineContext): String? {
        return RootContext.getXID().apply {
            if (xid != null) RootContext.bind(xid)
        }
    }
}
