package com.bybutter.sisyphus.collection

import java.util.Enumeration

private class IterableEnumeration<T>(iterator: Iterator<T>) : Enumeration<T>, Iterator<T> by iterator {
    override fun hasMoreElements(): Boolean {
        return hasNext()
    }

    override fun nextElement(): T {
        return next()
    }

    @Suppress("UNCHECKED_CAST")
    override fun asIterator(): MutableIterator<T> {
        return this as MutableIterator<T>
    }
}

/**
 * Make a [Enumeration] from [Iterator].
 */
fun <T> Iterator<T>.asEnumeration(): Enumeration<T> {
    return IterableEnumeration(this)
}

/**
 * Make a [Enumeration] from [Iterable].
 */
fun <T> Iterable<T>.asEnumeration(): Enumeration<T> {
    return IterableEnumeration(this.iterator())
}
