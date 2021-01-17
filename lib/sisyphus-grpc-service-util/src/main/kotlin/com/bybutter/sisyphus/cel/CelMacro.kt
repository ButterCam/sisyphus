package com.bybutter.sisyphus.cel

import com.bybutter.sisyphus.cel.grammar.CelParser
import com.bybutter.sisyphus.protobuf.Message

open class CelMacro {
    open fun has(context: CelContext, expr: CelParser.ExprContext): Boolean {
        if (!expr.text.matches(CelRuntime.memberRegex)) {
            throw IllegalArgumentException("Argument of 'has' macro '${expr.text}' must be a field selection.")
        }

        val part = expr.text.trim('.').split(".")
        var target: Any = context.global
        for (s in part) {
            when (target) {
                is Map<*, *> -> {
                    if (!target.containsKey(s)) return false
                    target = target[s] ?: return false
                }
                is Message<*, *> -> {
                    if (target.support().fieldInfo(s) == null) return false
                    if (!target.has(s)) return false
                    target = target[s]
                }
                else -> throw IllegalStateException("Just 'map' and 'message' type support nested field in 'has' macro.")
            }
        }

        return true
    }

    open fun List<*>.all(context: CelContext, name: CelParser.ExprContext, expr: CelParser.ExprContext): Boolean {
        if (!name.text.matches(CelRuntime.idRegex)) {
            throw IllegalArgumentException("Argument1 of 'all' macro '${name.text}' must be a identifier.")
        }
        val newContext = context.fork()

        return this.all {
            newContext.global[name.text] = it
            newContext.visit(expr) as? Boolean
                    ?: throw IllegalArgumentException("Argument2 of 'all' macro '${expr.text}' must return a bool.")
        }
    }

    open fun Map<*, *>.all(context: CelContext, name: CelParser.ExprContext, expr: CelParser.ExprContext): Boolean {
        return this.keys.toList().all(context, name, expr)
    }

    open fun List<*>.exists(context: CelContext, name: CelParser.ExprContext, expr: CelParser.ExprContext): Boolean {
        if (!name.text.matches(CelRuntime.idRegex)) {
            throw IllegalArgumentException("Argument1 of 'exists' macro '${name.text}' must be a identifier.")
        }
        val newContext = context.fork()

        return this.any {
            newContext.global[name.text] = it
            newContext.visit(expr) as? Boolean
                    ?: throw IllegalArgumentException("Argument2 of 'exists' macro '${expr.text}' must return a bool.")
        }
    }

    open fun Map<*, *>.exists(context: CelContext, name: CelParser.ExprContext, expr: CelParser.ExprContext): Boolean {
        return this.keys.toList().exists(context, name, expr)
    }

    open fun List<*>.exists_one(context: CelContext, name: CelParser.ExprContext, expr: CelParser.ExprContext): Boolean {
        if (!name.text.matches(CelRuntime.idRegex)) {
            throw IllegalArgumentException("Argument1 of 'exists_one' macro '${name.text}' must be a identifier.")
        }
        val newContext = context.fork()

        var counter: Int = 0

        for (value in this) {
            newContext.global[name.text] = value
            val result = newContext.visit(expr) as? Boolean
                    ?: throw IllegalArgumentException("Argument2 of 'exists_one' macro '${expr.text}' must return a bool.")
            if (result) {
                counter++
                if (counter > 1) return false
            }
        }

        return counter == 1
    }

    open fun Map<*, *>.exists_one(context: CelContext, name: CelParser.ExprContext, expr: CelParser.ExprContext): Boolean {
        return this.keys.toList().exists_one(context, name, expr)
    }

    open fun List<*>.map(context: CelContext, name: CelParser.ExprContext, expr: CelParser.ExprContext): List<*> {
        if (!name.text.matches(CelRuntime.idRegex)) {
            throw IllegalArgumentException("Argument1 of 'map' macro '${name.text}' must be a identifier.")
        }
        val newContext = context.fork()

        return this.map {
            newContext.global[name.text] = it
            newContext.visit(expr)
        }
    }

    open fun List<*>.filter(context: CelContext, name: CelParser.ExprContext, expr: CelParser.ExprContext): List<*> {
        if (!name.text.matches(CelRuntime.idRegex)) {
            throw IllegalArgumentException("Argument1 of 'filter' macro '${name.text}' must be a identifier.")
        }
        val newContext = context.fork()

        return this.filter {
            newContext.global[name.text] = it
            newContext.visit(expr) as? Boolean
                    ?: throw IllegalArgumentException("Argument2 of 'filter' macro '${expr.text}' must return a bool.")
        }
    }
}
