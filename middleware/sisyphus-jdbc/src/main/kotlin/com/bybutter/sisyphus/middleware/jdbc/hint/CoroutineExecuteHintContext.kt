package com.bybutter.sisyphus.middleware.jdbc.hint

import com.bybutter.sisyphus.middleware.jdbc.transaction.CoroutineTransactionContext
import kotlinx.coroutines.ThreadContextElement
import java.util.Stack
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class CoroutineExecuteHintContext :
    AbstractCoroutineContextElement(CoroutineTransactionContext),
    ThreadContextElement<CoroutineExecuteHintContext?> {

    private var hints = Stack<ExecuteHint>()

    fun hint(hint: ExecuteHint): CoroutineExecuteHintContext {
        hints.push(hint)
        return this
    }

    fun popHint(): ExecuteHint? {
        return hints.pop()
    }

    fun wrapSql(sql: String): String {
        val used = mutableSetOf<String>()
        var result = sql
        while (hints.isNotEmpty()) {
            val hint = hints.pop()
            if (used.add(hint.key())) {
                result = hint.wrapSql(result)
            }
        }
        return result
    }

    override fun updateThreadContext(context: CoroutineContext): CoroutineExecuteHintContext? {
        return threadContext.get().also {
            threadContext.set(this)
        }
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: CoroutineExecuteHintContext?) {
        threadContext.set(oldState)
    }

    companion object : CoroutineContext.Key<CoroutineExecuteHintContext> {
        private val threadContext = ThreadLocal<CoroutineExecuteHintContext>()

        fun current(): CoroutineExecuteHintContext? {
            return threadContext.get()
        }
    }
}
