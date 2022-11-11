package com.bybutter.sisyphus.dsl

import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

fun interface DslPattern {
    fun accept(context: ParseTree): Boolean

    companion object : DslPattern {
        override fun accept(context: ParseTree): Boolean {
            return true
        }

        fun <T : ParseTree> isInstance(clazz: Class<T>): DslPattern {
            return DslPattern {
                clazz.isInstance(it)
            }
        }

        inline fun <reified T : ParseTree> isInstance(): DslPattern {
            return isInstance(T::class.java)
        }
    }
}

fun DslPattern.and(other: DslPattern): DslPattern {
    return DslPattern {
        this.accept(it) && other.accept(it)
    }
}

fun DslPattern.andNot(other: DslPattern): DslPattern {
    return DslPattern { context ->
        this.accept(context) && !other.accept(context)
    }
}

fun DslPattern.not(): DslPattern {
    return DslPattern { context ->
        !this.accept(context)
    }
}

fun DslPattern.or(other: DslPattern): DslPattern {
    return DslPattern { context ->
        this.accept(context) || other.accept(context)
    }
}

fun <T : ParseTree> DslPattern.inside(clazz: Class<T>): DslPattern {
    return inside(DslPattern.isInstance(clazz))
}

inline fun <reified T : ParseTree> DslPattern.inside(): DslPattern {
    return inside(T::class.java)
}

fun DslPattern.inside(pattern: DslPattern): DslPattern {
    return and {
        var parent = it.parent
        while (parent != null) {
            if (pattern.accept(parent)) {
                return@and true
            }
            parent = parent.parent
        }
        return@and false
    }
}

fun DslPattern.insideSequence(vararg patterns: DslPattern): DslPattern {
    return and {
        var parent = it.parent
        var index = 0
        while (parent != null) {
            if (patterns[index].accept(parent)) {
                index++
                if (index == patterns.size) {
                    return@and true
                }
            }
            parent = parent.parent
        }
        return@and false
    }
}

fun <T : ParseTree> DslPattern.hasParent(clazz: Class<T>): DslPattern {
    return hasParent(DslPattern.isInstance(clazz))
}

inline fun <reified T : ParseTree> DslPattern.hasParent(): DslPattern {
    return hasParent(T::class.java)
}

fun DslPattern.hasParent(pattern: DslPattern): DslPattern {
    return and {
        pattern.accept(it.parent)
    }
}

fun <T : ParseTree> DslPattern.hasSuperParent(level: Int, clazz: Class<T>): DslPattern {
    return hasSuperParent(level, DslPattern.isInstance(clazz))
}

inline fun <reified T : ParseTree> DslPattern.hasSuperParent(level: Int): DslPattern {
    return hasSuperParent(level, T::class.java)
}

fun DslPattern.hasSuperParent(level: Int, pattern: DslPattern): DslPattern {
    return and {
        pattern.accept(it.superParent(level) ?: return@and false)
    }
}

fun DslPattern.allChildren(pattern: DslPattern): DslPattern {
    return and {
        for (i in 0 until it.childCount) {
            if (!pattern.accept(it.getChild(i))) {
                return@and false
            }
        }
        return@and true
    }
}

fun DslPattern.hasChildren(count: Int): DslPattern {
    return and {
        it.childCount == count
    }
}

fun DslPattern.hasChild(pattern: DslPattern): DslPattern {
    return and {
        for (i in 0 until it.childCount) {
            if (pattern.accept(it.getChild(i))) {
                return@and true
            }
        }
        return@and false
    }
}

fun DslPattern.hasFirstChild(pattern: DslPattern): DslPattern {
    return and {
        if (it.childCount == 0) return@and false
        pattern.accept(it.getChild(0))
    }
}

fun DslPattern.hasLastChild(pattern: DslPattern): DslPattern {
    return and {
        if (it.childCount == 0) return@and false
        pattern.accept(it.getChild(it.childCount - 1))
    }
}

fun DslPattern.beforeSibling(pattern: DslPattern): DslPattern {
    return and {
        return@and pattern.accept(it.nextSibling() ?: return@and false)
    }
}

fun DslPattern.afterSibling(pattern: DslPattern): DslPattern {
    return and {
        return@and pattern.accept(it.prevSibling() ?: return@and false)
    }
}

fun DslPattern.hasDescendant(pattern: DslPattern): DslPattern {
    return and {
        val stack = ArrayDeque<ParseTree>()
        for (i in 0 until it.childCount) {
            stack.add(it.getChild(i))
        }

        while (stack.isNotEmpty()) {
            val node = stack.removeFirst()
            if (pattern.accept(node)) {
                return@and true
            }
            for (i in 0 until node.childCount) {
                stack.add(node.getChild(i))
            }
        }
        return@and false
    }
}

fun DslPattern.afterLeaf(pattern: DslPattern): DslPattern {
    return and {
        return@and pattern.accept(it.prevLeaf() ?: return@and false)
    }
}

fun DslPattern.beforeLeaf(pattern: DslPattern): DslPattern {
    return and {
        return@and pattern.accept(it.nextLeaf() ?: return@and false)
    }
}

fun DslPattern.isFirstChild(): DslPattern {
    return and {
        it.prevSibling() == null
    }
}

fun DslPattern.isLastChild(): DslPattern {
    return and {
        it.nextSibling() == null
    }
}

fun DslPattern.isFirstLeaf(): DslPattern {
    return and {
        it.prevLeaf() == null
    }
}

fun DslPattern.isLastLeaf(): DslPattern {
    return and {
        val nextLeaf = it.nextLeaf()
        nextLeaf == null || nextLeaf is TerminalNode
    }
}
