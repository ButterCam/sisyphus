package com.bybutter.sisyphus.dsl

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree

fun ParseTree.superParent(level: Int): ParseTree? {
    if (level < 1) throw IllegalArgumentException("level must be greater than 0.")
    var parent: ParseTree? = this
    for (i in 0 until level) {
        parent = parent?.parent
    }
    return parent
}

fun ParseTree.children(): List<ParseTree> {
    if (this is ParserRuleContext) return this.children
    return (0 until childCount).map { getChild(it) }
}

fun ParseTree.prevSibling(): ParseTree? {
    val parent = parent ?: return null
    val children = parent.children()
    val index = children.indexOf(this)
    if (index == 0) return null
    return children[index - 1]
}

fun ParseTree.nextSibling(): ParseTree? {
    val parent = parent ?: return null
    val children = parent.children()
    val index = children.indexOf(this)
    if (index == children.lastIndex) return null
    return children[index + 1]
}

fun ParseTree.firstLeaf(): ParseTree {
    var current = this
    while (!current.isLeaf()) {
        current = current.getChild(0)
    }
    return current
}

fun ParseTree.lastLeaf(): ParseTree {
    var current = this
    while (!current.isLeaf()) {
        current = current.getChild(current.childCount - 1)
    }
    return current
}

fun ParseTree.prevLeaf(): ParseTree? {
    var current = this
    while (true) {
        val prev = current.prevSibling()
        if (prev != null) {
            return prev.lastLeaf()
        }
        val parent = current.parent ?: return null
        current = parent
    }
}

fun ParseTree.nextLeaf(): ParseTree? {
    var current = this
    while (true) {
        val next = current.nextSibling()
        if (next != null) {
            return next.firstLeaf()
        }
        val parent = current.parent ?: return null
        current = parent
    }
}

fun ParseTree.isLeaf(): Boolean {
    return childCount == 0
}
