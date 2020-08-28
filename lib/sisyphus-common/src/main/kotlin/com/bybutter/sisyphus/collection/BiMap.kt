package com.bybutter.sisyphus.collection

interface BiMap<K, V> : Map<K, V> {
    override val values: Set<V>
    val inverse: BiMap<V, K>
}

interface MutableBiMap<K, V> : BiMap<K, V>, MutableMap<K, V> {
    override val values: MutableSet<V>
    override val inverse: MutableBiMap<V, K>
}

abstract class AbstractBiMap<K, V> protected constructor(
    private val direct: MutableMap<K, V>,
    private val reverse: MutableMap<V, K>
) : MutableBiMap<K, V> {
    override val size: Int
        get() = direct.size

    override val inverse: MutableBiMap<V, K> = object : AbstractBiMap<V, K>(reverse as MutableMap<K, V>, direct as MutableMap<V, K>) {
        override val inverse: MutableBiMap<K, V>
            get() = this@AbstractBiMap
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> =
        BiMapSet(direct.entries, { it.key }, { BiMapEntry(it) })

    override val keys: MutableSet<K>
        get() = BiMapSet(direct.keys, { it }, { it })

    override val values: MutableSet<V>
        get() = inverse.keys

    override fun put(key: K, value: V): V? {
        val oldValue = direct.put(key, value)
        oldValue?.let { reverse.remove(it) }
        val oldKey = reverse.put(value, key)
        oldKey?.let { direct.remove(it) }
        return oldValue
    }

    override fun putAll(from: Map<out K, V>) {
        from.entries.forEach { put(it.key, it.value) }
    }

    override fun remove(key: K): V? {
        val oldValue = direct.remove(key)
        oldValue?.let { reverse.remove(it) }
        return oldValue
    }

    override fun clear() {
        direct.clear()
        reverse.clear()
    }

    override fun get(key: K): V? {
        return direct[key]
    }

    override fun containsKey(key: K): Boolean {
        return key in direct
    }

    override fun containsValue(value: V): Boolean {
        return value in reverse
    }

    override fun isEmpty(): Boolean {
        return direct.isEmpty()
    }

    private inner class BiMapSet<T>(
        private val elements: MutableSet<T>,
        private val keyGetter: (T) -> K,
        private val elementWrapper: (T) -> T
    ) : MutableSet<T> by elements {
        override fun remove(element: T): Boolean {
            if (element !in this) {
                return false
            }

            val key = keyGetter(element)
            val value = direct.remove(key) ?: return false
            try {
                reverse.remove(value)
            } catch (throwable: Throwable) {
                direct.put(key, value)
                throw throwable
            }
            return true
        }

        override fun clear() {
            direct.clear()
            reverse.clear()
        }

        override fun iterator(): MutableIterator<T> {
            val iterator = elements.iterator()
            return BiMapSetIterator(iterator, keyGetter, elementWrapper)
        }
    }

    private inner class BiMapSetIterator<T>(
        private val iterator: MutableIterator<T>,
        private val keyGetter: (T) -> K,
        private val elementWrapper: (T) -> T
    ) : MutableIterator<T> {
        private var last: T? = null

        override fun hasNext(): Boolean {
            return iterator.hasNext()
        }

        override fun next(): T {
            val element = iterator.next().apply {
                last = this
            }
            return elementWrapper(element)
        }

        override fun remove() {
            last ?: throw NullPointerException("Move to an element before removing it")
            try {
                val key = keyGetter(last!!)
                val value = direct[key] ?: error("BiMap doesn't contain key $key ")
                reverse.remove(value)
                try {
                    iterator.remove()
                } catch (throwable: Throwable) {
                    reverse[value] = key
                    throw throwable
                }
            } finally {
                last = null
            }
        }
    }

    private inner class BiMapEntry(
        private val entry: MutableMap.MutableEntry<K, V>
    ) : MutableMap.MutableEntry<K, V> by entry {
        override fun setValue(newValue: V): V {
            if (entry.value == newValue) {
                reverse[newValue] = entry.key
                try {
                    return entry.setValue(newValue)
                } catch (throwable: Throwable) {
                    reverse[entry.value] = entry.key
                    throw throwable
                }
            } else {
                check(newValue !in reverse) { "BiMap already contains value $newValue" }
                reverse[newValue] = entry.key
                try {
                    return entry.setValue(newValue)
                } catch (throwable: Throwable) {
                    reverse.remove(newValue)
                    throw throwable
                }
            }
        }
    }
}

class HashBiMap<K, V>(capacity: Int = 16) : AbstractBiMap<K, V>(HashMap(capacity), HashMap(capacity))

class LinkedHashBiMap<K, V>(capacity: Int = 16) : AbstractBiMap<K, V>(LinkedHashMap(capacity), LinkedHashMap(capacity))

object EmptyBiMap : BiMap<Any, Any> {
    override val values: Set<Any> = setOf()
    override val inverse: BiMap<Any, Any> = EmptyBiMap
    override val entries: Set<Map.Entry<Any, Any>> = setOf()
    override val keys: Set<Any> = setOf()
    override val size: Int = 0

    override fun containsKey(key: Any): Boolean {
        return false
    }

    override fun containsValue(value: Any): Boolean {
        return false
    }

    override fun get(key: Any): Any? {
        return null
    }

    override fun isEmpty(): Boolean {
        return true
    }
}

@Suppress("UNCHECKED_CAST")
fun <K, V> emptyBiMapOf(): BiMap<K, V> {
    return EmptyBiMap as BiMap<K, V>
}

fun <K, V> biMapOf(): BiMap<K, V> {
    return emptyBiMapOf()
}

fun <K, V> biMapOf(vararg pairs: Pair<K, V>): BiMap<K, V> {
    return LinkedHashBiMap<K, V>().apply {
        this.putAll(pairs)
    }
}

fun <K, V> biMapOf(map: Map<K, V>): BiMap<K, V> {
    return LinkedHashBiMap<K, V>().apply {
        this.putAll(map)
    }
}

fun <K, V> biMapOf(map: Iterable<Pair<K, V>>): BiMap<K, V> {
    return LinkedHashBiMap<K, V>().apply {
        this.putAll(map)
    }
}

fun <K, V> Map<K, V>.toBiMap(): BiMap<K, V> {
    return biMapOf(this)
}

fun <K, V> mutableBiMapOf(): MutableBiMap<K, V> {
    return LinkedHashBiMap()
}

fun <K, V> mutableBiMapOf(vararg pairs: Pair<K, V>): MutableBiMap<K, V> {
    return LinkedHashBiMap<K, V>().apply {
        this.putAll(pairs)
    }
}

fun <K, V> mutableBiMapOf(map: Map<K, V>): MutableBiMap<K, V> {
    return LinkedHashBiMap<K, V>().apply {
        this.putAll(map)
    }
}

fun <K, V> mutableBiMapOf(map: Iterable<Pair<K, V>>): MutableBiMap<K, V> {
    return LinkedHashBiMap<K, V>().apply {
        this.putAll(map)
    }
}

fun <K, V> Map<K, V>.toMutableBiMap(): MutableBiMap<K, V> {
    return mutableBiMapOf(this)
}

fun <K, V> linkedBiMapOf(): LinkedHashBiMap<K, V> {
    return LinkedHashBiMap()
}

fun <K, V> linkedBiMapOf(vararg pairs: Pair<K, V>): LinkedHashBiMap<K, V> {
    return LinkedHashBiMap<K, V>().apply {
        this.putAll(pairs)
    }
}

fun <K, V> linkedBiMapOf(map: Map<K, V>): LinkedHashBiMap<K, V> {
    return LinkedHashBiMap<K, V>().apply {
        this.putAll(map)
    }
}

fun <K, V> linkedBiMapOf(map: Iterable<Pair<K, V>>): LinkedHashBiMap<K, V> {
    return LinkedHashBiMap<K, V>().apply {
        this.putAll(map)
    }
}

fun <K, V> hashBiMapOf(): HashBiMap<K, V> {
    return HashBiMap()
}

fun <K, V> hashBiMapOf(vararg pairs: Pair<K, V>): HashBiMap<K, V> {
    return HashBiMap<K, V>().apply {
        this.putAll(pairs)
    }
}

fun <K, V> hashBiMapOf(map: Map<K, V>): HashBiMap<K, V> {
    return HashBiMap<K, V>().apply {
        this.putAll(map)
    }
}

fun <K, V> hashBiMapOf(map: Iterable<Pair<K, V>>): HashBiMap<K, V> {
    return HashBiMap<K, V>().apply {
        this.putAll(map)
    }
}
