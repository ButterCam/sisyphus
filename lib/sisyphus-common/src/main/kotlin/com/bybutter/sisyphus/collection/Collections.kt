package com.bybutter.sisyphus.collection

/**
 * Ensure a list is mutable, if the list is mutable already, do nothing, otherwise convert it to [MutableList]
 */
operator fun <T> List<T>.unaryPlus(): MutableList<T> {
    return if (this is MutableList) {
        this
    } else {
        this.toMutableList()
    }
}

/**
 * Ensure a map is mutable, if the map is mutable already, do nothing, otherwise convert it to [MutableMap]
 */
operator fun <TKey, TValue> Map<TKey, TValue>.unaryPlus(): MutableMap<TKey, TValue> {
    return if (this is MutableMap) {
        this
    } else {
        this.toMutableMap()
    }
}

fun <T> MutableList<T>.addNotNull(value: T?): MutableList<T> {
    value ?: return this
    this += value
    return this
}

fun <T> MutableList<T>.addAllNotNull(vararg values: T?): MutableList<T> {
    for (value in values) {
        value ?: continue
        this += value
    }
    return this
}

fun <T> MutableList<T>.addAllNotNull(values: Iterable<T?>): MutableList<T> {
    for (value in values) {
        value ?: continue
        this += value
    }
    return this
}

/**
 * Ensure a set is mutable, if the set is mutable already, do nothing, otherwise convert it to [MutableSet]
 */
operator fun <T> Set<T>.unaryPlus(): MutableSet<T> {
    return if (this is MutableSet) {
        this
    } else {
        this.toMutableSet()
    }
}

fun <T> List<T>.contentEquals(other: List<T>?): Boolean {
    other ?: return false

    if (size != other.size) {
        return false
    }

    return this.zip(other).all {
        it.first == it.second
    }
}

fun <TKey, TValue> Map<TKey, TValue>.contentEquals(other: Map<TKey, TValue>?): Boolean {
    other ?: return false

    if (size != other.size) {
        return false
    }

    if (!keys.containsAll(other.keys)) return false
    if (!other.keys.containsAll(keys)) return false
    if (keys.any { this[it] != other[it] }) return false

    return true
}

fun <T, R> Iterable<T>.firstNotNull(block: (T) -> R?): R? {
    for (value in this) {
        return block(value) ?: continue
    }
    return null
}

fun <T, R> Iterable<T>.firstNotNullOrDefault(default: R, block: (T) -> R?): R {
    for (value in this) {
        return block(value) ?: continue
    }
    return default
}

fun <T> MutableIterable<T>.takeWhen(block: (T) -> Boolean): List<T> {
    val iterator = this.iterator()
    val result = mutableListOf<T>()

    while (iterator.hasNext()) {
        val element = iterator.next()
        if (block(element)) {
            result += element
            iterator.remove()
        }
    }

    return result
}
