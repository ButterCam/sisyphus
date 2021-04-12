package com.bybutter.sisyphus.protobuf.primitives

import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.MutableMessage
import com.bybutter.sisyphus.protobuf.invoke
import java.util.SortedMap
import java.util.TreeMap

operator fun <T : Message<T, TM>, TM : MutableMessage<T, TM>> T.times(mask: FieldMask): T {
    return FieldMaskTree(mask).applyTo(this)
}

infix fun FieldMask.union(other: FieldMask): FieldMask {
    return this + other
}

operator fun FieldMask.Companion.invoke(vararg paths: String): FieldMask {
    return FieldMask {
        this.paths += paths.toList()
    }
}

operator fun FieldMask.Companion.invoke(paths: String): FieldMask {
    return FieldMask {
        this.paths += paths.split(',').map { it.trim() }
    }
}

operator fun FieldMask.plus(other: FieldMask): FieldMask {
    return FieldMaskTree(this).let {
        it.merge(FieldMaskTree(other))
        it.toFieldMask()
    }
}

fun FieldMask.string(): String {
    return this.paths.joinToString(",")
}

fun Message<*, *>.resolveMask(mask: FieldMask?): FieldMask {
    mask?.paths?.isNullOrEmpty() ?: return FieldMask {
        paths += this@resolveMask.support().fieldDescriptors.map { it.name }
    }

    return FieldMask {
        paths += mask.paths.mapNotNull {
            this@resolveMask.support().fieldInfo(it)?.name
        }.toSet()
    }
}

operator fun FieldMask?.rangeTo(message: Message<*, *>): Iterable<String> {
    return message.resolveMask(this).paths
}

inline fun FieldMask?.forEach(message: Message<*, *>, block: (String) -> Unit) {
    message.resolveMask(this).paths.forEach(block)
}

class FieldMaskTree {
    val children: SortedMap<String, FieldMaskTree> = TreeMap()

    constructor(vararg paths: String) : this(paths.asIterable())

    constructor(mask: FieldMask) : this(mask.paths)

    constructor(paths: Iterable<String>) {
        for (path in paths) {
            addPath(path)
        }
    }

    fun addPath(path: String) {
        addPath(splitPath(path))
    }

    fun removePath(path: String) {
        removePath(splitPath(path))
    }

    fun union(other: FieldMaskTree): FieldMaskTree {
        return FieldMaskTree((this.getPaths().asSequence() + other.getPaths().asSequence()).asIterable())
    }

    fun merge(other: FieldMaskTree) {
        for (path in getPaths()) {
            addPath(path)
        }
    }

    fun <T : Message<T, TM>, TM : MutableMessage<T, TM>> applyTo(proto: T): T {
        return proto {
            for ((field, value) in this) {
                if (!this.has(field.number)) {
                    continue
                }

                val tree = children[field.name]
                when {
                    tree == null -> {
                        this.clear(field.number)
                    }
                    tree.children.isNotEmpty() -> {
                        this[field.number] = applyTo(value as T)
                    }
                    else -> {
                    }
                }
            }
        }
    }

    fun toFieldMask(): FieldMask {
        return FieldMask {
            paths += getPaths()
        }
    }

    fun getPaths(): List<String> {
        val result = mutableListOf<String>()
        for ((path, tree) in children) {
            getPaths(path, result)
        }
        return result
    }

    private fun getPaths(prefix: String, result: MutableList<String>) {
        if (children.isEmpty()) {
            result.add(prefix)
            return
        }

        for ((path, tree) in children) {
            tree.getPaths("$prefix.$path", result)
        }
    }

    private fun addPath(pathParts: List<String>, index: Int = 0) {
        if (index == pathParts.size) {
            return
        }

        val part = pathParts[index]
        children.getOrPut(part) {
            FieldMaskTree()
        }.addPath(pathParts, index + 1)
    }

    private fun removePath(pathParts: List<String>, index: Int = 0) {
        if (index == pathParts.size - 1) {
            children.remove(pathParts[index])
        }

        children[pathParts[index]]?.removePath(pathParts, index + 1)
    }

    private fun splitPath(path: String): List<String> {
        return path.split('.')
    }
}
